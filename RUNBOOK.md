# User Account Service — Operations Runbook

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [First-Time Environment Setup](#2-first-time-environment-setup)
3. [GitHub Environment Configuration Reference](#3-github-environment-configuration-reference)
4. [How Deployments Work](#4-how-deployments-work)
5. [Migrating to a New Azure Subscription](#5-migrating-to-a-new-azure-subscription)
6. [Rolling Back a Deployment](#6-rolling-back-a-deployment)
7. [Troubleshooting](#7-troubleshooting)

---

## 1. Architecture Overview

```
GitHub Actions CI/CD
    │
    ├── build job         Maven build + unit tests + local integration tests
    │                     (MongoDB sidecar container, Azure Functions Core Tools)
    │
    ├── deploy job        ./mvnw azure-functions:deploy
    │                     Maven plugin authenticates via Service Principal
    │                     Deploys the function package to the pre-created Function App
    │
    ├── configure job     az functionapp config appsettings set
    │                     Injects secrets and environment variables
    │
    └── integration-tests job   (PRs only)
                          Runs Cucumber suite against the live deployed function
```

**External dependencies (not provisioned here):**

| Dependency | Where | Notes |
|---|---|---|
| MongoDB | Atlas / self-hosted | URI stored as `MONGODB_URI` GitHub secret |
| API Gateway (Tyk / APIM) | Separate repo | Signs HTTP requests with RSA key from `environments/<env>.yml` |

---

## 2. First-Time Environment Setup

Run this once per environment (`non-production`, `pre-production`, `production`).

### Prerequisites

- [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) installed and authenticated (`az login`)
- Owner or Contributor + User Access Administrator on the target subscription
- Bash (WSL, macOS terminal, or Linux)

### Step 1 — Run the bootstrap script

```bash
chmod +x infrastructure/bootstrap.sh

./infrastructure/bootstrap.sh <environment> <subscription-id> [region]

# Examples:
./infrastructure/bootstrap.sh non-production xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx uksouth
./infrastructure/bootstrap.sh pre-production xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx uksouth
./infrastructure/bootstrap.sh production     xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx uksouth
```

The script provisions:

| # | Resource | Name pattern |
|---|---|---|
| 1 | Resource Group | `uas-<env>` |
| 2 | Storage Account | `uas<env-no-hyphens>sa` (max 24 chars, alphanumeric) |
| 3 | Application Insights | `uas-<env>-insights` |
| 4 | Consumption Function App (Java 8, v3) | `user-account-service-<env>` |
| 5 | Service Principal (Contributor on RG) | `uas-github-<env>` |

At the end it prints a summary block — copy these values for the next step:

```
╔══════════════════════════════════════════════════════════════╗
  Infrastructure ready for: non-production
╠══════════════════════════════════════════════════════════════╣

  GitHub Environment Variables (vars.*):
    AZURE_SUBSCRIPTION_ID          = xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
    AZURE_RESOURCE_GROUP           = uas-non-production
    APPINSIGHTS_INSTRUMENTATIONKEY = xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
    MATH_DOJO_ENV_NAME             = non-production
    SPRING_DATA_MONGODB_DATABASE   = non-production
    MATH_DOJO_HTTP_REQUEST_SIGNATURE_EXPECTED_KEYID     = <from environments/non-production.yml>
    MATH_DOJO_HTTP_REQUEST_SIGNATURE_B64_DER_PUBLIC_KEY = <from environments/non-production.yml>

  GitHub Environment Secrets (secrets.*):
    AZURE_CLIENT_ID                = xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
    AZURE_TENANT_ID                = xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
    AZURE_CLIENT_SECRET            = xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
    MONGODB_URI                    = <your external MongoDB URI for non-production>
╚══════════════════════════════════════════════════════════════╝
```

### Step 2 — Create the GitHub Environment

1. Navigate to **GitHub → Repository → Settings → Environments**.
2. Click **New environment** and name it exactly (e.g. `non-production`).
3. Optionally add **protection rules** (required reviewers, deployment branches).
4. Enter all `vars.*` values from the summary as **Variables**.
5. Enter all `secrets.*` values from the summary as **Secrets**.
6. For `MONGODB_URI`, use the full connection string from your external MongoDB provider.

Repeat for each environment.

### Step 3 — Fill in the signing key variables

The HTTP Signature public key for each environment lives in `environments/<env>.yml`:

| GitHub Variable | YAML key |
|---|---|
| `MATH_DOJO_HTTP_REQUEST_SIGNATURE_EXPECTED_KEYID` | `math-dojo.security.signature.expected-key-id` |
| `MATH_DOJO_HTTP_REQUEST_SIGNATURE_B64_DER_PUBLIC_KEY` | `math-dojo.security.signature.b64-der-public-key` |

Copy the values from the appropriate file into the GitHub Environment Variables.

### Step 4 — Trigger the first deploy

Push to `master` (targets `pre-production`) or any feature branch (targets `non-production`).

---

## 3. GitHub Environment Configuration Reference

### Variables (`vars.*`) — non-sensitive, visible in logs

| Variable | Description | Example |
|---|---|---|
| `AZURE_SUBSCRIPTION_ID` | Azure subscription GUID | `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| `AZURE_RESOURCE_GROUP` | Resource group name (from bootstrap) | `uas-non-production` |
| `APPINSIGHTS_INSTRUMENTATIONKEY` | Application Insights key (from bootstrap) | `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| `MATH_DOJO_ENV_NAME` | Logical environment name passed to the app | `non-production` |
| `SPRING_DATA_MONGODB_DATABASE` | MongoDB database name | `non-production` |
| `MATH_DOJO_HTTP_REQUEST_SIGNATURE_EXPECTED_KEYID` | Expected key ID for HTTP Signature verification | (from environments yml) |
| `MATH_DOJO_HTTP_REQUEST_SIGNATURE_B64_DER_PUBLIC_KEY` | RSA public key Base64-DER for signature verification | (from environments yml) |

### Secrets (`secrets.*`) — masked in logs

| Secret | Description |
|---|---|
| `AZURE_CLIENT_ID` | Service principal App ID |
| `AZURE_TENANT_ID` | Azure tenant ID |
| `AZURE_CLIENT_SECRET` | Service principal client secret |
| `MONGODB_URI` | Full MongoDB connection string (including credentials) |

---

## 4. How Deployments Work

### Branch → GitHub Environment mapping

| Branch pattern | GitHub Environment | Function App suffix |
|---|---|---|
| `ft/**`, `develop`, other feature branches | `non-production` | `<branch-name>` |
| `master` | `pre-production` | `master` |
| `rl/**` (release tags) | `production` | `<tag-name>` |
| Pull Request | `non-production` | `pr<N>` |

### Maven properties that drive deployment

`pom.xml` exposes three overridable properties. The defaults point to the old subscription — the workflow always overrides them at deploy time:

| Maven property | Default in pom.xml | Overridden by workflow |
|---|---|---|
| `subscriptionId` | `5594cd6c-...` (legacy) | `-DsubscriptionId=${{ vars.AZURE_SUBSCRIPTION_ID }}` |
| `functionResourceGroup` | `math-dojo-hzprod-...` (legacy) | `-DfunctionResourceGroup=${{ vars.AZURE_RESOURCE_GROUP }}` |
| `functionAppName` | `user-account-service-function` | `-DfunctionAppName=user-account-service-<branch>` |

> The defaults exist so local `mvnw azure-functions:deploy` still works when a developer has the legacy subscription configured. Override them locally with `-D` flags if targeting a different subscription.

---

## 5. Migrating to a New Azure Subscription

The entire migration is five steps. No code changes are needed.

### Step 1 — Provision infrastructure in the new subscription

```bash
./infrastructure/bootstrap.sh <environment> <new-subscription-id> [region]
```

Run for each environment you are migrating.

### Step 2 — Update two GitHub Environment Variables

In **GitHub → Settings → Environments → \<env\>**:

| Variable | New value |
|---|---|
| `AZURE_SUBSCRIPTION_ID` | New subscription GUID |
| `AZURE_RESOURCE_GROUP` | New RG name printed by bootstrap (e.g. `uas-non-production`) |
| `APPINSIGHTS_INSTRUMENTATIONKEY` | New key printed by bootstrap |

### Step 3 — Update three GitHub Environment Secrets

Replace with the new Service Principal credentials from the bootstrap output:

- `AZURE_CLIENT_ID`
- `AZURE_TENANT_ID`
- `AZURE_CLIENT_SECRET`

### Step 4 — Trigger deployment

Push any commit or re-run the last workflow. The Maven plugin deploys to the new subscription automatically — **no `pom.xml` changes required**.

### Step 5 — Decommission old resources (optional)

```bash
az group delete --name <old-resource-group> --yes --no-wait
az ad sp delete --id <old-client-id>
```

> **MongoDB is unaffected** — it's external and `MONGODB_URI` stays the same.

---

## 6. Rolling Back a Deployment

### Option A — Re-run a previous workflow run

1. Go to **GitHub → Actions**.
2. Find the last successful run for the target environment.
3. Click **Re-run all jobs**.

### Option B — Force-push a known-good commit

```bash
git checkout <good-commit>
git push origin HEAD:master --force   # targets pre-production
```

### Option C — Manual deploy from local machine

```bash
# Authenticate first
az login
az account set --subscription <subscription-id>

# Generate settings.xml with SP credentials
mkdir -p .mvn
cat > .mvn/settings.xml <<EOF
<settings>
  <servers>
    <server>
      <id>azure-auth</id>
      <configuration>
        <client>YOUR_CLIENT_ID</client>
        <tenant>YOUR_TENANT_ID</tenant>
        <key>YOUR_CLIENT_SECRET</key>
        <environment>AZURE</environment>
      </configuration>
    </server>
  </servers>
</settings>
EOF

./mvnw --settings .mvn/settings.xml \
  -DsubscriptionId=<subscription-id> \
  -DfunctionResourceGroup=uas-<env> \
  -DfunctionAppName=user-account-service-<env> \
  clean package azure-functions:deploy
```

---

## 7. Troubleshooting

### Deploy fails with "ResourceGroupNotFound"

The resource group doesn't exist in the target subscription. Run `bootstrap.sh`, then update `AZURE_RESOURCE_GROUP` in GitHub Environment Variables.

### Deploy fails with 401 / AuthenticationException

Service Principal credentials are wrong or expired. Re-run `bootstrap.sh` (it creates a new SP/secret), then update the three `AZURE_*` GitHub Secrets.

### Function App returns 500 after deploy

Check app settings are present:
```bash
az functionapp config appsettings list \
  --name "user-account-service-<env>" \
  --resource-group "uas-<env>"
```

Query Application Insights for exceptions:
```bash
az monitor app-insights query \
  --app "uas-<env>-insights" \
  --analytics-query "exceptions | order by timestamp desc | take 20"
```

### HTTP Signature verification fails (403)

- Confirm `MATH_DOJO_HTTP_REQUEST_SIGNATURE_EXPECTED_KEYID` matches the `keyId` the gateway sends.
- Confirm `MATH_DOJO_HTTP_REQUEST_SIGNATURE_B64_DER_PUBLIC_KEY` is the Base64-DER encoded public key matching the gateway's private key.
- Reference values: `environments/<env>.yml`.

### Local integration tests fail

Ensure MongoDB and the Functions host are running:

```bash
# Terminal 1 — MongoDB
docker run --rm -p 27017:27017 mongo:4.4

# Terminal 2 — Functions host (after build)
./mvnw clean package -DskipTests
cd target/azure-functions/user-account-service-function
func start

# Terminal 3 — Run tests
cd integration-tests
npm run local:test
```

