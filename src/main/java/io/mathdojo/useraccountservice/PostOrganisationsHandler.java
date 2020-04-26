package io.mathdojo.useraccountservice;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Optional;
import java.util.logging.Level;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import org.springframework.cloud.function.adapter.azure.AzureSpringBootRequestHandler;

import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;
import io.mathdojo.useraccountservice.security.HTTPRequestSignatureVerificationException;
import io.mathdojo.useraccountservice.security.HTTPRequestSignatureVerifierSingleton;

public class PostOrganisationsHandler extends AzureSpringBootRequestHandler<AccountRequest, Organisation> {

    @FunctionName("createOrganisation")
    public HttpResponseMessage executePostForOrganisations(
        @HttpTrigger(
            name = "request", 
            methods = { HttpMethod.POST }, 
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "organisations"
        ) HttpRequestMessage<Optional<AccountRequest>> request,
        ExecutionContext context) {
            if (!"local".equals(System.getenv("MATH_DOJO_ENV_NAME"))) {
                try {
                    boolean verificationResult = HTTPRequestSignatureVerifierSingleton
                        .getInstance().verifySignatureHeader(request.getHeaders(),
                        request.getUri().getPath(), request.getHttpMethod());
                    if(!verificationResult) {
                        return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                            .body("signature verification failed")
                            .build();                    
                    }
                } catch (InvalidKeyException | SignatureException | UnsupportedEncodingException
                        | NoSuchAlgorithmException | HTTPRequestSignatureVerificationException e) {
                        context.getLogger().log(
                            Level.WARNING, "signature verification threw an exception", e);
                        return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                            .body("signature verification failed")
                            .build();
                }
            }

            try {
                Organisation createdOrg = handleRequest(request.getBody().get(), context);
                return request.createResponseBuilder(HttpStatus.CREATED)
                    .body(createdOrg)
                    .build();

            } catch (Exception e) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .build();
            }

    }
}
