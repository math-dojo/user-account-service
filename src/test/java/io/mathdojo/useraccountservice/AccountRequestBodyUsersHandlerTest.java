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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import io.mathdojo.useraccountservice.model.requestobjects.AccountModificationRequest;

public class AccountRequestBodyUsersHandlerTest {
    private ExecutionContext mockExecContext;

    @BeforeEach
    public void setup() {
        Logger testLogger = mock(Logger.class);
        mockExecContext = mock(ExecutionContext.class);

        Mockito.when(mockExecContext.getLogger()).thenReturn(testLogger);
    }

    @Test
    public void testPostToCreateUserReturns401FromVerificationFailure() {
        AccountRequestBodyUsersHandler handler = new AccountRequestBodyUsersHandler();
        AccountRequestBodyUsersHandler handlerSpy = Mockito.spy(handler);

        HttpRequestMessage<Optional<AccountModificationRequest>> mockMessage = (HttpRequestMessage<Optional<AccountModificationRequest>>) mock(HttpRequestMessage.class);
        AccountModificationRequest mockAccountRequest = mock(AccountModificationRequest.class);
        Optional<AccountModificationRequest> mockAccountRequestOptional = Optional.of(mockAccountRequest);

        when(mockMessage.getBody()).thenReturn(mockAccountRequestOptional);

        HttpResponseMessage expectedResponseFromSignatureVerifier = mock(HttpResponseMessage.class);
        when(expectedResponseFromSignatureVerifier.getStatus()).thenReturn(HttpStatus.UNAUTHORIZED);

        doReturn(expectedResponseFromSignatureVerifier).when(handlerSpy).handleRequest(
            any(HttpRequestMessage.class), any(AccountModificationRequest.class), any(ExecutionContext.class));

        HttpResponseMessage actualResponseMessage = handlerSpy.executePostForNewUserInOrg(
            mockMessage, 
            "someValidOrgId",
            mockExecContext
        );
        handlerSpy.close();

        assertEquals(expectedResponseFromSignatureVerifier.getStatus(), actualResponseMessage.getStatus());

    }

    @Test
    public void testGetForUserReturns401FromVerificationFailure() {
        AccountRequestBodyUsersHandler handler = new AccountRequestBodyUsersHandler();
        AccountRequestBodyUsersHandler handlerSpy = Mockito.spy(handler);

        HttpRequestMessage<Optional<AccountModificationRequest>> mockMessage = (HttpRequestMessage<Optional<AccountModificationRequest>>) mock(HttpRequestMessage.class);
        AccountModificationRequest mockAccountRequest = mock(AccountModificationRequest.class);
        Optional<AccountModificationRequest> mockAccountRequestOptional = Optional.of(mockAccountRequest);

        when(mockMessage.getBody()).thenReturn(mockAccountRequestOptional);

        HttpResponseMessage expectedResponseFromSignatureVerifier = mock(HttpResponseMessage.class);
        when(expectedResponseFromSignatureVerifier.getStatus()).thenReturn(HttpStatus.UNAUTHORIZED);

        doReturn(expectedResponseFromSignatureVerifier).when(handlerSpy).handleRequest(
            any(HttpRequestMessage.class), any(AccountModificationRequest.class), any(ExecutionContext.class));

        HttpResponseMessage actualResponseMessage = handlerSpy.executeGetForUserInOrg(
            mockMessage, 
            "someValidOrgId",
            "someValidUserId",
            mockExecContext
        );
        handlerSpy.close();

        assertEquals(expectedResponseFromSignatureVerifier.getStatus(), actualResponseMessage.getStatus());

    }

    @Test
    public void testPutToCreateUserReturns401FromVerificationFailure() {
        AccountRequestBodyUsersHandler handler = new AccountRequestBodyUsersHandler();
        AccountRequestBodyUsersHandler handlerSpy = Mockito.spy(handler);

        HttpRequestMessage<Optional<AccountModificationRequest>> mockMessage = (HttpRequestMessage<Optional<AccountModificationRequest>>) mock(HttpRequestMessage.class);
        AccountModificationRequest mockAccountRequest = mock(AccountModificationRequest.class);
        Optional<AccountModificationRequest> mockAccountRequestOptional = Optional.of(mockAccountRequest);

        when(mockMessage.getBody()).thenReturn(mockAccountRequestOptional);

        HttpResponseMessage expectedResponseFromSignatureVerifier = mock(HttpResponseMessage.class);
        when(expectedResponseFromSignatureVerifier.getStatus()).thenReturn(HttpStatus.UNAUTHORIZED);

        doReturn(expectedResponseFromSignatureVerifier).when(handlerSpy).handleRequest(
            any(HttpRequestMessage.class), any(AccountModificationRequest.class), any(ExecutionContext.class));

        HttpResponseMessage actualResponseMessage = handlerSpy.executePutUserInOrg(
            mockMessage, 
            "someValidOrgId",
            "someUserId",
            mockExecContext
        );
        handlerSpy.close();

        assertEquals(expectedResponseFromSignatureVerifier.getStatus(), actualResponseMessage.getStatus());

    }
}
