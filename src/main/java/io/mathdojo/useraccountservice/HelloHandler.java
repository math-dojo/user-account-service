package io.mathdojo.useraccountservice;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Collections;
import java.util.Map;
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

import io.mathdojo.useraccountservice.model.Greeting;
import io.mathdojo.useraccountservice.model.User;
import io.mathdojo.useraccountservice.security.HTTPRequestSignatureVerificationException;
import io.mathdojo.useraccountservice.security.HTTPRequestSignatureVerifier;

public class HelloHandler extends AzureSpringBootRequestHandler<User, Greeting> {

    @FunctionName("hello")
    public HttpResponseMessage execute(@HttpTrigger(name = "request", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<User>> request,
            ExecutionContext context) throws NoSuchAlgorithmException {

        if (!System.getenv("MATH_DOJO_ENV_NAME").equals("local")) {
            HTTPRequestSignatureVerifier verifier = createVerifier(
                    System.getenv("MATH_DOJO_HTTP_REQUEST_SIGNATURE_EXPECTED_KEYID"),
                    System.getenv("MATH_DOJO_HTTP_REQUEST_SIGNATURE_B64_DER_PUBLIC_KEY"));
            try {
                boolean verificationResult = verifier.verifySignatureHeader(request.getHeaders(),
                        request.getUri().getPath(), request.getHttpMethod());
                if(!verificationResult) {
                    return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                        .body("signature verification failed")
                        .build();                    
                }
            } catch (InvalidKeyException | SignatureException | UnsupportedEncodingException
                    | HTTPRequestSignatureVerificationException e) {
                    context.getLogger().log(
                        Level.WARNING, "signature verification threw an exception", e);
                    return request.createResponseBuilder(HttpStatus.UNAUTHORIZED)
                        .body("signature verification failed")
                        .build();
            }
        }

        context.getLogger().info("URI path is: "+request.getUri().getPath());
        context.getLogger().info("Got headers: ");
        request.getHeaders().forEach((eachKey, eachValue) -> context.getLogger()
                .info("Got header " + eachKey + " with value: " + eachValue));
        context.getLogger().info("Greeting user name: " + request.getBody().get().getName());

        return request.createResponseBuilder(HttpStatus.OK)
                .body(handleRequest(request.getBody().get(), context))
                .build();
    }

    private HTTPRequestSignatureVerifier createVerifier(String expectedKeyId, String b64EncDerOfPublicKey)
            throws NoSuchAlgorithmException {
        Map<String, String> mapOfKeyIdAndB64EncDerPubKey = Collections.singletonMap(expectedKeyId,
                b64EncDerOfPublicKey);
        return new HTTPRequestSignatureVerifier(mapOfKeyIdAndB64EncDerPubKey);
    }
}