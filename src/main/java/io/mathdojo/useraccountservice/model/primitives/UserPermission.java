package io.mathdojo.useraccountservice.model.primitives;

public enum UserPermission {
    CONSUMER("consumer"), CREATOR("creator"), ORG_ADMIN("org-admin"), GLOBAL_ADMIN("global-admin");

    private String value;

    UserPermission(String value) {
        this.value = value;
    }

    public String toString() {
        return String.valueOf(value);
    }

    public static UserPermission fromValue(String text) {
        for (UserPermission b : UserPermission.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
