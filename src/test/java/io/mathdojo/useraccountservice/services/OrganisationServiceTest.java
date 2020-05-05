package io.mathdojo.useraccountservice.services;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.validation.ConstraintViolationException;

import org.junit.BeforeClass;
import org.junit.Test;

import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;

public class OrganisationServiceTest {

    private static OrganisationService organisationService;
    private String PRECONDITIONED_UNKNOWN_ORG_ID = "unknownOrganisationId";

    @BeforeClass
    public static void setUp() {
        organisationService = OrganisationServiceSingleton.getInstance();
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

        Exception exception = assertThrows(ConstraintViolationException.class,() -> {
            organisationService.createNewOrganisation(newRequest);
        });

        String exceptionMessage = exception.getMessage();
        assertTrue(exceptionMessage.contains("no") && exceptionMessage.contains("null"));        
    }

    @Test
    public void throwsExceptionIfAccountRequestWithTrueVerificationStatusForCreateNewOrganisation() {
        boolean accountVerificationStatus = true;
        String name = "bob yourunclde";
        String profileImageLink = "https://my.image.domain/super.gif";

        AccountRequest newRequest = new AccountRequest(accountVerificationStatus, name, profileImageLink);

        RuntimeException exception = assertThrows(OrganisationServiceException.class,() -> {
            organisationService.createNewOrganisation(newRequest);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("a new organisation cannot be created with a true verification status", 
        exceptionMessage);
        
    }

    @Test
    public void throwsExceptionIfDeleteForNull() {

        RuntimeException exception = assertThrows(OrganisationServiceException.class,() -> {
            organisationService.deleteOrganisationWithId(null);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("the specified organisation could not be found", exceptionMessage);

    }

    @Test
    public void throwsExceptionIfGetOrganisationWithNullOrgId() {

        RuntimeException exception = assertThrows(OrganisationServiceException.class,() -> {
            organisationService.getOrganisationById(null);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("the specified organisation could not be found", 
        exceptionMessage);
        
    }

    @Test
    public void throwsExceptionIfDeleteForPreconditionedNonExistentOrg() {

        RuntimeException exception = assertThrows(OrganisationServiceException.class,() -> {
            organisationService.deleteOrganisationWithId(PRECONDITIONED_UNKNOWN_ORG_ID);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("the specified organisation could not be found", exceptionMessage);
    }

    @Test
    public void throwsExceptionIfGetOrganisationWithPreconditionedUnknownOrgId() {

        RuntimeException exception = assertThrows(OrganisationServiceException.class,() -> {
            organisationService.getOrganisationById("unknownOrganisationId");
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("the specified organisation could not be found", 
        exceptionMessage);
        
    }

    @Test
    public void throwsNoErrorIfDeletingForValidOrgId() {
        organisationService.deleteOrganisationWithId("knownOrganisationId");        
    }

    @Test
    public void updateOrgWithIdReturnsModificationResultIfAllParamsFilledAndValid() {
        AccountRequest accountCreationRequest = new AccountRequest(false, "aName iWillChange", "https://my.custom.domain/image-i-dont-like.png");
        Organisation oldOrganisation = organisationService.createNewOrganisation(accountCreationRequest);


        String newName = "aName iWillNotChange";
        String newProfileImageLink = "https://my.custom.domain/image-i-like.png";
        AccountRequest accountModificationRequest = new AccountRequest(true, newName, newProfileImageLink);
        Organisation modificationResult = organisationService.updateOrganisationWithId(oldOrganisation.getId(), accountModificationRequest);

        assertEquals(oldOrganisation.getId(), modificationResult.getId());
        assertEquals(newName, modificationResult.getName());
        assertEquals(newProfileImageLink, modificationResult.getProfileImageLink());
    }

    // TODO #15: Add unit test coverage for partial filling of account request params on update

    @Test
    public void throwsExceptionIfUpdateForPreconditionedNonExistentOrg() {
        String newName = "aName iWillNotChange";
        String newProfileImageLink = "https://my.custom.domain/image-i-like.png";
        AccountRequest accountModificationRequest = new AccountRequest(true, newName, newProfileImageLink);

        RuntimeException exception = assertThrows(OrganisationServiceException.class,() -> {
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
}