package io.mathdojo.useraccountservice;

import static org.junit.Assert.assertEquals;
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;

public class AccountRequestBodyOrganisationsHandlerTest {
    private ExecutionContext mockExecContext;

    @Before
    public void setup() {
        Logger testLogger = mock(Logger.class);
        mockExecContext = mock(ExecutionContext.class);

        Mockito.when(mockExecContext.getLogger()).thenReturn(testLogger);
    }

    @Test
    public void testPostToCreateOrgReturns401FromVerificationFailure() {
        AccountRequestBodyOrganisationsHandler handler = new AccountRequestBodyOrganisationsHandler();
        AccountRequestBodyOrganisationsHandler handlerSpy = Mockito.spy(handler);

        HttpRequestMessage<Optional<AccountRequest>> mockMessage = (HttpRequestMessage<Optional<AccountRequest>>) mock(HttpRequestMessage.class);
        AccountRequest mockAccountRequest = mock(AccountRequest.class);
        Optional<AccountRequest> mockAccountRequestOptional = Optional.of(mockAccountRequest);

        when(mockMessage.getBody()).thenReturn(mockAccountRequestOptional);

        HttpResponseMessage expectedResponseFromSignatureVerifier = mock(HttpResponseMessage.class);
        when(expectedResponseFromSignatureVerifier.getStatus()).thenReturn(HttpStatus.UNAUTHORIZED);

        doReturn(expectedResponseFromSignatureVerifier).when(handlerSpy).handleRequest(
            any(HttpRequestMessage.class), any(AccountRequest.class), any(ExecutionContext.class));

        HttpResponseMessage actualResponseMessage = handlerSpy.executePostForOrganisations(
            mockMessage, mockExecContext);

        assertEquals(expectedResponseFromSignatureVerifier.getStatus(), actualResponseMessage.getStatus());

    }    

    @Test
    public void testPutToCreateOrgReturns401FromVerificationFailure() {
        AccountRequestBodyOrganisationsHandler handler = new AccountRequestBodyOrganisationsHandler();
        AccountRequestBodyOrganisationsHandler handlerSpy = Mockito.spy(handler);

        HttpRequestMessage<Optional<AccountRequest>> mockMessage = (HttpRequestMessage<Optional<AccountRequest>>) mock(HttpRequestMessage.class);
        AccountRequest mockAccountRequest = mock(AccountRequest.class);
        Optional<AccountRequest> mockAccountRequestOptional = Optional.of(mockAccountRequest);

        when(mockMessage.getBody()).thenReturn(mockAccountRequestOptional);

        HttpResponseMessage expectedResponseFromSignatureVerifier = mock(HttpResponseMessage.class);
        when(expectedResponseFromSignatureVerifier.getStatus()).thenReturn(HttpStatus.UNAUTHORIZED);

        doReturn(expectedResponseFromSignatureVerifier).when(handlerSpy).handleRequest(
            any(HttpRequestMessage.class), any(AccountRequest.class), any(ExecutionContext.class));

        HttpResponseMessage actualResponseMessage = handlerSpy.executePutForOrganisations(
            mockMessage, "orgId", mockExecContext);

        assertEquals(expectedResponseFromSignatureVerifier.getStatus(), actualResponseMessage.getStatus());

    }    
}