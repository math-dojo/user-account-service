package io.mathdojo.useraccountservice.model;

import io.mathdojo.useraccountservice.model.primitives.AccountType;

public class User extends AccountHolder {

    public User(String id, boolean accountVerified, String name, String profileImageLink) {
        super(id, accountVerified, name, profileImageLink, AccountType.USER);
    }

}