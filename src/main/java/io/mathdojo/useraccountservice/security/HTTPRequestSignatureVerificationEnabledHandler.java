package io.mathdojo.useraccountservice.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpStatus;

import org.springframework.cloud.function.adapter.azure.AzureSpringBootRequestHandler;

import io.mathdojo.useraccountservice.services.SystemService;

public class HTTPRequestSignatureVerificationEnabledHandler<I, O> extends AzureSpringBootRequestHandler<I, O> {
    private static final Gson GSON = new Gson();
    private final SystemService systemService = new SystemService();

    public HTTPRequestSignatureVerificationEnabledHandler(Class<?> configurationClass) {
        super(configurationClass);
    }

    public HTTPRequestSignatureVerificationEnabledHandler() {
        super();
    }
    
    public Object handleRequest(HttpRequestMessage<Optional<I>> request, I inputObjectToBeHandled, ExecutionContext context) {
            String functionEnv = this.getSystemService().getFunctionEnv();
            if (functionEnv == null) {
                context.getLogger().log(Level.WARNING,
                    "MATH_DOJO_ENV_NAME is not set or is unrecognised; treating as non-local and enforcing signature verification");
            }
            if (!"local".equals(functionEnv)) {
                try {
                    String keyId = this.getSystemService().getVerifierPublicKeyId();
                    String b64Key = this.getSystemService().getVerifierPublicKey();
                    if (keyId == null || keyId.isEmpty() || b64Key == null || b64Key.isEmpty()) {
                        context.getLogger().log(Level.WARNING,
                            "Signature verification env vars are not set or are empty; rejecting request");
                        return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                            .body("signature verification failed")
                            .build();
                    }

                    HTTPRequestSignatureVerifier verifier = this.getVerifier(keyId, b64Key);

                    boolean verificationResult = verifier.verifySignatureHeader(
                        request.getHeaders(), request.getUri().getPath(), request.getHttpMethod());
                    if (!verificationResult) {
                        return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                            .body("signature verification failed")
                            .build();
                    }

                    // Digest header verification (no-op when the Digest header is absent)
                    byte[] bodyBytes = inputObjectToBeHandled != null
                        ? GSON.toJson(inputObjectToBeHandled).getBytes("UTF-8")
                        : new byte[0];
                    verifier.verifyDigestHeader(request.getHeaders(), bodyBytes);

                } catch (InvalidKeyException | SignatureException | UnsupportedEncodingException
                        | NoSuchAlgorithmException | HTTPRequestSignatureVerificationException e) {
                        context.getLogger().log(
                            Level.WARNING, "signature verification threw an exception", e);
                        return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                            .body("signature verification failed")
                            .build();
                } catch(Exception e) {
                        context.getLogger().log(
                            Level.WARNING, "signature verification failed for an unknown reason", e);
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

    private HTTPRequestSignatureVerifier getVerifier(String expectedKeyId, String expectB64PublicKeyDerString) throws NoSuchAlgorithmException {
            return createVerifier(expectedKeyId, expectB64PublicKeyDerString);
    }
    
        // getters and setters
    private HTTPRequestSignatureVerifier createVerifier(String expectedKeyId, String b64EncDerOfPublicKey)
            throws NoSuchAlgorithmException {
        Map<String, String> mapOfKeyIdAndB64EncDerPubKey = Collections.singletonMap(expectedKeyId,
                b64EncDerOfPublicKey);
        return new HTTPRequestSignatureVerifier(mapOfKeyIdAndB64EncDerPubKey);
    }
       
}
