package io.mathdojo.useraccountservice.model;

import java.util.HashMap;
import java.util.Map;

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

}