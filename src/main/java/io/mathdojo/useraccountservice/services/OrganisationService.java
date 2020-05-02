package io.mathdojo.useraccountservice.services;

import java.util.UUID;

import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;
import io.mathdojo.useraccountservice.model.validators.ValidatorSingleton;

public class OrganisationService {

    public String aString = "hii";

    public OrganisationService() {
    }

    public Organisation createNewOrganisation(AccountRequest request) throws OrganisationServiceException {
        ValidatorSingleton.validateObject(request);
        if (true == request.isAccountVerified()) {
            throw new OrganisationServiceException(
                    "a new organisation cannot be created with a true verification status");
        }
        return new Organisation(UUID.randomUUID().toString(), false, request.getName(), request.getProfileImageLink());
    }

    public Organisation getOrganisationById(String organisationId) throws OrganisationServiceException {
        if (null == organisationId || organisationId.isEmpty() || organisationId.equals("unknownOrganisationId")) {
            throw new OrganisationServiceException("the requested organisation could not be found");
        }
        return new Organisation(organisationId, false, UUID.randomUUID().toString(),
                "https://my.domain.com/myimage.jpg");
    }


	public void deleteOrganisationWithId(String organisationId) throws OrganisationServiceException {
        if (null == organisationId || organisationId.isEmpty() || organisationId.equals("unknownOrganisationId")) {
            throw new OrganisationServiceException("the specified organisation could not be found");
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