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

import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.requestobjects.AccountModificationRequest;
import io.mathdojo.useraccountservice.security.HTTPRequestSignatureVerificationEnabledHandler;
import io.mathdojo.useraccountservice.services.OrganisationService;
import io.mathdojo.useraccountservice.services.OrganisationServiceException;

public class OrgnisationModificationRequestBodyHandler extends HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, Organisation>{
    

    @FunctionName("updateOrganisationById")
    public HttpResponseMessage executePutForOrganisations(@HttpTrigger(name = "request", methods = {
            HttpMethod.PUT }, authLevel = AuthorizationLevel.ANONYMOUS, route = "organisations/{orgId:alpha}") HttpRequestMessage<Optional<AccountModificationRequest>> request,
            @BindingName("orgId") String orgId, ExecutionContext context) {

        try {
            AccountModificationRequest modificationRequest = new AccountModificationRequest(orgId, 
                request.getBody().get().isAccountVerified(), request.getBody().get().getName(), 
                request.getBody().get().getProfileImageLink());
            Object handledRequest = handleRequest(request, modificationRequest, context);
            if (handledRequest instanceof HttpResponseMessage) {
                return (HttpResponseMessage) handledRequest;
            }
            Organisation finalResult = (Organisation) handledRequest;
            return request.createResponseBuilder(HttpStatus.NO_CONTENT)
                .header("Content-Type", "/organisations/"+finalResult.getId())
                .body(finalResult).build();

        } catch (ConstraintViolationException | OrganisationServiceException e) {
            if(OrganisationService.UNKNOWN_ORGID_EXCEPTION_MSG == e.getMessage()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).build();
            }
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            context.getLogger().log(Level.WARNING, "Organisation update operation for "+ orgId + " failed", e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}