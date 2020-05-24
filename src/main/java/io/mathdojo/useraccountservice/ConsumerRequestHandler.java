package io.mathdojo.useraccountservice;

import java.util.Optional;
import java.util.logging.Level;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import io.mathdojo.useraccountservice.model.requestobjects.AccountModificationRequest;
import io.mathdojo.useraccountservice.security.HTTPRequestSignatureVerificationEnabledHandler;
import io.mathdojo.useraccountservice.services.IdentityServiceException;

public class ConsumerRequestHandler extends HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, String> {
    @FunctionName("deleteOrganisationById")
    public HttpResponseMessage executeDeleteByIdForOrganisations(
        @HttpTrigger(
            name = "request", 
            methods = { HttpMethod.DELETE }, 
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "organisations/{orgId:alpha}"
        ) HttpRequestMessage<Optional<AccountModificationRequest>> request,
        @BindingName("orgId") String orgId,
        ExecutionContext context) {

            try {
                AccountModificationRequest deletionRequest = new AccountModificationRequest(
                    orgId, null, false, null, null);
                Object handledRequest = handleRequest(request, deletionRequest, context);
                if(handledRequest instanceof HttpResponseMessage) {
                    return (HttpResponseMessage) handledRequest;
                }
                return request.createResponseBuilder(HttpStatus.NO_CONTENT)
                    .body("")
                    .build();

            } catch (IdentityServiceException e) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body(e.getMessage())
                    .build();
            } catch (Exception e) {
                context.getLogger().log(Level.WARNING, "Attempt to delete organisationBy Id failed", e);
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
            }

    }

    @FunctionName("deleteUserFromOrg")
    public HttpResponseMessage executeDeleteByIdForUser(
        @HttpTrigger(
            name = "request", 
            methods = { HttpMethod.DELETE }, 
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "organisations/{orgId:alpha}/users/{userId:alpha}"
            ) HttpRequestMessage<Optional<AccountModificationRequest>> request,
        @BindingName("orgId") String orgId,
        @BindingName("userId") String userId,
        ExecutionContext context) {

            try {
                AccountModificationRequest deletionRequest = new AccountModificationRequest(
                    userId, orgId, false, null, null);
                Object handledRequest = handleRequest(request, deletionRequest, context);
                if(handledRequest instanceof HttpResponseMessage) {
                    return (HttpResponseMessage) handledRequest;
                }
                return request.createResponseBuilder(HttpStatus.NO_CONTENT)
                    .body("")
                    .build();

            } catch (IdentityServiceException e) {
                context.getLogger().log(Level.INFO, String.format("A user error in request %s to function %s caused a failure",
                    context.getInvocationId(), context.getFunctionName()), e);
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body(e.getMessage())
                    .build();
            } catch (Exception e) {
                context.getLogger().log(Level.WARNING, String.format("A system error occured while processing request %s to function %s",
                    context.getInvocationId(), context.getFunctionName()), e);
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
            }

    }

    @FunctionName("updateUserPermissions")
    public HttpResponseMessage executeUpdateUserPermissions(
        @HttpTrigger(
            name = "request", 
            methods = { HttpMethod.PUT }, 
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "organisations/{orgId:alpha}/users/{userId:alpha}/permissions"
            ) HttpRequestMessage<Optional<AccountModificationRequest>> request,
        @BindingName("orgId") String orgId,
        @BindingName("userId") String userId,
        ExecutionContext context) {

            try {
                AccountModificationRequest permissionModRequest = AccountModificationRequest
                    .Builder.createBuilder().withAccountId(userId).withParentOrgId(orgId)
                    .withUserPermissions(request.getBody().get().getUserPermissions())
                    .build();
                Object handledRequest = handleRequest(request, permissionModRequest, context);
                if(handledRequest instanceof HttpResponseMessage) {
                    return (HttpResponseMessage) handledRequest;
                }
                return request.createResponseBuilder(HttpStatus.NO_CONTENT)
                    .body("")
                    .build();

            } catch (IdentityServiceException e) {
                context.getLogger().log(Level.INFO, String.format("A user error in request %s to function %s caused a failure",
                    context.getInvocationId(), context.getFunctionName()), e);
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body(e.getMessage())
                    .build();
            } catch (Exception e) {
                context.getLogger().log(Level.WARNING, String.format("A system error occured while processing request %s to function %s",
                    context.getInvocationId(), context.getFunctionName()), e);
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
            }

    }  
}