package io.mathdojo.useraccountservice.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.HashMap;
import java.util.logging.Logger;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.mathdojo.useraccountservice.UserAccountServiceApplication;
import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;
import io.mathdojo.useraccountservice.services.SystemService;

@ExtendWith(SpringExtension.class)
public class HTTPRequestSignatureVerificationEnabledHandlerTest {

    private ExecutionContext mockExecContext;
    private HttpRequestMessage mockMessage;
    private SystemService mockSystemService;

    @BeforeEach
    public void setUp() {
        Logger testLogger = mock(Logger.class);
        mockExecContext = mock(ExecutionContext.class);
        mockMessage = mock(HttpRequestMessage.class);
        mockSystemService = mock(SystemService.class);
        when(mockSystemService.getFunctionEnv()).thenReturn("local");

        Mockito.when(mockExecContext.getLogger()).thenReturn(testLogger);

    }

    @Test
    public void testUnauthorizedResponseIfNotLocalEnvAndNoSignature() throws Exception {
        HTTPRequestSignatureVerificationEnabledHandler<AccountRequest, Organisation> handler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                UserAccountServiceApplication.class);
        HTTPRequestSignatureVerificationEnabledHandler<AccountRequest, Organisation> handlerSpy = Mockito.spy(handler);
        Mockito.doReturn(mockSystemService).when(handlerSpy).getSystemService();

        when(mockSystemService.getFunctionEnv()).thenReturn("non-production");
        when(mockSystemService.getVerifierPublicKeyId()).thenReturn("some-keyId");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyGen.generateKeyPair();
        String encodedPublicKeyString = Base64.getEncoder()
            .encodeToString(keyPair.getPublic().getEncoded());
        when(mockSystemService.getVerifierPublicKey()).thenReturn(encodedPublicKeyString);

        HttpResponseMessage mockResponseMessage = mock(HttpResponseMessage.class);
        HttpResponseMessage.Builder mockResponseMessageBuilder = mock(HttpResponseMessage.Builder.class);
        
        when(mockMessage.getHeaders()).thenReturn(new HashMap<>());
        when(mockMessage.getUri()).thenReturn(new URI("https", "my.server.com", "/path"));
        when(mockMessage.getHttpMethod()).thenReturn(HttpMethod.GET);
        when(mockMessage.createResponseBuilder(HttpStatus.UNAUTHORIZED)).thenReturn(mockResponseMessageBuilder);
        when(mockResponseMessageBuilder.body(anyString())).thenReturn(mockResponseMessageBuilder);
        when(mockResponseMessageBuilder.build()).thenReturn(mockResponseMessage);
        when(mockResponseMessage.getStatus()).thenReturn(HttpStatus.UNAUTHORIZED);
        
        when(mockExecContext.getFunctionName()).thenReturn("createOrganisation");
        String profileImageLink = "https://profileImageLink";
        HttpResponseMessage responseMessage = (HttpResponseMessage) handlerSpy.handleRequest(mockMessage, new AccountRequest(false, "foo", profileImageLink), mockExecContext);
        handler.close();
        assertThat(responseMessage.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
