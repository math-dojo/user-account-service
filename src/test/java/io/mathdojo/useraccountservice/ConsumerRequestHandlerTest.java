package io.mathdojo.useraccountservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.logging.Logger;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.mathdojo.useraccountservice.model.requestobjects.AccountModificationRequest;

public class ConsumerRequestHandlerTest {
    private ExecutionContext mockExecContext;
    private HttpRequestMessage mockMessage;

    @BeforeEach
    public void setup() {
        Logger testLogger = mock(Logger.class);
        mockExecContext = mock(ExecutionContext.class);
        mockMessage = mock(HttpRequestMessage.class);

        Mockito.when(mockExecContext.getLogger()).thenReturn(testLogger);
    }

    @Test
    public void testDeleteOrgByIdReturns401FromVerificationFailure() {
        ConsumerRequestHandler handler = new ConsumerRequestHandler();
        ConsumerRequestHandler handlerSpy = Mockito.spy(handler);

        HttpResponseMessage expectedResponseFromSignatureVerifier = mock(HttpResponseMessage.class);
        when(expectedResponseFromSignatureVerifier.getStatus()).thenReturn(HttpStatus.UNAUTHORIZED);

        doReturn(expectedResponseFromSignatureVerifier).when(handlerSpy).handleRequest(
            any(HttpRequestMessage.class), any(AccountModificationRequest.class), any(ExecutionContext.class));

        HttpResponseMessage actualResponseMessage = handlerSpy.executeDeleteByIdForOrganisations(
            mockMessage, "orgId", mockExecContext);
        handlerSpy.close();

        assertEquals(expectedResponseFromSignatureVerifier.getStatus(), actualResponseMessage.getStatus());

    }
    
    @Test
    public void testDeleteUserByIdReturns401FromVerificationFailure() {
        ConsumerRequestHandler handler = new ConsumerRequestHandler();
        ConsumerRequestHandler handlerSpy = Mockito.spy(handler);

        HttpResponseMessage expectedResponseFromSignatureVerifier = mock(HttpResponseMessage.class);
        when(expectedResponseFromSignatureVerifier.getStatus()).thenReturn(HttpStatus.UNAUTHORIZED);

        doReturn(expectedResponseFromSignatureVerifier).when(handlerSpy).handleRequest(
            any(HttpRequestMessage.class), any(AccountModificationRequest.class), any(ExecutionContext.class));

        HttpResponseMessage actualResponseMessage = handlerSpy.executeDeleteByIdForUser(
            mockMessage, "orgId", "userId", mockExecContext);
        handlerSpy.close();

        assertEquals(expectedResponseFromSignatureVerifier.getStatus(), actualResponseMessage.getStatus());

    }

    @Test
    public void testUpdateUserPermissionsReturns401FromVerificationFailure() {
        ConsumerRequestHandler handler = new ConsumerRequestHandler();
        ConsumerRequestHandler handlerSpy = Mockito.spy(handler);

        HttpResponseMessage expectedResponseFromSignatureVerifier = mock(HttpResponseMessage.class);
        when(expectedResponseFromSignatureVerifier.getStatus()).thenReturn(HttpStatus.UNAUTHORIZED);

        AccountModificationRequest mockAccountRequest = mock(AccountModificationRequest.class);
        Optional<AccountModificationRequest> mockAccountRequestOptional = Optional.of(mockAccountRequest);
        when(mockMessage.getBody()).thenReturn(mockAccountRequestOptional);

        doReturn(expectedResponseFromSignatureVerifier).when(handlerSpy).handleRequest(
            any(HttpRequestMessage.class), any(AccountModificationRequest.class), any(ExecutionContext.class));

        HttpResponseMessage actualResponseMessage = handlerSpy.executeUpdateUserPermissions(
            mockMessage, "orgId", "userId", mockExecContext);
        handlerSpy.close();

        assertEquals(expectedResponseFromSignatureVerifier.getStatus(), actualResponseMessage.getStatus());

    } 
}