package io.mathdojo.useraccountservice.model.requestobjects;

import java.util.Objects;

public class AccountRequest {
    private boolean accountVerified;

    private String name;

    private String profileImageLink;

    public AccountRequest(boolean accountVerified, String name, String profileImageLink) {
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
                && Objects.equals(this.profileImageLink, accountHolder.profileImageLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountVerified, name, profileImageLink);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountRequest {\n");
        sb.append("    accountVerified: ").append(toIndentedString(accountVerified)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    profileImageLink: ").append(toIndentedString(profileImageLink)).append("\n");
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