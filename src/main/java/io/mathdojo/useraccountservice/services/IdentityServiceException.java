package io.mathdojo.useraccountservice.services;

public class IdentityServiceException extends RuntimeException {
    /**
     * Exception that represents some error in the operations within
     * the Identity Service.
     */
    private static final long serialVersionUID = -1862566127632509952L;

    public IdentityServiceException(String message) {
		super(message);
	};
}
