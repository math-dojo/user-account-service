package io.mathdojo.useraccountservice.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.mathdojo.useraccountservice.model.primitives.AccountType;
import io.mathdojo.useraccountservice.model.primitives.OrganisationBillingDetails;

public class Organisation extends AccountHolder {

    private Map<String, User> adminUsers;

    private OrganisationBillingDetails billingDetails;

    public Organisation(String id, boolean accountVerified, String name, String profileImageLink) {
        super(id, accountVerified, name, profileImageLink, AccountType.ORGANISATION);
    }

    public Map<String, User> getAdminUsers() {
        return adminUsers;
    }

    public void setAdminUsers(Map<String, User> adminUsers) {
        this.adminUsers = adminUsers;
    }

    public void addAdminUser(User adminUserToBeAddedd) {
        if(this.adminUsers == null) {
            this.adminUsers = new HashMap<String, User>();
        }
        this.adminUsers.put(adminUserToBeAddedd.getName(), adminUserToBeAddedd);
    }

    public OrganisationBillingDetails getBillingDetails() {
        return billingDetails;
    }

    public void setBillingDetails(OrganisationBillingDetails billingDetails) {
        this.billingDetails = billingDetails;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Organisation {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    adminUsers: ").append(toIndentedString(printMapProperties(adminUsers))).append("\n");
        sb.append("    billingDetails: ").append(toIndentedString(billingDetails)).append("\n");
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

    private String printMapProperties(Map<String, ?> map) {
        String mapAsString = map.keySet().stream().map(key -> key + "=" + map.get(key))
                .collect(Collectors.joining(", ", "{", "}"));
        return mapAsString;
    }
}