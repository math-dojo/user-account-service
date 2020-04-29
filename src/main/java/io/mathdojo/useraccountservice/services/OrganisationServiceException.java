package io.mathdojo.useraccountservice.services;

public class OrganisationServiceException extends RuntimeException {
    /**
     * Exception that represents some error in the operations within
     * the Organisation Service.
     */
    private static final long serialVersionUID = -1862566127632509952L;

    public OrganisationServiceException(String message) {
		super(message);
	};
}
