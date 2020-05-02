package io.mathdojo.useraccountservice;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ConsumerRequestHandlerTest {
    private ExecutionContext mockExecContext;
    private HttpRequestMessage mockMessage;

    @Before
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
            any(HttpRequestMessage.class), anyString(), any(ExecutionContext.class));

        HttpResponseMessage actualResponseMessage = handlerSpy.executeDeleteByIdForOrganisations(
            mockMessage, "orgId", mockExecContext);

        assertEquals(expectedResponseFromSignatureVerifier.getStatus(), actualResponseMessage.getStatus());

    }    
}