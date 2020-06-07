package io.mathdojo.useraccountservice.model;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.mathdojo.useraccountservice.model.primitives.AccountType;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;

/**
 * This class is a representation of a generic mathdojo 
 * AccountHolder
 */
public abstract class AccountHolder extends AccountRequest {

    private String id;

    private AccountType accountType;

    public AccountHolder(String id, boolean accountVerified, String name, String profileImageLink,
            AccountType accountType) {
        super(accountVerified, name, profileImageLink);
        this.id = id;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountHolder accountHolder = (AccountHolder) o;
        return Objects.equals(this.id, accountHolder.id)
                && Objects.equals(this.isAccountVerified(), accountHolder.isAccountVerified())
                && Objects.equals(this.getName(), accountHolder.getName())
                && Objects.equals(this.getProfileImageLink(), accountHolder.getProfileImageLink())
                && Objects.equals(this.accountType, accountHolder.accountType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, this.isAccountVerified(), this.getName(), this.getProfileImageLink(), accountType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountHolder {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    accountVerified: ").append(toIndentedString(this.isAccountVerified())).append("\n");
        sb.append("    name: ").append(toIndentedString(this.getName())).append("\n");
        sb.append("    profileImageLink: ").append(toIndentedString(this.getProfileImageLink())).append("\n");
        sb.append("    accountType: ").append(toIndentedString(accountType)).append("\n");
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

}
