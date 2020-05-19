package io.mathdojo.useraccountservice.services;

import java.util.UUID;
import java.util.logging.Level;

import com.microsoft.azure.functions.ExecutionContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.User;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;
import io.mathdojo.useraccountservice.model.validators.ValidatorSingleton;

@Service
public class OrganisationService {

    private static final String PRECONDITIONED_UNKNOWN_USER_ID = "unknownUserId";
    private static final String PRECONDITIONED_UNKNOWN_ORGANISATION_ID = "unknownOrganisationId";
    public static final String NEW_ENTITY_CANNOT_BE_ALREADY_VERIFIED_ERROR_MSG = "a new organisation cannot be created with a true verification status";
    public static final String ORG_LESS_NEW_USER_ERROR_MSG = "a new user cannot be made without specifying a valid parent org";
    public static final String UNKNOWN_ORGID_EXCEPTION_MSG = "the specified organisation could not be found";
    public static final String UNKNOWN_USERID_EXCEPTION_MSG = "the specified user could not be found";

    @Autowired
    private ExecutionContext targetExecutionContext;

    public String aString = "hii";

    public OrganisationService() {
    }

    public Organisation createNewOrganisation(AccountRequest request) throws OrganisationServiceException {
        ValidatorSingleton.validateObject(request);
        if (true == request.isAccountVerified()) {
            throw new OrganisationServiceException(NEW_ENTITY_CANNOT_BE_ALREADY_VERIFIED_ERROR_MSG);
        }
        return new Organisation(UUID.randomUUID().toString(), false, request.getName(), request.getProfileImageLink());
    }

    public Organisation getOrganisationById(String organisationId) throws OrganisationServiceException {
        if (null == organisationId || organisationId.isEmpty() || organisationId.equals(PRECONDITIONED_UNKNOWN_ORGANISATION_ID)) {
            throw new OrganisationServiceException(UNKNOWN_ORGID_EXCEPTION_MSG);
        }
        return new Organisation(organisationId, false, UUID.randomUUID().toString(),
                "https://my.domain.com/myimage.jpg");
    }

    public Organisation updateOrganisationWithId(String organisationId, AccountRequest accountModificationRequest) {
        try {
            validateAccountModificationRequest(accountModificationRequest);
        } catch (OrganisationServiceException e) {
            targetExecutionContext.getLogger().log(Level.WARNING, e.getMessage(), e);
            throw e;
        }

        if (null == organisationId || organisationId.isEmpty() || organisationId.equals(PRECONDITIONED_UNKNOWN_ORGANISATION_ID)) {
            throw new OrganisationServiceException(UNKNOWN_ORGID_EXCEPTION_MSG);
        }

        Organisation foundOrganisation;

        try {
            foundOrganisation = getOrganisationById(organisationId);
        } catch (OrganisationServiceException e) {
            targetExecutionContext.getLogger().log(Level.WARNING, UNKNOWN_ORGID_EXCEPTION_MSG, e);
            throw e;
        }

        String nameToUpdate = (null == accountModificationRequest.getName()
                || accountModificationRequest.getName().isEmpty()) ? foundOrganisation.getName()
                        : accountModificationRequest.getName();
        String profileToUpdate = (null == accountModificationRequest.getProfileImageLink()
                || accountModificationRequest.getProfileImageLink().isEmpty()) ? foundOrganisation.getProfileImageLink()
                        : accountModificationRequest.getProfileImageLink();
        boolean verificationStatusToUpdate = (!foundOrganisation.isAccountVerified()
                && accountModificationRequest.isAccountVerified()) ? accountModificationRequest.isAccountVerified()
                        : foundOrganisation.isAccountVerified();

        return new Organisation(foundOrganisation.getId(), (verificationStatusToUpdate), nameToUpdate, profileToUpdate);
    }

    public String deleteOrganisationWithId(String organisationId) throws OrganisationServiceException {
        if (null == organisationId || organisationId.isEmpty() || organisationId.equals(PRECONDITIONED_UNKNOWN_ORGANISATION_ID)) {
            throw new OrganisationServiceException(UNKNOWN_ORGID_EXCEPTION_MSG);
        }
        return "";
    }

    public User createUserInOrg(String parentOrgId, AccountRequest userToCreate) {
        ValidatorSingleton.validateObject(userToCreate);
        if (true == userToCreate.isAccountVerified()) {
            targetExecutionContext.getLogger().log(Level.FINEST,
                    String.format("Failed attempt to create an already validated user."));
            throw new OrganisationServiceException(NEW_ENTITY_CANNOT_BE_ALREADY_VERIFIED_ERROR_MSG);
        }
        if (null == parentOrgId || null == getOrganisationById(parentOrgId)) {
            targetExecutionContext.getLogger().log(Level.FINEST,
                    String.format("Failed attempt to create a user without an org."));
            throw new OrganisationServiceException(ORG_LESS_NEW_USER_ERROR_MSG);
        }

        return new User(UUID.randomUUID().toString(), userToCreate.isAccountVerified(), userToCreate.getName(),
                userToCreate.getProfileImageLink(), parentOrgId);
    }

    private void validateAccountModificationRequest(AccountRequest request) throws OrganisationServiceException {
        if (null == request.getName() && null == request.getProfileImageLink()
                && request.isAccountVerified() == false) {
            throw new OrganisationServiceException(
                    "The name, profileImageLink and accountVerified cannot all be empty in a modification request.");
        }
    }

    public User getUserInOrg(String expectedOrganisationId, String userId) {
        String returnedOrgId = (getOrganisationById(expectedOrganisationId)).getId();
        if (PRECONDITIONED_UNKNOWN_USER_ID.equals(userId)) {
            targetExecutionContext.getLogger().log(Level.WARNING,
                    String.format("UserId %s in Org %s could not be found", userId, expectedOrganisationId));
            throw new OrganisationServiceException(UNKNOWN_USERID_EXCEPTION_MSG);
        }
        return new User(userId, false, "a name", "https://domain.com/img.png", returnedOrgId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OrganisationService {\n");

        sb.append("    aString: ").append(toIndentedString(aString)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}