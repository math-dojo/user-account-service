package io.mathdojo.useraccountservice.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Optional;
import java.util.logging.Level;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpStatus;

import org.springframework.cloud.function.adapter.azure.AzureSpringBootRequestHandler;

import io.mathdojo.useraccountservice.services.SystemService;

public class HTTPRequestSignatureVerificationEnabledHandler<I, O> extends AzureSpringBootRequestHandler<I, O> {
    private final SystemService systemService = new SystemService();

    public HTTPRequestSignatureVerificationEnabledHandler(Class<?> configurationClass) {
        super(configurationClass);
    }

    public HTTPRequestSignatureVerificationEnabledHandler() {
        super();
    }
    
    public Object handleRequest(HttpRequestMessage<Optional<I>> request, I inputObjectToBeHandled, ExecutionContext context) {
            if (!"local".equals(this.getSystemService().getFunctionEnv())) {
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
            return super.handleRequest(inputObjectToBeHandled, context);
    } 

    /**
     * This exposes the systemService being used for the purposes of mocking
     * in unit tests
     * @return SystemService
     */
    public SystemService getSystemService() {
		return systemService;
	}
       
}
