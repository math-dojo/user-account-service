package io.mathdojo.useraccountservice.model.primitives;

public enum PaymentProcessor {
    STRIPE("stripe");

    private String value;

    PaymentProcessor(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static PaymentProcessor fromValue(String text) {
      for (PaymentProcessor b : PaymentProcessor.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
}