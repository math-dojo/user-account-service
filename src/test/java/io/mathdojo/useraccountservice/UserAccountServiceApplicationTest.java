package io.mathdojo.useraccountservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import io.mathdojo.useraccountservice.services.IdentityService;
import io.mathdojo.useraccountservice.services.IdentityServiceException;
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
        public void testPostForDummyUserHandler() {
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
        public void testPostOrganisationsHandlerReturnsCreatedOrg() {
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
        public void testGetOrganisationsByIdReturnsAnOrgIfPresent() {
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
        public void testDeleteOrganisationThrowsNoErrorIfSuccessful() {
                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, String> handler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                                UserAccountServiceApplication.class);
                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, String> handlerSpy = Mockito.spy(handler);
                Mockito.doReturn(mockSystemService).when(handlerSpy).getSystemService();

                when(mockExecContext.getFunctionName()).thenReturn("deleteOrganisationById");

                AccountModificationRequest accountDeletionRequest = new AccountModificationRequest("knownOrganisationId",
                        null,false, null, null);
                assertDoesNotThrow(() -> {
                        handlerSpy.handleRequest(mockMessage, accountDeletionRequest, mockExecContext);
                        handlerSpy.close();
                });
        }

        @Test
        public void testDeleteOrganisationThrowsOrgServiceExceptionIfUnsuccessful() {
                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, String> handler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                                UserAccountServiceApplication.class);
                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, String> handlerSpy = Mockito.spy(handler);
                Mockito.doReturn(mockSystemService).when(handlerSpy).getSystemService();
               AccountModificationRequest accountDeletionRequest = new AccountModificationRequest("unknownOrganisationId",
                        null,false, null, null);
                when(mockExecContext.getFunctionName()).thenReturn("deleteOrganisationById");
                IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
                        handlerSpy.handleRequest(mockMessage, accountDeletionRequest, mockExecContext);
                        handlerSpy.close();
                });

                assertThat(exception.getMessage()).isEqualTo(IdentityService.UNKNOWN_ORGID_EXCEPTION_MSG);

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
        public void testPostCreateUserInOrgFunctionReturnsCreatedUser() {
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
                                new AccountModificationRequest(null, parentOrgId, false, name, profileImageLink),
                                mockExecContext);
                handlerSpy.close();
                assertThat(result.getName()).isEqualTo(name);
                assertThat(result.getProfileImageLink()).isEqualTo(profileImageLink);
                assertThat(result.getBelongsToOrgWithId()).isEqualTo(parentOrgId);
                assertFalse(result.isAccountVerified());
        }

        @Test
        public void testGetUserInOrgFunctionReturnsKnownUser() {
                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, User> handler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                                UserAccountServiceApplication.class);
                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, User> handlerSpy = Mockito
                                .spy(handler);
                Mockito.doReturn(mockSystemService).when(handlerSpy).getSystemService();

                when(mockExecContext.getFunctionName()).thenReturn("getUserInOrg");
                String parentOrgId = "customOrgId";
                String userId = "my coolName";
                User result = (User) handlerSpy.handleRequest(mockMessage,
                                new AccountModificationRequest(userId, parentOrgId, false, null, null),
                                mockExecContext);
                handlerSpy.close();
                assertThat(result.getId()).isEqualTo(userId);
                assertThat(result.getBelongsToOrgWithId()).isEqualTo(parentOrgId);
        }

        @Test
        public void testUpdateUserInOrgFunctionReturnsKnownUser() {
                /**
                 * Pre-req: Create a user to update
                 */

                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, User> createUserHandler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                                UserAccountServiceApplication.class);
                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, User> createUserHandlerSpy = Mockito
                                .spy(createUserHandler);
                Mockito.doReturn(mockSystemService).when(createUserHandlerSpy).getSystemService();

                when(mockExecContext.getFunctionName()).thenReturn("createUserInOrg");
                String parentOrgId = "customOrgId";
                String name = "My Name";
                String imageLink = "https://superdomain.com/cool.img";
                User result = (User) createUserHandlerSpy.handleRequest(mockMessage,
                                new AccountModificationRequest(null, parentOrgId, false, name, imageLink),
                                mockExecContext);
                createUserHandlerSpy.close();
                assertThat(result.getBelongsToOrgWithId()).isEqualTo(parentOrgId);

                /**
                 * Actual test begins
                 */
                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, User> updateUserHandler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                                UserAccountServiceApplication.class);
                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, User> updateUserHandlerSpy = Mockito
                                .spy(updateUserHandler);
                Mockito.doReturn(mockSystemService).when(updateUserHandlerSpy).getSystemService();

                when(mockExecContext.getFunctionName()).thenReturn("updateUserInOrg");

                String newName = "aName iWillNotChange";
                String newProfileImageLink = "https://my.custom.domain/image-i-like.png";
                boolean newAccountVerificationStatus = true;
                User modifiedResult = (User) updateUserHandlerSpy.handleRequest(mockMessage,
                                new AccountModificationRequest(result.getId(), result.getBelongsToOrgWithId(),
                                                newAccountVerificationStatus, newName, newProfileImageLink),
                                mockExecContext);

                assertThat(modifiedResult.getId()).isEqualTo(result.getId());
                assertThat(modifiedResult.getBelongsToOrgWithId()).isEqualTo(result.getBelongsToOrgWithId());
                assertThat(modifiedResult.isAccountVerified()).isEqualTo(newAccountVerificationStatus);
                assertThat(modifiedResult.getName()).isEqualTo(newName);
                assertThat(modifiedResult.getProfileImageLink()).isEqualTo(newProfileImageLink);

        }

        @Test
        public void testDeleteUserThrowsNoErrorIfSuccessful() {
                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, String> handler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                                UserAccountServiceApplication.class);
                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, String> handlerSpy = Mockito.spy(handler);
                Mockito.doReturn(mockSystemService).when(handlerSpy).getSystemService();
                AccountModificationRequest accountDeletionRequest = new AccountModificationRequest("someUserId",
                        "someOrgId",false, null, null);
                when(mockExecContext.getFunctionName()).thenReturn("deleteUserFromOrg");
                assertDoesNotThrow(() -> {
                        handlerSpy.handleRequest(mockMessage, accountDeletionRequest, mockExecContext);
                        handlerSpy.close();
                });
        }

        @Test
        public void testDeleteThrowsOrgServiceExceptionIfUnsuccessful() {
                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, String> handler = new HTTPRequestSignatureVerificationEnabledHandler<>(
                                UserAccountServiceApplication.class);
                HTTPRequestSignatureVerificationEnabledHandler<AccountModificationRequest, String> handlerSpy = Mockito.spy(handler);
                Mockito.doReturn(mockSystemService).when(handlerSpy).getSystemService();
                AccountModificationRequest accountDeletionRequest = new AccountModificationRequest("someUserId",
                        "unknownOrganisationId",false, null, null);
                when(mockExecContext.getFunctionName()).thenReturn("deleteUserFromOrg");
                IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
                        handlerSpy.handleRequest(mockMessage, accountDeletionRequest, mockExecContext);
                        handlerSpy.close();
                });

                assertThat(exception.getMessage()).isEqualTo(IdentityService.UNKNOWN_ORGID_EXCEPTION_MSG);

        }
}
