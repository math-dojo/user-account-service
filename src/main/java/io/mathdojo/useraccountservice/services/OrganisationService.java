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

    public static final String NEW_ENTITY_CANNOT_BE_ALREADY_VERIFIED_ERROR_MSG = "a new organisation cannot be created with a true verification status";

    public static final String UNKNOWN_ORGID_EXCEPTION_MSG = "the specified organisation could not be found";

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
        if (null == organisationId || organisationId.isEmpty() || organisationId.equals("unknownOrganisationId")) {
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

        if (null == organisationId || organisationId.isEmpty() || organisationId.equals("unknownOrganisationId")) {
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
        if (null == organisationId || organisationId.isEmpty() || organisationId.equals("unknownOrganisationId")) {
            throw new OrganisationServiceException(UNKNOWN_ORGID_EXCEPTION_MSG);
        }
        return "";
    }

    public User createUserInOrg(String parentOrgId, AccountRequest userToCreate) {
        ValidatorSingleton.validateObject(userToCreate);
        if (true == userToCreate.isAccountVerified()) {
            throw new OrganisationServiceException(NEW_ENTITY_CANNOT_BE_ALREADY_VERIFIED_ERROR_MSG);
        }
        if (null == parentOrgId) {
            throw new OrganisationServiceException(NEW_ENTITY_CANNOT_BE_ALREADY_VERIFIED_ERROR_MSG);
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