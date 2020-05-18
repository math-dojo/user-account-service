package io.mathdojo.useraccountservice.services;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.logging.Logger;

import javax.validation.ConstraintViolationException;

import com.microsoft.azure.functions.ExecutionContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.User;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationServiceTest {

    @Mock
    private ExecutionContext targetExecutionContext;

    @InjectMocks
    private OrganisationService organisationService = new OrganisationService();

    private String PRECONDITIONED_UNKNOWN_ORG_ID = "unknownOrganisationId";
    private String PRECONDITIONED_UNKNOWN_USER_ID = "unknownUserId";

    @Before
    public void setUp() {
        Logger testLogger = mock(Logger.class);
        Mockito.when(targetExecutionContext.getLogger()).thenReturn(testLogger);
    }

    @Test
    public void createNewOrganisationIfAccountRequestParamsValid() throws OrganisationServiceException {
        boolean accountVerificationStatus = false;
        String name = "fizz buzz";
        String profileImageLink = "https://my.image.domain/super.gif";

        AccountRequest newRequest = new AccountRequest(accountVerificationStatus, name, profileImageLink);

        Organisation testOrg = organisationService.createNewOrganisation(newRequest);

        assertEquals(name, testOrg.getName());
        assertEquals(profileImageLink, testOrg.getProfileImageLink());
        assertEquals(accountVerificationStatus, testOrg.isAccountVerified());

    }

    @Test
    public void throwsExceptionIfAccountRequestWithNullNameForCreateNewOrganisation() {
        boolean accountVerificationStatus = false;
        String name = null;
        String profileImageLink = "https://my.image.domain/super.gif";

        AccountRequest newRequest = new AccountRequest(accountVerificationStatus, name, profileImageLink);

        Exception exception = assertThrows(ConstraintViolationException.class, () -> {
            organisationService.createNewOrganisation(newRequest);
        });

        String exceptionMessage = exception.getMessage();
        assertTrue(exceptionMessage.contains("name"));
    }

    @Test
    public void throwsExceptionIfAccountRequestWithTrueVerificationStatusForCreateNewOrganisation() {
        boolean accountVerificationStatus = true;
        String name = "bob yourunclde";
        String profileImageLink = "https://my.image.domain/super.gif";

        AccountRequest newRequest = new AccountRequest(accountVerificationStatus, name, profileImageLink);

        RuntimeException exception = assertThrows(OrganisationServiceException.class, () -> {
            organisationService.createNewOrganisation(newRequest);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("a new organisation cannot be created with a true verification status", exceptionMessage);

    }

    @Test
    public void throwsExceptionIfDeleteForNull() {

        RuntimeException exception = assertThrows(OrganisationServiceException.class, () -> {
            organisationService.deleteOrganisationWithId(null);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("the specified organisation could not be found", exceptionMessage);

    }

    @Test
    public void throwsExceptionIfGetOrganisationWithNullOrgId() {

        RuntimeException exception = assertThrows(OrganisationServiceException.class, () -> {
            organisationService.getOrganisationById(null);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("the specified organisation could not be found", exceptionMessage);

    }

    @Test
    public void throwsExceptionIfDeleteForPreconditionedNonExistentOrg() {

        RuntimeException exception = assertThrows(OrganisationServiceException.class, () -> {
            organisationService.deleteOrganisationWithId(PRECONDITIONED_UNKNOWN_ORG_ID);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("the specified organisation could not be found", exceptionMessage);
    }

    @Test
    public void throwsExceptionIfGetOrganisationWithPreconditionedUnknownOrgId() {

        RuntimeException exception = assertThrows(OrganisationServiceException.class, () -> {
            organisationService.getOrganisationById("unknownOrganisationId");
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("the specified organisation could not be found", exceptionMessage);

    }

    @Test
    public void throwsNoErrorIfDeletingForValidOrgId() {
        organisationService.deleteOrganisationWithId("knownOrganisationId");
    }

    @Test
    public void updateOrgWithIdReturnsModificationResultIfAllParamsFilledAndValid() {
        AccountRequest accountCreationRequest = new AccountRequest(false, "aName iWillChange",
                "https://my.custom.domain/image-i-dont-like.png");
        Organisation oldOrganisation = organisationService.createNewOrganisation(accountCreationRequest);

        String newName = "aName iWillNotChange";
        String newProfileImageLink = "https://my.custom.domain/image-i-like.png";
        AccountRequest accountModificationRequest = new AccountRequest(true, newName, newProfileImageLink);
        Organisation modificationResult = organisationService.updateOrganisationWithId(oldOrganisation.getId(),
                accountModificationRequest);

        assertEquals(oldOrganisation.getId(), modificationResult.getId());
        assertEquals(newName, modificationResult.getName());
        assertEquals(newProfileImageLink, modificationResult.getProfileImageLink());
    }

    // TODO #15: Add unit test coverage for partial filling of account request
    // params on update

    @Test
    public void throwsExceptionIfUpdateForPreconditionedNonExistentOrg() {
        String newName = "aName iWillNotChange";
        String newProfileImageLink = "https://my.custom.domain/image-i-like.png";
        AccountRequest accountModificationRequest = new AccountRequest(true, newName, newProfileImageLink);

        RuntimeException exception = assertThrows(OrganisationServiceException.class, () -> {
            organisationService.updateOrganisationWithId(PRECONDITIONED_UNKNOWN_ORG_ID, accountModificationRequest);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("the specified organisation could not be found", exceptionMessage);

    }

    @Test
    public void returnsOrgWithMatchingIdIfPossibleToFind() {

        String expectedOrganisationId = "aKnownOrg";
        Organisation foundOrg = organisationService.getOrganisationById(expectedOrganisationId);

        assertEquals(expectedOrganisationId, foundOrg.getId());

    }

    @Test
    public void createUserInOrgReturnsNewUserIfSuccessful() {

        boolean accountVerified = false;
        String name = "fizz buzz";
        String profileImageLink = "https://domain.com/cool.png";
        AccountRequest userToCreate = new AccountRequest(accountVerified, name, profileImageLink);
        User createdUser = organisationService.createUserInOrg("randomParentOrgId", userToCreate);

        assertEquals(accountVerified, createdUser.isAccountVerified());
        assertEquals(name, createdUser.getName());
        assertEquals(profileImageLink, createdUser.getProfileImageLink());

    }

    @Test
    public void throwErrorIfAttemptToCreateVerifiedUser() {

        boolean accountVerified = true;
        String name = "fizz buzz";
        String profileImageLink = "https://domain.com/cool.png";
        AccountRequest userToCreate = new AccountRequest(accountVerified, name, profileImageLink);

        OrganisationServiceException exception = assertThrows(OrganisationServiceException.class, () -> {
            organisationService.createUserInOrg("randomParentOrgId", userToCreate);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(OrganisationService.NEW_ENTITY_CANNOT_BE_ALREADY_VERIFIED_ERROR_MSG, exceptionMessage);

    }

    @Test
    public void throwErrorIfAttemptToCreateUserWithoutSpecifiedOrg() {

        boolean accountVerified = false;
        String name = "fizz buzz";
        String profileImageLink = "https://domain.com/cool.png";
        AccountRequest userToCreate = new AccountRequest(accountVerified, name, profileImageLink);

        OrganisationServiceException exception = assertThrows(OrganisationServiceException.class, () -> {
            organisationService.createUserInOrg(null, userToCreate);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(OrganisationService.ORG_LESS_NEW_USER_ERROR_MSG, exceptionMessage);

    }

    @Test
    public void throwErrorIfAttemptToCreateUserInPreconditionedNonExistentOrg() {

        boolean accountVerified = false;
        String name = "fizz buzz";
        String profileImageLink = "https://domain.com/cool.png";
        AccountRequest userToCreate = new AccountRequest(accountVerified, name, profileImageLink);

        OrganisationServiceException exception = assertThrows(OrganisationServiceException.class, () -> {
            organisationService.createUserInOrg(PRECONDITIONED_UNKNOWN_ORG_ID, userToCreate);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(OrganisationService.UNKNOWN_ORGID_EXCEPTION_MSG, exceptionMessage);

    }

    @Test
    public void throwErrorIfAttemptToCreateUserWithNullName() {

        boolean accountVerified = false;
        String name = "";
        String profileImageLink = "https://domain.com/cool.png";
        AccountRequest userToCreate = new AccountRequest(accountVerified, name, profileImageLink);

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            organisationService.createUserInOrg("someKnownOrgId", userToCreate);
        });

        String exceptionMessage = exception.getMessage();
        assertTrue(exceptionMessage.contains("name"));

    }

    @Test
    public void returnsUserWithMatchingIdIfPossibleToFindInOrg() {

        String expectedOrganisationId = "aKnownOrg";
        String userId = "knownUserId";

        User createdUser = organisationService.getUserInOrg(expectedOrganisationId, userId);

        assertEquals(expectedOrganisationId, createdUser.getBelongsToOrgWithId());

    }

    @Test
    public void throwsExceptionIfAttemptToRetrieveUserFromUnknownOrg() {

        String expectedOrganisationId = PRECONDITIONED_UNKNOWN_ORG_ID;
        String userId = "knownUserId";

        OrganisationServiceException exception = assertThrows(OrganisationServiceException.class, () -> {
            organisationService.getUserInOrg(expectedOrganisationId, userId);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(OrganisationService.UNKNOWN_ORGID_EXCEPTION_MSG, exceptionMessage);

    }

    @Test
    public void throwsExceptionIfAttemptToRetrieveUnknownUserFromKnownOrg() {

        String expectedOrganisationId = "aKnownOrg";
        String userId = PRECONDITIONED_UNKNOWN_USER_ID;

        OrganisationServiceException exception = assertThrows(OrganisationServiceException.class, () -> {
            organisationService.getUserInOrg(expectedOrganisationId, userId);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(OrganisationService.UNKNOWN_USERID_EXCEPTION_MSG, exceptionMessage);

    }

    @Test
    public void updateUserWithIdReturnsResultIfOrgAndAllParamsFilledAndValid() {

    }

    @Test
    public void throwsErrorIfAttemptToUpdateUserInNonExistentOrg() {
        String newName = "aName iWillNotChange";
        String newProfileImageLink = "https://my.custom.domain/image-i-like.png";
        AccountRequest accountModificationRequest = new AccountRequest(true, newName, newProfileImageLink);

        OrganisationServiceException exception = assertThrows(OrganisationServiceException.class, () -> {
            organisationService.updateUserWithId(PRECONDITIONED_UNKNOWN_ORG_ID, "knownUserId",
                    accountModificationRequest);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(OrganisationService.UNKNOWN_ORGID_EXCEPTION_MSG, exceptionMessage);
    }

    @Test
    public void throwsErrorIfAttemptToUpdateNonExistentUserInValidOrg() {
        String newName = "aName iWillNotChange";
        String newProfileImageLink = "https://my.custom.domain/image-i-like.png";
        AccountRequest accountModificationRequest = new AccountRequest(true, newName, newProfileImageLink);

        OrganisationServiceException exception = assertThrows(OrganisationServiceException.class, () -> {
            organisationService.updateUserWithId("knownOrg", PRECONDITIONED_UNKNOWN_USER_ID,
                    accountModificationRequest);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(OrganisationService.UNKNOWN_USERID_EXCEPTION_MSG, exceptionMessage);
    }

    @Test
    public void throwsErrorIfAttemptToUpdateValidUserWithInvalidParams() {

    }
}