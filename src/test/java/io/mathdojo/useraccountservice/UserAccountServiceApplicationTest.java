package io.mathdojo.useraccountservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import io.mathdojo.useraccountservice.model.DummyUser;
import io.mathdojo.useraccountservice.model.Greeting;
import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;
import io.mathdojo.useraccountservice.security.HTTPRequestSignatureVerificationEnabledHandler;
import io.mathdojo.useraccountservice.services.SystemService;

@RunWith(SpringRunner.class)
public class UserAccountServiceApplicationTest {

    private ExecutionContext mockExecContext;
    private HttpRequestMessage mockMessage;
    private SystemService mockSystemService;

    @Before
    public void setUp() {
        Logger testLogger = mock(Logger.class);
        mockExecContext = mock(ExecutionContext.class);
        mockMessage = mock(HttpRequestMessage.class);
        mockSystemService = mock(SystemService.class);
        when(mockSystemService.getFunctionEnv()).thenReturn("local");

        Mockito.when(mockExecContext.getLogger()).thenReturn(testLogger);

    }

    @Test
    public void testPostForDummyUserHandler() throws Exception {
        HTTPRequestSignatureVerificationEnabledHandler<DummyUser, Greeting> handler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                UserAccountServiceApplication.class);
        HTTPRequestSignatureVerificationEnabledHandler<DummyUser, Greeting> handlerSpy = Mockito.spy(handler);
        Mockito.doReturn(mockSystemService).when(handlerSpy).getSystemService();

        when(mockExecContext.getFunctionName()).thenReturn("hello");

        Greeting result = (Greeting) handlerSpy.handleRequest(mockMessage, new DummyUser("foo"), mockExecContext);
        handlerSpy.close();
        assertThat(result.getMessage()).isEqualTo("Welcome, foo");
    }

    @Test
    public void testPostOrganisationsHandlerReturnsCreatedOrg() throws Exception {
        HTTPRequestSignatureVerificationEnabledHandler<AccountRequest, Organisation> handler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                UserAccountServiceApplication.class);
        HTTPRequestSignatureVerificationEnabledHandler<AccountRequest, Organisation> handlerSpy = Mockito.spy(handler);
        Mockito.doReturn(mockSystemService).when(handlerSpy).getSystemService();

        when(mockExecContext.getFunctionName()).thenReturn("createOrganisation");
        String profileImageLink = "https://profileImageLink";
        Organisation result = (Organisation) handlerSpy.handleRequest(mockMessage,
                new AccountRequest(false, "foo", profileImageLink), mockExecContext);
        handler.close();
        assertThat(result.getName()).isEqualTo("foo");
        assertThat(result.getProfileImageLink()).isEqualTo(profileImageLink);
        assertFalse(result.isAccountVerified());
    }

    @Test
    public void testGetOrganisationsByIdReturnsAnOrgIfPresent() throws Exception {
        HTTPRequestSignatureVerificationEnabledHandler<String, Organisation> handler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                UserAccountServiceApplication.class);
        HTTPRequestSignatureVerificationEnabledHandler<String, Organisation> handlerSpy = Mockito.spy(handler);
        Mockito.doReturn(mockSystemService).when(handlerSpy).getSystemService();

        when(mockExecContext.getFunctionName()).thenReturn("getOrganisationById");

        Organisation result = (Organisation) handlerSpy.handleRequest(mockMessage, "myCustomOrgId", mockExecContext);
        handler.close();
        assertThat(result.getId()).isEqualTo("myCustomOrgId");
    }
}
