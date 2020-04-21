package io.mathdojo.useraccountservice.model.primitives;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Domain object representing a user's billing account type
 */
public enum BillingAccountType {
    CORPORATE("corporate"),
    
    EDUCATOR("educator"),
    
    FREE("free");
    
    private String value;

    BillingAccountType(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static BillingAccountType fromValue(String text) {
      for (BillingAccountType b : BillingAccountType.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
}