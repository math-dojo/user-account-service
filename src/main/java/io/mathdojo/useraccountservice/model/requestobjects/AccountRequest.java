package io.mathdojo.useraccountservice.model.requestobjects;

import java.util.Objects;

import javax.validation.constraints.NotNull;

public class AccountRequest {
    private boolean accountVerified;

    @NotNull
    private String name;

    private String profileImageLink;

    private String idOfAccountToModify;

    public AccountRequest() {

    }

    /**
     * Creates an instance of the AccountRequest class
     * <p>
     * 
     * @param accountVerified  the status of the account being created
     * @param name             the name of the prospective account holder
     * @param profileImageLink an image of the profile user to be created
     * 
     */
    public AccountRequest(boolean accountVerified, String name, String profileImageLink) {
        this.accountVerified = accountVerified;
        this.name = name;
        this.profileImageLink = profileImageLink;
    }

    /**
     * Creates an instance of the AccountRequest class
     * <p>
     * 
     * @param accountId        the id of the account to be modified
     * @param accountVerified  the status of the account being created
     * @param name             the name of the prospective account holder
     * @param profileImageLink an image of the profile user to be created
     * 
     */
    public AccountRequest(String accountId, boolean accountVerified, String name, String profileImageLink) {
        this.idOfAccountToModify = accountId;
        this.accountVerified = accountVerified;
        this.name = name;
        this.profileImageLink = profileImageLink;
    }

    public boolean isAccountVerified() {
        return accountVerified;
    }

    public void setAccountVerified(boolean accountVerified) {
        this.accountVerified = accountVerified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageLink() {
        return profileImageLink;
    }

    public void setProfileImageLink(String profileImageLink) {
        this.profileImageLink = profileImageLink;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountRequest accountHolder = (AccountRequest) o;
        return Objects.equals(this.accountVerified, accountHolder.accountVerified)
                && Objects.equals(this.name, accountHolder.name)
                && Objects.equals(this.profileImageLink, accountHolder.profileImageLink)
                && Objects.equals(this.idOfAccountToModify, accountHolder.idOfAccountToModify);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountVerified, name, profileImageLink, idOfAccountToModify);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountRequest {\n");
        sb.append("    accountVerified: ").append(toIndentedString(accountVerified)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    profileImageLink: ").append(toIndentedString(profileImageLink)).append("\n");
        sb.append("    idOfAccountToModify: ").append(toIndentedString(idOfAccountToModify)).append("\n");
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

    public String getIdOfAccountToModify() {
        return idOfAccountToModify;
    }

    public void setIdOfAccountToModify(String idOfAccountToModify) {
        this.idOfAccountToModify = idOfAccountToModify;
    }

}