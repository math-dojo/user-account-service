package io.mathdojo.useraccountservice.model.primitives;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentProcessor {
    STRIPE("stripe");

    private String value;

    PaymentProcessor(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static PaymentProcessor fromValue(String text) {
      for (PaymentProcessor b : PaymentProcessor.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
}