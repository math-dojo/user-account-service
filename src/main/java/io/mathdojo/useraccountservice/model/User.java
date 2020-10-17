package io.mathdojo.useraccountservice.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.Document;

import io.mathdojo.useraccountservice.model.primitives.AccountType;
import io.mathdojo.useraccountservice.model.primitives.UserActivityHistory;
import io.mathdojo.useraccountservice.model.primitives.UserPermission;
@Document(collection = "users")
public class User extends AccountHolder {

    @NotNull
    private String belongsToOrgWithId;

    private Set<UserPermission> permissions;

    private UserActivityHistory activityHistory;

    /***
     * Creates a user in a parent organisation
     * 
     * @param id                 the user id
     * @param accountVerified    whether the account is verfied or not
     * @param name               the user's name
     * @param profileImageLink   their profile image link
     * @param belongsToOrgWithId the id of the organisation they belong to
     */
    public User(String id, boolean accountVerified, String name, String profileImageLink, String belongsToOrgWithId) {
        super(id, accountVerified, name, profileImageLink, AccountType.USER);
        permissions = UserPermission.getDefaultPermissionSet();
        activityHistory = new UserActivityHistory();
        this.belongsToOrgWithId = belongsToOrgWithId;
    }

    public String getBelongsToOrgWithId() {
        return belongsToOrgWithId;
    }

    public void setBelongsToOrgWithId(String belongsToOrgWithId) {
        this.belongsToOrgWithId = belongsToOrgWithId;
    }

    public Set<UserPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<UserPermission> permissions) {
        this.permissions = permissions;
    }

    public UserActivityHistory getActivityHistory() {
        return activityHistory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(this.permissions, user.permissions)
                && Objects.equals(this.belongsToOrgWithId, user.belongsToOrgWithId)
                && Objects.equals(this.activityHistory, user.activityHistory) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permissions, belongsToOrgWithId, activityHistory, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class User {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    permissions: ").append(toIndentedString(permissions)).append("\n");
        sb.append("    belongsToOrgWithId: ").append(toIndentedString(belongsToOrgWithId)).append("\n");
        sb.append("    activityHistory: ").append(toIndentedString(activityHistory)).append("\n");
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