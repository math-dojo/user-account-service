package io.mathdojo.useraccountservice;

import java.util.Optional;

import javax.validation.ConstraintViolationException;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;
import io.mathdojo.useraccountservice.security.HTTPRequestSignatureVerificationEnabledHandler;
import io.mathdojo.useraccountservice.services.OrganisationServiceException;

public class PostOrganisationsHandler extends HTTPRequestSignatureVerificationEnabledHandler<AccountRequest, Organisation> {

    @FunctionName("createOrganisation")
    public HttpResponseMessage executePostForOrganisations(
        @HttpTrigger(
            name = "request", 
            methods = { HttpMethod.POST }, 
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "organisations"
        ) HttpRequestMessage<Optional<AccountRequest>> request,
        ExecutionContext context) {

            try {
                Organisation createdOrg = (Organisation) handleRequest(request, request.getBody().get(), context);
                return request.createResponseBuilder(HttpStatus.CREATED)
                    .body(createdOrg)
                    .build();

            } catch (ConstraintViolationException | OrganisationServiceException e) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .build();
            } catch (Exception e) {
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
            }

    }
}
