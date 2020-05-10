package io.mathdojo.useraccountservice.model;

import java.util.Set;

import javax.validation.constraints.NotNull;

import io.mathdojo.useraccountservice.model.primitives.AccountType;
import io.mathdojo.useraccountservice.model.primitives.UserActivityHistory;
import io.mathdojo.useraccountservice.model.primitives.UserPermission;

public class User extends AccountHolder {

    @NotNull
    private String belongsToOrgWithId;

    private Set<UserPermission> permissions;

    private UserActivityHistory activityHistory;

    public User(String id, boolean accountVerified, String name, String profileImageLink) {
        super(id, accountVerified, name, profileImageLink, AccountType.USER);
    }

    //TODO Add hashcode, equals, to string
}