package io.mathdojo.useraccountservice.model.primitives;

import java.util.Objects;

/**
 * Domain Object representing a Math Dojo organisation's billing details
 */
public class OrganisationBillingDetails {

    private PaymentProcessor paymentProcessor;

    private String idOnPaymentProcessor;

    private BillingAccountType billingAccountType;

    public OrganisationBillingDetails(PaymentProcessor paymentProcessor, String idOnPaymentProcessor,
            BillingAccountType billingAccountType) {
        this.paymentProcessor = paymentProcessor;
        this.idOnPaymentProcessor = idOnPaymentProcessor;
        this.billingAccountType = billingAccountType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OrganisationBillingDetails {\n");

        sb.append("    billingAccountType: ").append(toIndentedString(billingAccountType)).append("\n");
        sb.append("    paymentProcessor: ").append(toIndentedString(paymentProcessor)).append("\n");
        sb.append("    idOnPaymentProcessor: ").append(toIndentedString(idOnPaymentProcessor)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    @Override
    public int hashCode() {
        return Objects.hash(billingAccountType, paymentProcessor, idOnPaymentProcessor);
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrganisationBillingDetails organisationBillingDetails = (OrganisationBillingDetails) o;
        return Objects.equals(this.billingAccountType, organisationBillingDetails.billingAccountType)
                && Objects.equals(this.paymentProcessor, organisationBillingDetails.paymentProcessor)
                && Objects.equals(this.idOnPaymentProcessor, organisationBillingDetails.idOnPaymentProcessor);
    }

    public PaymentProcessor getPaymentProcessor() {
        return paymentProcessor;
    }

    public void setPaymentProcessor(PaymentProcessor paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }

    public String getIdOnPaymentProcessor() {
        return idOnPaymentProcessor;
    }

    public void setIdOnPaymentProcessor(String idOnPaymentProcessor) {
        this.idOnPaymentProcessor = idOnPaymentProcessor;
    }

    public BillingAccountType getBillingAccountType() {
        return billingAccountType;
    }

    public void setBillingAccountType(BillingAccountType billingAccountType) {
        this.billingAccountType = billingAccountType;
    }
}
