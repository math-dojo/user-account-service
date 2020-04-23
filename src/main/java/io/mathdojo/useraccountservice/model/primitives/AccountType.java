package io.mathdojo.useraccountservice.model.primitives;

/**
 * Enum representing the types of user account holders
 */
public enum AccountType {
  USER("user"),
  ORGANISATION("organisation");

  private String value;

  AccountType(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static AccountType fromValue(String text) {
    for (AccountType b : AccountType.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
