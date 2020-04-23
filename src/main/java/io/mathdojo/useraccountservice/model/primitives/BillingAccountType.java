package io.mathdojo.useraccountservice.model.primitives;

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
    public String toString() {
      return String.valueOf(value);
    }

    public static BillingAccountType fromValue(String text) {
      for (BillingAccountType b : BillingAccountType.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
}