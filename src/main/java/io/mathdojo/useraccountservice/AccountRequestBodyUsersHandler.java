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
import io.mathdojo.useraccountservice.services.OrganisationServiceException;

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
                null, orgId, requestBody.isAccountVerified(), requestBody.getName(), requestBody.getProfileImageLink()
            );
            Object handledRequest = handleRequest(request, modificationRequest, context);
            if (handledRequest instanceof HttpResponseMessage) {
                return (HttpResponseMessage) handledRequest;
            }
            User createdOrg = (User) handledRequest;
            return request.createResponseBuilder(HttpStatus.CREATED).body(createdOrg).build();

        } catch (ConstraintViolationException | OrganisationServiceException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            context.getLogger().log(Level.WARNING, "New user creation failed", e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}