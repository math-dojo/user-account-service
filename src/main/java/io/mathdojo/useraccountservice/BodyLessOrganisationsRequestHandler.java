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

import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.security.HTTPRequestSignatureVerificationEnabledHandler;
import io.mathdojo.useraccountservice.services.OrganisationServiceException;

public class BodyLessOrganisationsRequestHandler
        extends HTTPRequestSignatureVerificationEnabledHandler<String, Organisation> {

    @FunctionName("getOrganisationById")
    public HttpResponseMessage executeGetByIdForOrganisations(
        @HttpTrigger(
            name = "request", 
            methods = { HttpMethod.GET }, 
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "organisations/{orgId:alpha}"
        ) HttpRequestMessage<Optional<String>> request,
        @BindingName("orgId") String orgId,
        ExecutionContext context) {

            try {
                Object handledRequest = handleRequest(request, orgId, context);
                if(handledRequest instanceof HttpResponseMessage) {
                    return (HttpResponseMessage) handledRequest;
                }
                Organisation createdOrg = (Organisation) handledRequest;
                return request.createResponseBuilder(HttpStatus.OK)
                    .body(createdOrg)
                    .build();

            } catch (OrganisationServiceException e) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body(e.getMessage())
                    .build();
            } catch (Exception e) {
                context.getLogger().log(Level.WARNING, "Attempt to get organisationBy Id failed", e);
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
            }

    }
}
