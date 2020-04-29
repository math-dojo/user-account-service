package io.mathdojo.useraccountservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;

import com.microsoft.azure.functions.ExecutionContext;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.function.adapter.azure.AzureSpringBootRequestHandler;
import org.springframework.test.context.junit4.SpringRunner;

import io.mathdojo.useraccountservice.model.DummyUser;
import io.mathdojo.useraccountservice.model.Greeting;
import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;

@RunWith(SpringRunner.class)
public class UserAccountServiceApplicationTest {

    private ExecutionContext mockExecContext;

    @Before
    public void setUp() {
        Logger testLogger = mock(Logger.class);
        mockExecContext = mock(ExecutionContext.class);
        Mockito.when(mockExecContext.getLogger()).thenReturn(testLogger);
    }

    @Test
    public void testPostForDummyUserHandler() throws Exception {
        AzureSpringBootRequestHandler<DummyUser, Greeting> handler = new AzureSpringBootRequestHandler<>(
                UserAccountServiceApplication.class);
        when(mockExecContext.getFunctionName()).thenReturn("hello");
        Greeting result = handler.handleRequest(new DummyUser("foo"), mockExecContext);
        handler.close();
        assertThat(result.getMessage()).isEqualTo("Welcome, foo");
    }

    // TODO #5: Write automated integration to check that unauthorized is returned if no signature added

    @Test
    public void testPostOrganisationsHandlerReturnsCreatedOrg() throws Exception {
        AzureSpringBootRequestHandler<AccountRequest, Organisation> handler = new AzureSpringBootRequestHandler<>(
                UserAccountServiceApplication.class);
        when(mockExecContext.getFunctionName()).thenReturn("createOrganisation");
        String profileImageLink = "https://profileImageLink";
        Organisation result = handler.handleRequest(new AccountRequest(false, "foo", profileImageLink), mockExecContext);
        handler.close();
        assertThat(result.getName()).isEqualTo("foo");
        assertThat(result.getProfileImageLink()).isEqualTo(profileImageLink);
        assertFalse(result.isAccountVerified());
    }
}
