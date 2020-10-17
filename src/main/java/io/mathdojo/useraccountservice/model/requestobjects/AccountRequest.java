package io.mathdojo.useraccountservice.model.requestobjects;

import java.util.Objects;

import javax.validation.constraints.NotEmpty;

public class AccountRequest {
    private boolean accountVerified;

    @NotEmpty
    private String name;
    
    protected String id;

    private String profileImageLink;

    public AccountRequest() {

    }

    /**
     * Creates an instance of the AccountRequest class with a null id
     * <p>
     * 
     * @param accountVerified  the status of the account being created
     * @param name             the name of the prospective account holder
     * @param profileImageLink an image of the profile user to be created
     * 
     */
    public AccountRequest(boolean accountVerified, String name, String profileImageLink) {
        this(accountVerified, name, profileImageLink, null);
    }
    /**
     * Creates an instance of the AccountRequest class
     * <p>
     * 
     * @param accountVerified  the status of the account being created
     * @param name             the name of the prospective account holder
     * @param profileImageLink an image of the profile user to be created
     * @param id  the id of the account being created
     * 
     */
    public AccountRequest(boolean accountVerified, String name,String profileImageLink, String id) {
		super();
		this.accountVerified = accountVerified;
		this.name = name;
		this.id = id;
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
    
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setProfileImageLink(String profileImageLink) {
        this.profileImageLink = profileImageLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountRequest accountRequest = (AccountRequest) o;
        return Objects.equals(this.accountVerified, accountRequest.accountVerified)
                && Objects.equals(this.name, accountRequest.name)
                && Objects.equals(this.profileImageLink, accountRequest.profileImageLink);
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
    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }

}