package io.mathdojo.useraccountservice.model.requestobjects;

import java.util.Objects;

import io.mathdojo.useraccountservice.model.primitives.UserPermission;

public class AccountModificationRequest extends AccountRequest {

 
    private String parentOrgId;
    private UserPermission[] permissions;
    
    

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
        super(accountVerified, name, profileImageLink, accountId);
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
        super(accountVerified, name, profileImageLink, accountId);
        this.parentOrgId = parentOrgId;
    }

    public AccountModificationRequest(Builder builder) {
        super(builder.accountVerified, builder.name, builder.profileImageLink, builder.accountId);
        this.parentOrgId = builder.parentOrgId;
        this.permissions = builder.userPermissions;
    }


    public String getParentOrgId() {
        return parentOrgId;
    }

    public UserPermission[] getUserPermissions() {
        return permissions;
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
                && Objects.equals(this.parentOrgId, accountModRequest.parentOrgId)
                && Objects.equals(this.permissions, accountModRequest.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentOrgId, permissions, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountModificationRequest {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    parentOrgId: ").append(toIndentedString(parentOrgId)).append("\n");
        sb.append("    userPermissions: ").append(toIndentedString(permissions)).append("\n");
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
        private UserPermission[] userPermissions;
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
            this.userPermissions = new UserPermission[1];
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

        public Builder withUserPermissions(UserPermission[] permissions) {
            this.userPermissions = permissions;
            return this;
        }

        public AccountModificationRequest build() {
            return new AccountModificationRequest(this);
        }
    }
}
