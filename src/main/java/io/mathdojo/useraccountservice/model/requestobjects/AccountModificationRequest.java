package io.mathdojo.useraccountservice.model.requestobjects;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import io.mathdojo.useraccountservice.model.primitives.UserPermission;

public class AccountModificationRequest extends AccountRequest {

    private String accountId;
    private String parentOrgId;
    private Set<UserPermission> userPermissions;

    /**
     * Creates an instance of the AccountModificationRequest class
     * <p>
     * 
     * @param accountId        the id of the account to be modified
     * @param accountVerified  the status of the account being created
     * @param name             the name of the prospective account holder
     * @param profileImageLink an image of the profile user to be created
     * 
     */
    public AccountModificationRequest(String accountId, boolean accountVerified, String name, String profileImageLink) {
        super(accountVerified, name, profileImageLink);
        this.accountId = accountId;
    }

    /**
     * Creates an instance of the AccountModificationRequest class
     * that contains a parent organisation Id. To be used for modifications
     * to instances of {@link io.mathdojo.useraccountservice.model.User }
     * <p>
     * 
     * @param accountId        the id of the account to be modified
     * @param parentOrgId
     * @param accountVerified  the status of the account being created
     * @param name             the name of the prospective account holder
     * @param profileImageLink an image of the profile user to be created
     * 
     */
    public AccountModificationRequest(String accountId, String parentOrgId, boolean accountVerified, String name, String profileImageLink) {
        super(accountVerified, name, profileImageLink);
        this.accountId = accountId;
        this.parentOrgId = parentOrgId;
    }

    public AccountModificationRequest(Builder builder) {
        super(builder.accountVerified, builder.name, builder.profileImageLink);
        this.accountId = builder.accountId;
        this.parentOrgId = builder.parentOrgId;
        this.userPermissions = builder.userPermissions;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getParentOrgId() {
        return parentOrgId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountModificationRequest accountModRequest = (AccountModificationRequest) o;
        return super.equals(o)
                && Objects.equals(this.accountId, accountModRequest.accountId)
                && Objects.equals(this.parentOrgId, accountModRequest.parentOrgId)
                && Objects.equals(this.userPermissions, accountModRequest.userPermissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, parentOrgId, userPermissions, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountModificationRequest {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    accountId: ").append(toIndentedString(accountId)).append("\n");
        sb.append("    parentOrgId: ").append(toIndentedString(parentOrgId)).append("\n");
        sb.append("    userPermissions: ").append(toIndentedString(userPermissions)).append("\n");
        sb.append("}");
        return sb.toString();
    }
    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }    

    /**
     * A Builder for creating AccountModificationRequests
     */
    public static class Builder {
        private Set<UserPermission> userPermissions;
        private String accountId;
        private String parentOrgId;
        private boolean accountVerified;
        private String name;
        private String profileImageLink;        
    
        private Builder() {
            this.accountId = null;
            this.parentOrgId = null;
            this.accountVerified = false;
            this.name = null;
            this.profileImageLink = null;
            this.userPermissions = new HashSet<>();
        }

        /**
         * Creates a new instance of an AccountModificationRequest.Builder
         * to which further parameter modification calls can be chained.
         * @return an instance of the builder
         */
        public static Builder createBuilder() {
            return new Builder();
        }

        public Builder withAccountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder withParentOrgId(String parentOrgId) {
            this.parentOrgId = parentOrgId;
            return this;
        }

        public Builder withAccountVerifiedStatus(boolean status) {
            this.accountVerified = status;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withProfileImageLink(String imageUrlString) {
            this.profileImageLink = imageUrlString;
            return this;
        }

        public Builder withUserPermissions(Set<UserPermission> permissions) {
            this.userPermissions = permissions;
            return this;
        }

        public AccountModificationRequest build() {
            return new AccountModificationRequest(this);
        }
    }
}
