package io.mathdojo.useraccountservice.model;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import io.mathdojo.useraccountservice.model.interfaces.AccountType;

/**
 * This class is a representation of a generic mathdojo 
 * AccountHolder
 */
@Validated
public abstract class AccountHolder {
    @JsonProperty("id")
    private String id;

    @JsonProperty("accountVerified")
    private boolean accountVerified;

    @JsonProperty("name")
    private String name;

    @JsonProperty("profileImageLink")
    private String profileImageLink;

    @JsonProperty("accountType")
    private AccountType accountType;

    public AccountHolder(String id, boolean accountVerified, String name, String profileImageLink,
            AccountType accountType) {
        this.id = id;
        this.accountVerified = accountVerified;
        this.name = name;
        this.profileImageLink = profileImageLink;
        this.accountType = accountType;
    }

    /**
     * Get id
     * 
     * @return id
     **/
    @NotNull

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get accountVerified
     * 
     * @return accountVerified
     **/
    @NotNull

    public boolean isAccountVerified() {
        return accountVerified;
    }

    public void setAccountVerified(Boolean accountVerified) {
        this.accountVerified = accountVerified;
    }

    /**
     * Get name
     * 
     * @return name
     **/
    @NotNull

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get profileImageLink
     * 
     * @return profileImageLink
     **/

    public String getProfileImageLink() {
        return profileImageLink;
    }

    public void setProfileImageLink(String profileImageLink) {
        this.profileImageLink = profileImageLink;
    }

    /**
     * Get accountType
     * 
     * @return accountType
     **/
    @NotNull

    @Valid
    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountHolder accountHolder = (AccountHolder) o;
        return Objects.equals(this.id, accountHolder.id)
                && Objects.equals(this.accountVerified, accountHolder.accountVerified)
                && Objects.equals(this.name, accountHolder.name)
                && Objects.equals(this.profileImageLink, accountHolder.profileImageLink)
                && Objects.equals(this.accountType, accountHolder.accountType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountVerified, name, profileImageLink, accountType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountHolder {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    accountVerified: ").append(toIndentedString(accountVerified)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    profileImageLink: ").append(toIndentedString(profileImageLink)).append("\n");
        sb.append("    accountType: ").append(toIndentedString(accountType)).append("\n");
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
