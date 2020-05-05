package io.mathdojo.useraccountservice;

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

import io.mathdojo.useraccountservice.model.DummyUser;
import io.mathdojo.useraccountservice.model.Greeting;
import io.mathdojo.useraccountservice.security.HTTPRequestSignatureVerificationEnabledHandler;

public class HelloHandler extends HTTPRequestSignatureVerificationEnabledHandler<DummyUser, Greeting> {

        @FunctionName("hello")
        public HttpResponseMessage execute(@HttpTrigger(name = "request", methods = { HttpMethod.GET,
                        HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<DummyUser>> request,
                        ExecutionContext context) {

                context.getLogger().info("URI path is: " + request.getUri().getPath());
                context.getLogger().info("Got headers: ");
                request.getHeaders().forEach((eachKey, eachValue) -> context.getLogger()
                                .info("Got header " + eachKey + " with value: " + eachValue));
                context.getLogger().info("Greeting user name: " + request.getBody().get().getName());

                try {
                        Object handledRequest = handleRequest(request, request.getBody().get(), context);
                        if (handledRequest instanceof HttpResponseMessage) {
                                return (HttpResponseMessage) handledRequest;
                        }
                        DummyUser dummyUser = (DummyUser) handledRequest;
                        return request.createResponseBuilder(HttpStatus.OK).body(dummyUser).build();

                } catch (Exception e) {
                        context.getLogger().log(Level.WARNING, "Hello handler failed", e);
                        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
        }
}
