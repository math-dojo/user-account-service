#!/usr/bin/env bash
# Provisions the Azure serverless infrastructure for the User Account Service.
# Usage:   ./infrastructure/bootstrap.sh <environment> <subscription-id> [region]
# Example: ./infrastructure/bootstrap.sh non-production xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx uksouth
#
# What this creates:
#   1. Resource Group
#   2. Storage Account (required by Functions runtime)
#   3. Application Insights
#   4. Consumption Function App (Java 8, Functions v3)
#   5. Service Principal scoped to the resource group (for GitHub Actions)
#
# What this does NOT manage:
#   - MongoDB (external — Atlas or self-hosted)
#   - Function App settings (managed by the deploy workflow via az functionapp config)

set -euo pipefail

# ── Argument validation ────────────────────────────────────────────────────────
if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <environment> <subscription-id> [region]"
  echo "  environment     e.g. non-production, pre-production, production"
  echo "  subscription-id Azure subscription GUID"
  echo "  region          Azure region (default: uksouth)"
  exit 1
fi

ENV=$1
SUB=$2
REGION=${3:-uksouth}

# ── Resource names ─────────────────────────────────────────────────────────────
# Storage account: alphanumeric only, max 24 chars
RG="uas-${ENV}"
STORAGE="uas${ENV//[-_]/}sa"
STORAGE="${STORAGE:0:24}"
PLAN="uas-${ENV}-plan"
FUNC_APP="user-account-service-${ENV}"
APP_INSIGHTS="uas-${ENV}-insights"
SP_NAME="uas-github-${ENV}"

echo ""
echo "  Environment  : $ENV"
echo "  Subscription : $SUB"
echo "  Region       : $REGION"
echo "  Resource Group: $RG"
echo "  Function App : $FUNC_APP"
echo ""

# ── 1. Subscription ────────────────────────────────────────────────────────────
echo "==> [1/5] Setting active subscription to $SUB"
az account set --subscription "$SUB"

# ── 2. Resource Group ──────────────────────────────────────────────────────────
echo "==> [2/5] Creating resource group: $RG in $REGION"
az group create \
  --name "$RG" \
  --location "$REGION" \
  --output none

# ── 3. Storage Account ─────────────────────────────────────────────────────────
echo "==> [3/5] Creating storage account: $STORAGE"
az storage account create \
  --name "$STORAGE" \
  --resource-group "$RG" \
  --location "$REGION" \
  --sku Standard_LRS \
  --allow-blob-public-access false \
  --min-tls-version TLS1_2 \
  --output none

# ── 4. Application Insights + Function App ─────────────────────────────────────
echo "==> [4/5] Creating Application Insights: $APP_INSIGHTS"
az extension add --name application-insights --only-show-errors 2>/dev/null || true
az monitor app-insights component create \
  --app "$APP_INSIGHTS" \
  --location "$REGION" \
  --resource-group "$RG" \
  --kind web \
  --output none

INSTRUMENTATION_KEY=$(az monitor app-insights component show \
  --app "$APP_INSIGHTS" \
  --resource-group "$RG" \
  --query instrumentationKey \
  --output tsv)

echo "==> [4b/5] Creating Consumption Function App: $FUNC_APP"
az functionapp create \
  --name "$FUNC_APP" \
  --resource-group "$RG" \
  --storage-account "$STORAGE" \
  --consumption-plan-location "$REGION" \
  --runtime java \
  --runtime-version 8 \
  --functions-version 3 \
  --app-insights "$APP_INSIGHTS" \
  --disable-app-insights false \
  --output none

# ── 5. Service Principal ───────────────────────────────────────────────────────
echo "==> [5/5] Creating service principal: $SP_NAME (Contributor on $RG)"
SP_JSON=$(az ad sp create-for-rbac \
  --name "$SP_NAME" \
  --role Contributor \
  --scopes "/subscriptions/${SUB}/resourceGroups/${RG}")

CLIENT_ID=$(echo "$SP_JSON"     | python3 -c "import sys,json; print(json.load(sys.stdin)['appId'])")
TENANT_ID=$(echo "$SP_JSON"     | python3 -c "import sys,json; print(json.load(sys.stdin)['tenant'])")
CLIENT_SECRET=$(echo "$SP_JSON" | python3 -c "import sys,json; print(json.load(sys.stdin)['password'])")

# ── Summary ────────────────────────────────────────────────────────────────────
echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "  Infrastructure ready for: $ENV"
echo "╠══════════════════════════════════════════════════════════════╣"
echo ""
echo "  GitHub Environment Variables (vars.*):"
echo "    AZURE_SUBSCRIPTION_ID                    = $SUB"
echo "    AZURE_RESOURCE_GROUP                     = $RG"
echo "    APPINSIGHTS_INSTRUMENTATIONKEY           = $INSTRUMENTATION_KEY"
echo "    MATH_DOJO_ENV_NAME                       = $ENV"
echo "    SPRING_DATA_MONGODB_DATABASE             = $ENV"
echo "    MATH_DOJO_HTTP_REQUEST_SIGNATURE_EXPECTED_KEYID      = <key-id from environments/${ENV}.yml>"
echo "    MATH_DOJO_HTTP_REQUEST_SIGNATURE_B64_DER_PUBLIC_KEY  = <public key from environments/${ENV}.yml>"
echo ""
echo "  GitHub Environment Secrets (secrets.*):"
echo "    AZURE_CLIENT_ID                          = $CLIENT_ID"
echo "    AZURE_TENANT_ID                          = $TENANT_ID"
echo "    AZURE_CLIENT_SECRET                      = $CLIENT_SECRET"
echo "    MONGODB_URI                              = <your external MongoDB URI for $ENV>"
echo ""
echo "╚══════════════════════════════════════════════════════════════╝"

