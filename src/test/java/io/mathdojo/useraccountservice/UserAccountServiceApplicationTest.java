package io.mathdojo.useraccountservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import io.mathdojo.useraccountservice.model.User;
import io.mathdojo.useraccountservice.model.requestobjects.AccountModificationRequest;
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

                Greeting result = (Greeting) handlerSpy.handleRequest(mockMessage, new DummyUser("foo"),
                                mockExecContext);
                handlerSpy.close();
                assertThat(result.getMessage()).isEqualTo("Welcome, foo");
        }

        @Test
        public void testPostOrganisationsHandlerReturnsCreatedOrg() throws Exception {
                HTTPRequestSignatureVerificationEnabledHandler<AccountRequest, Organisation> handler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                                UserAccountServiceApplication.class);
                HTTPRequestSignatureVerificationEnabledHandler<AccountRequest, Organisation> handlerSpy = Mockito
                                .spy(handler);
                Mockito.doReturn(mockSystemService).when(handlerSpy).getSystemService();

                when(mockExecContext.getFunctionName()).thenReturn("createOrganisation");
                String profileImageLink = "https://profileImageLink";
                Organisation result = (Organisation) handlerSpy.handleRequest(mockMessage,
                                new AccountRequest(false, "foo", profileImageLink), mockExecContext);
                handlerSpy.close();
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

                Organisation result = (Organisation) handlerSpy.handleRequest(mockMessage, "myCustomOrgId",
                                mockExecContext);
                handlerSpy.close();
                assertThat(result.getId()).isEqualTo("myCustomOrgId");
        }

        @Test
        public void testDeleteOrganisationsThrowsNoErrorIfSuccessful() throws Exception {
                HTTPRequestSignatureVerificationEnabledHandler<String, String> handler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                                UserAccountServiceApplication.class);
                HTTPRequestSignatureVerificationEnabledHandler<String, String> handlerSpy = Mockito.spy(handler);
                Mockito.doReturn(mockSystemService).when(handlerSpy).getSystemService();

                when(mockExecContext.getFunctionName()).thenReturn("deleteOrganisationById");
                assertDoesNotThrow(() -> {
                        handlerSpy.handleRequest(mockMessage, "myCustomOrgId", mockExecContext);
                        handlerSpy.close();
                });
        }

        @Test
        public void testUpdateOrgByIdFunctionReturnsUpdatedOrg() {
                /**
                 * Pre-req: Create an organisation to update
                 */
                HTTPRequestSignatureVerificationEnabledHandler<AccountRequest, Organisation> createOrgHandler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                                UserAccountServiceApplication.class);
                HTTPRequestSignatureVerificationEnabledHandler<AccountRequest, Organisation> createOrgHandlerSpy = Mockito
                                .spy(createOrgHandler);
                Mockito.doReturn(mockSystemService).when(createOrgHandlerSpy).getSystemService();

                when(mockExecContext.getFunctionName()).thenReturn("createOrganisation");
                String profileImageLink = "https://profileImageLink";
                Organisation oldResult = (Organisation) createOrgHandlerSpy.handleRequest(mockMessage,
                                new AccountRequest(false, "foo", profileImageLink), mockExecContext);
                createOrgHandlerSpy.close();

                /**
                 * Actual test begins
                 */

                HTTPRequestSignatureVerificationEnabledHandler<AccountRequest, Organisation> updateOrgHandler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                                UserAccountServiceApplication.class);
                HTTPRequestSignatureVerificationEnabledHandler<AccountRequest, Organisation> updateOrgHandlerSpy = Mockito
                                .spy(updateOrgHandler);
                Mockito.doReturn(mockSystemService).when(updateOrgHandlerSpy).getSystemService();
                String idOfOrgToUpdate = oldResult.getId();
                when(mockExecContext.getFunctionName()).thenReturn("updateOrganisationById");
                String newProfileImageLink = "https://profileImageLink/new.jpg";
                String newName = "a new glorious name";
                boolean newAccountVerificationStatus = true;

                Organisation result = (Organisation) updateOrgHandlerSpy.handleRequest(
                                mockMessage, new AccountModificationRequest(idOfOrgToUpdate,
                                                newAccountVerificationStatus, newName, newProfileImageLink),
                                mockExecContext);
                updateOrgHandlerSpy.close();

                assertThat(result.getId()).isEqualTo(idOfOrgToUpdate);
                assertThat(result.getName()).isEqualTo(newName);
                assertThat(result.getProfileImageLink()).isEqualTo(newProfileImageLink);
                assertThat(result.isAccountVerified()).isEqualTo(newAccountVerificationStatus);
        }

        @Test
        public void testPostCreateUserInOrgFunctionReturnsCreatedUser() throws Exception {
                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, User> handler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                                UserAccountServiceApplication.class);
                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, User> handlerSpy = Mockito
                                .spy(handler);
                Mockito.doReturn(mockSystemService).when(handlerSpy).getSystemService();

                when(mockExecContext.getFunctionName()).thenReturn("createUserInOrg");
                String profileImageLink = "https://profileImageLink";
                String parentOrgId = "customOrgId";
                String name = "my coolName";
                User result = (User) handlerSpy.handleRequest(mockMessage,
                                new AccountModificationRequest(null, parentOrgId, false,
                                                name, profileImageLink),
                                mockExecContext);
                handlerSpy.close();
                assertThat(result.getName()).isEqualTo(name);
                assertThat(result.getProfileImageLink()).isEqualTo(profileImageLink);
                assertThat(result.getBelongsToOrgWithId()).isEqualTo(parentOrgId);
                assertFalse(result.isAccountVerified());
        }
}
