package io.mathdojo.useraccountservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class BodyLessOrganisationsRequestHandlerTest {
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
    public void testGetOrgByIdReturns401FromVerificationFailure() {
        BodyLessOrganisationsRequestHandler handler = new BodyLessOrganisationsRequestHandler();
        BodyLessOrganisationsRequestHandler handlerSpy = Mockito.spy(handler);

        HttpResponseMessage expectedResponseFromSignatureVerifier = mock(HttpResponseMessage.class);
        when(expectedResponseFromSignatureVerifier.getStatus()).thenReturn(HttpStatus.UNAUTHORIZED);

        doReturn(expectedResponseFromSignatureVerifier).when(handlerSpy).handleRequest(
            any(HttpRequestMessage.class), anyString(), any(ExecutionContext.class));

        HttpResponseMessage actualResponseMessage = handlerSpy.executeGetByIdForOrganisations(
            mockMessage, "orgId", mockExecContext);
        handlerSpy.close();

        assertEquals(expectedResponseFromSignatureVerifier.getStatus(), actualResponseMessage.getStatus());

    }
}