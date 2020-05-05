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
    public void throwsExceptionIfGetOrganisationWithNullOrgId() {

        RuntimeException exception = assertThrows(OrganisationServiceException.class,() -> {
            organisationService.getOrganisationById(null);
        });

        String exceptionMessage = exception.getMessage();
        assertEquals("the requested organisation could not be found", 
        exceptionMessage);
        
    }

    @Test
    public void returnsOrgWithMatchingIdIfPossibleToFind() {

        String expectedOrganisationId = "aKnownOrg";
        Organisation foundOrg = organisationService.getOrganisationById(expectedOrganisationId);

        
        assertEquals(expectedOrganisationId, foundOrg.getId());
        
    }
}