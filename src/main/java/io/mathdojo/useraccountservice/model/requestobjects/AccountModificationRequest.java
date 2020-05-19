package io.mathdojo.useraccountservice.model.requestobjects;

import java.util.Objects;

public class AccountModificationRequest extends AccountRequest {


    private String accountId;
    private String parentOrgId;

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

    public String getAccountId() {
        return accountId;
    }

    public String getParentOrgId() {
        return parentOrgId;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountModificationRequest accountModRequest = (AccountModificationRequest) o;
        return super.equals(o)
                && Objects.equals(this.accountId, accountModRequest.accountId)
                && Objects.equals(this.parentOrgId, accountModRequest.parentOrgId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, parentOrgId, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountModificationRequest {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    accountId: ").append(toIndentedString(accountId)).append("\n");
        sb.append("    parentOrgId: ").append(toIndentedString(parentOrgId)).append("\n");
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
