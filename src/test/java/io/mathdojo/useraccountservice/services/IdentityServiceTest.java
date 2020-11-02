package io.mathdojo.useraccountservice.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.validation.ConstraintViolationException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.microsoft.azure.functions.ExecutionContext;

import io.mathdojo.useraccountservice.MathDojoUserRepository;
import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.User;
import io.mathdojo.useraccountservice.model.primitives.UserPermission;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;

@RunWith(MockitoJUnitRunner.class)
public class IdentityServiceTest {

    @Mock
    private ExecutionContext targetExecutionContext;

    @Mock
    private MathDojoUserRepository userRepo;

    @InjectMocks
    private IdentityService organisationService = new IdentityService();

    private String PRECONDITIONED_UNKNOWN_ORG_ID = "unknownOrganisationId";
    private String PRECONDITIONED_UNKNOWN_USER_ID = "unknownUserId";


    @Before
    public void setUp() {
        Logger testLogger = mock(Logger.class);
        Mockito.when(targetExecutionContext.getLogger()).thenReturn(testLogger);
        Mockito.when(userRepo.save(Mockito.any(User.class))).thenAnswer(new Answer<User>() {
            public User answer(InvocationOnMock invocation) {
                return (User) invocation.getArguments()[0];
            }
        });

    }

    @Test
    public void createNewOrganisationIfAccountRequestParamsValid() throws IdentityServiceException {
        boolean accountVerificationStatus = false;
        String name = "fizz buzz";
        String profileImageLink = "https://my.image.domain/super.gif";

        AccountRequest newRequest = new AccountRequest(accountVerificationStatus, name, profileImageLink, null);

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

        AccountRequest newRequest = new AccountRequest(accountVerificationStatus, name, profileImageLink, null);

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

        AccountRequest newRequest = new AccountRequest(accountVerificationStatus, name, profileImageLink, null);

        RuntimeException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.createNewOrganisation(newRequest);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("a new organisation cannot be created with a true verification status", exceptionMessage);

    }

    @Test
    public void throwsExceptionIfDeleteForNull() {

        RuntimeException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.deleteOrganisationWithId(null);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("the specified organisation could not be found", exceptionMessage);

    }

    @Test
    public void throwsExceptionIfGetOrganisationWithNullOrgId() {

        RuntimeException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.getOrganisationById(null);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("the specified organisation could not be found", exceptionMessage);

    }

    @Test
    public void throwsExceptionIfDeleteForPreconditionedNonExistentOrg() {

        RuntimeException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.deleteOrganisationWithId(PRECONDITIONED_UNKNOWN_ORG_ID);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("the specified organisation could not be found", exceptionMessage);
    }

    @Test
    public void throwsExceptionIfGetOrganisationWithPreconditionedUnknownOrgId() {

        RuntimeException exception = assertThrows(IdentityServiceException.class, () -> {
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
                "https://my.custom.domain/image-i-dont-like.png", null);
        Organisation oldOrganisation = organisationService.createNewOrganisation(accountCreationRequest);

        String newName = "aName iWillNotChange";
        String newProfileImageLink = "https://my.custom.domain/image-i-like.png";
        AccountRequest accountModificationRequest = new AccountRequest(true, newName, newProfileImageLink, null);
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
        AccountRequest accountModificationRequest = new AccountRequest(true, newName, newProfileImageLink, null);

        RuntimeException exception = assertThrows(IdentityServiceException.class, () -> {
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
        AccountRequest userToCreate = new AccountRequest(accountVerified, name, profileImageLink, null);
        User createdUser = organisationService.createUserInOrg("randomParentOrgId", userToCreate);

        assertEquals(accountVerified, createdUser.isAccountVerified());
        assertEquals(name, createdUser.getName());
        assertEquals(profileImageLink, createdUser.getProfileImageLink());
        assertNotNull(createdUser.getId());

    }
    @Test
    public void createUserInOrgRetainsIdIfSpecified() {
    	String id = "testId";
        boolean accountVerified = false;
        String name = "fizz buzz";
        String profileImageLink = "https://domain.com/cool.png";
        AccountRequest userToCreate = new AccountRequest(accountVerified, name, profileImageLink, id);
        User createdUser = organisationService.createUserInOrg("randomParentOrgId", userToCreate);
		assertEquals(id, createdUser.getId());
		assertEquals(createdUser.getPermissions().size(), 1);
		assertTrue(createdUser.getPermissions().contains(UserPermission.CONSUMER));

    }

    @Test
    public void throwErrorIfAttemptToCreateVerifiedUser() {

        boolean accountVerified = true;
        String name = "fizz buzz";
        String profileImageLink = "https://domain.com/cool.png";
        AccountRequest userToCreate = new AccountRequest(accountVerified, name, profileImageLink, null);

        IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.createUserInOrg("randomParentOrgId", userToCreate);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(IdentityService.NEW_ENTITY_CANNOT_BE_ALREADY_VERIFIED_ERROR_MSG, exceptionMessage);

    }

    @Test
    public void throwErrorIfAttemptToCreateUserWithoutSpecifiedOrg() {

        boolean accountVerified = false;
        String name = "fizz buzz";
        String profileImageLink = "https://domain.com/cool.png";
        AccountRequest userToCreate = new AccountRequest(accountVerified, name, profileImageLink, null);

        IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.createUserInOrg(null, userToCreate);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(IdentityService.ORG_LESS_NEW_USER_ERROR_MSG, exceptionMessage);

    }

    @Test
    public void throwErrorIfAttemptToCreateUserInPreconditionedNonExistentOrg() {

        boolean accountVerified = false;
        String name = "fizz buzz";
        String profileImageLink = "https://domain.com/cool.png";
        AccountRequest userToCreate = new AccountRequest(accountVerified, name, profileImageLink, null);

        IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.createUserInOrg(PRECONDITIONED_UNKNOWN_ORG_ID, userToCreate);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(IdentityService.UNKNOWN_ORGID_EXCEPTION_MSG, exceptionMessage);

    }

    @Test
    public void throwErrorIfAttemptToCreateUserWithNullName() {

        boolean accountVerified = false;
        String name = "";
        String profileImageLink = "https://domain.com/cool.png";
        AccountRequest userToCreate = new AccountRequest(accountVerified, name, profileImageLink, null);

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

        IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.getUserInOrg(expectedOrganisationId, userId);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(IdentityService.UNKNOWN_ORGID_EXCEPTION_MSG, exceptionMessage);

    }

    @Test
    public void throwsExceptionIfAttemptToRetrieveUnknownUserFromKnownOrg() {

        String expectedOrganisationId = "aKnownOrg";
        String userId = PRECONDITIONED_UNKNOWN_USER_ID;

        IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.getUserInOrg(expectedOrganisationId, userId);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(IdentityService.UNKNOWN_USERID_EXCEPTION_MSG, exceptionMessage);

    }

    @Test
    public void updateUserWithIdReturnsResultIfOrgAndAllParamsFilledAndValid() {
        String orgId = "knownOrg";

        AccountRequest accountCreationRequest = new AccountRequest(false, "aName iWillChange",
                "https://my.custom.domain/image-i-dont-like.png", null);
        User oldUser = organisationService.createUserInOrg(orgId, accountCreationRequest);

        String newName = "aName iWillNotChange";
        String newProfileImageLink = "https://my.custom.domain/image-i-like.png";
        AccountRequest accountModificationRequest = new AccountRequest(true, newName, newProfileImageLink, null);

        User modifiedUser = organisationService.updateUserWithId(orgId, oldUser.getId(),
                    accountModificationRequest);

        assertEquals(oldUser.getId(), modifiedUser.getId());
        assertEquals(newName, modifiedUser.getName());
        assertEquals(newProfileImageLink, modifiedUser.getProfileImageLink());
        
    }

    // TODO: #18 Add unit test coverage for partial filling of user account modification params

    @Ignore
    public void updateUserWithIdUpdatesOnlyNonNullFields() {
        String orgId = "knownOrg";

        AccountRequest accountCreationRequest = new AccountRequest(false, "aName iWillChange",
                "https://my.custom.domain/image-i-dont-like.png", null);
        User oldUser = organisationService.createUserInOrg(orgId, accountCreationRequest);

        String newName = null;
        String newProfileImageLink = "https://my.custom.domain/image-i-like.png";
        AccountRequest accountModificationRequest = new AccountRequest(true, newName, newProfileImageLink, null);

        User modifiedUser = organisationService.updateUserWithId(orgId, oldUser.getId(),
                    accountModificationRequest);

        assertEquals(oldUser.getId(), modifiedUser.getId());
        assertEquals(oldUser.getName(), modifiedUser.getName());
        assertEquals(newProfileImageLink, modifiedUser.getProfileImageLink());

        String secondNewName = "newerName";
        String secondNewProfileImageLink = null;
        AccountRequest secondAccountModificationRequest = new AccountRequest(true, secondNewName, secondNewProfileImageLink, null);

        User secondModifiedUser = organisationService.updateUserWithId(orgId, modifiedUser.getId(),
            secondAccountModificationRequest);

        assertEquals(modifiedUser.getId(), secondModifiedUser.getId());
        assertEquals(secondNewName, secondModifiedUser.getName());
        assertEquals(modifiedUser.getProfileImageLink(), secondModifiedUser.getProfileImageLink());
        

    }

    @Test
    public void throwsErrorIfAttemptToUpdateUserInNonExistentOrg() {
        String newName = "aName iWillNotChange";
        String newProfileImageLink = "https://my.custom.domain/image-i-like.png";
        AccountRequest accountModificationRequest = new AccountRequest(true, newName, newProfileImageLink, null);

        IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.updateUserWithId(PRECONDITIONED_UNKNOWN_ORG_ID, "knownUserId",
                    accountModificationRequest);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(IdentityService.UNKNOWN_ORGID_EXCEPTION_MSG, exceptionMessage);
    }

    @Test
    public void throwsErrorIfAttemptToUpdateNonExistentUserInValidOrg() {
        String newName = "aName iWillNotChange";
        String newProfileImageLink = "https://my.custom.domain/image-i-like.png";
        AccountRequest accountModificationRequest = new AccountRequest(true, newName, newProfileImageLink, null);

        IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.updateUserWithId("knownOrg", PRECONDITIONED_UNKNOWN_USER_ID,
                    accountModificationRequest);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(IdentityService.UNKNOWN_USERID_EXCEPTION_MSG, exceptionMessage);
    }

    @Test
    public void throwsErrorIfAttemptToUpdateValidUserWithNullParams() {
        AccountRequest accountModificationRequest = new AccountRequest(false, null, null, null);

        IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.updateUserWithId("knownOrg", "knownUserId",
                    accountModificationRequest);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("One or more of the properties to update for the user are incorrect.", exceptionMessage);
    }

    @Test
    public void throwsNoErrorIfDeletingForValidUserInValidOrgId() {
        organisationService.deleteUserFromOrg("knownOrganisationId", "knownUserId");
    }

    @Test
    public void throwsErrorIfDeletingUserForInvalidOrgId() {
        IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.deleteUserFromOrg(PRECONDITIONED_UNKNOWN_ORG_ID, "knownUserId");
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(IdentityService.UNKNOWN_ORGID_EXCEPTION_MSG, exceptionMessage);
    }

    @Test
    public void throwsErrorIfDeletingNonExistentUserId() {
        IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.deleteUserFromOrg("knownOrgId", PRECONDITIONED_UNKNOWN_USER_ID);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(IdentityService.UNKNOWN_USERID_EXCEPTION_MSG, exceptionMessage);
    }

    @Test
    public void throwsNoErrorIfSettingValidPermissionsForValidUserInValidOrgId() {
        Set<UserPermission> permissionsToSet = new HashSet<UserPermission>();
            permissionsToSet.add(UserPermission.CONSUMER);
            permissionsToSet.add(UserPermission.CREATOR);
            permissionsToSet.add(UserPermission.ORG_ADMIN);
        User modifiedUser = organisationService.updateUserPermissions("knownOrganisationId", "knownUserId",
            permissionsToSet);

        Set<UserPermission> modifiedUserPermissions = modifiedUser.getPermissions();

        assertEquals(permissionsToSet, modifiedUserPermissions);
    }

    @Test
    public void throwsErrorIfAnyPermissionInvalid() {
        Set<UserPermission> permissions = new HashSet<UserPermission>();
            permissions.add(UserPermission.CONSUMER);
            permissions.add(UserPermission.CREATOR);
            permissions.add(null);

        IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.updateUserPermissions("knownOrg", "knownUserId",
                permissions);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("One or more of the permissions to update for the user are incorrect.", exceptionMessage);
    }

    @Test
    public void denySettingOfGlobalAdminPermissionAlongWithAllOthers() {
       Set<UserPermission> permissions = new HashSet<UserPermission>();
            permissions.add(UserPermission.CONSUMER);
            permissions.add(UserPermission.CREATOR);
            permissions.add(UserPermission.GLOBAL_ADMIN);

        IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.updateUserPermissions("knownOrg", "knownUserId",
                permissions);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("A user can only hold global-admin privileges exclusive of all others.", exceptionMessage);
    }

    @Test
    public void throwsErrorIfEmptySetOfPermissionsSuppliedForUpdate() {
        Set<UserPermission> permissions = new HashSet<UserPermission>();

        IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.updateUserPermissions("knownOrg", "knownUserId",
                permissions);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("One or more of the permissions to update for the user are incorrect.", exceptionMessage);
    }

    @Test
    public void throwsErrorIfSettingPermissionsForInvalidOrgId() {
        Set<UserPermission> permissions = new HashSet<UserPermission>();
            permissions.add(UserPermission.CONSUMER);
            
        IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.updateUserPermissions(PRECONDITIONED_UNKNOWN_ORG_ID, "knownUserId",
                permissions);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(IdentityService.UNKNOWN_ORGID_EXCEPTION_MSG, exceptionMessage);
    }

    @Test
    public void throwsErrorIfSettingPermissionsOnNonExistentUserId() {
        Set<UserPermission> permissions = new HashSet<UserPermission>();
            permissions.add(UserPermission.CONSUMER);

        IdentityServiceException exception = assertThrows(IdentityServiceException.class, () -> {
            organisationService.updateUserPermissions("knownOrgId", PRECONDITIONED_UNKNOWN_USER_ID,
                permissions);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals(IdentityService.UNKNOWN_USERID_EXCEPTION_MSG, exceptionMessage);
    }
    
}