package io.mathdojo.useraccountservice.security;

public class HTTPRequestSignatureVerificationException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public HTTPRequestSignatureVerificationException(String errorMessage) {
        super(errorMessage);
    }

}