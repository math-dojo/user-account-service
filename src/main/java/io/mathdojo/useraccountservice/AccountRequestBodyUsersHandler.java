package io.mathdojo.useraccountservice;

import java.util.Optional;
import java.util.logging.Level;

import javax.validation.ConstraintViolationException;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import io.mathdojo.useraccountservice.model.User;
import io.mathdojo.useraccountservice.model.requestobjects.AccountModificationRequest;
import io.mathdojo.useraccountservice.security.HTTPRequestSignatureVerificationEnabledHandler;
import io.mathdojo.useraccountservice.services.IdentityService;
import io.mathdojo.useraccountservice.services.IdentityServiceException;

public class AccountRequestBodyUsersHandler
        extends HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, User> {

    @FunctionName("createUserInOrg")
    public HttpResponseMessage executePostForNewUserInOrg(
        @HttpTrigger(
            name = "request", 
            methods = { HttpMethod.POST }, 
            authLevel = AuthorizationLevel.ANONYMOUS, 
            route = "organisations/{orgId:alpha}/users"
            ) HttpRequestMessage<Optional<AccountModificationRequest>> request,
        @BindingName("orgId") String orgId,
        ExecutionContext context) {

        try {
            AccountModificationRequest requestBody = request.getBody().get();
            AccountModificationRequest modificationRequest = new AccountModificationRequest(
            		requestBody.getId(), orgId, requestBody.isAccountVerified(), requestBody.getName(), requestBody.getProfileImageLink()
            );
            Object handledRequest = handleRequest(request, modificationRequest, context);
            if (handledRequest instanceof HttpResponseMessage) {
                return (HttpResponseMessage) handledRequest;
            }
            User createdUser = (User) handledRequest;
            return request.createResponseBuilder(HttpStatus.CREATED).body(createdUser).build();

        } catch (ConstraintViolationException | IdentityServiceException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            context.getLogger().log(Level.WARNING, "New user creation failed", e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @FunctionName("getUserInOrg")
    public HttpResponseMessage executeGetForUserInOrg(
        @HttpTrigger(
            name = "request", 
            methods = { HttpMethod.GET }, 
            authLevel = AuthorizationLevel.ANONYMOUS, 
            route = "organisations/{orgId:alpha}/users/{userId:alpha}"
            ) HttpRequestMessage<Optional<AccountModificationRequest>> request,
        @BindingName("orgId") String orgId,
        @BindingName("userId") String userId,
        ExecutionContext context) {

        try {
            AccountModificationRequest modificationRequest = new AccountModificationRequest(
                userId, orgId, false, null, null
            );
            Object handledRequest = handleRequest(request, modificationRequest, context);
            if (handledRequest instanceof HttpResponseMessage) {
                return (HttpResponseMessage) handledRequest;
            }
            User retrievedUser = (User) handledRequest;
            return request.createResponseBuilder(HttpStatus.OK).body(retrievedUser).build();

        } catch (IdentityServiceException e) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            context.getLogger().log(Level.WARNING, "User retrieval by Id failed", e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @FunctionName("updateUserInOrg")
    public HttpResponseMessage executePutUserInOrg(@HttpTrigger(name = "request", methods = {
            HttpMethod.PUT }, authLevel = AuthorizationLevel.ANONYMOUS, route = "organisations/{orgId:alpha}/users/{userId:alpha}") HttpRequestMessage<Optional<AccountModificationRequest>> request,
            @BindingName("orgId") String orgId, @BindingName("userId") String userId, ExecutionContext context) {

        try {
            AccountModificationRequest requestBody = request.getBody().get();
            AccountModificationRequest modificationRequest = new AccountModificationRequest(userId, orgId,
                    requestBody.isAccountVerified(), requestBody.getName(), requestBody.getProfileImageLink());
            Object handledRequest = handleRequest(request, modificationRequest, context);
            if (handledRequest instanceof HttpResponseMessage) {
                return (HttpResponseMessage) handledRequest;
            }
            User finalResult = (User) handledRequest;
            return request.createResponseBuilder(HttpStatus.NO_CONTENT)
                .header("Content-Location", String
                    .format("/organisations/%s/users/%s", finalResult.getBelongsToOrgWithId(), 
                        finalResult.getId()))
                .build();

        } catch (ConstraintViolationException | IdentityServiceException e) {
            context.getLogger().log(Level.INFO, String.format("A user error in request %s to function %s caused a failure",
                context.getInvocationId(), context.getFunctionName()), e);
            if(IdentityService.UNKNOWN_ORGID_EXCEPTION_MSG == e.getMessage() || 
                IdentityService.UNKNOWN_USERID_EXCEPTION_MSG == e.getMessage()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).build();
            }
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).build();

        } catch (Exception e) {
            context.getLogger().log(Level.WARNING, String.format("A system error occured while processing request %s to function %s",
                context.getInvocationId(), context.getFunctionName()), e);            
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
