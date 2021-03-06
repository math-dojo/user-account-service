package io.mathdojo.useraccountservice.model.primitives;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

public enum UserPermission {

    @SerializedName("CONSUMER")
    CONSUMER("consumer"), 
    
    @SerializedName("CREATOR")
    CREATOR("creator"), 
    
    @SerializedName("ORG-ADMIN")
    ORG_ADMIN("org-admin"), 
    
    @SerializedName("GLOBAL-ADMIN")
    GLOBAL_ADMIN("global-admin");

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
    public static Set<UserPermission> getDefaultPermissionSet() {
    	Set<UserPermission> retVal = new HashSet<>();
    	retVal.add(CONSUMER);
    	return retVal; 
    }
}
