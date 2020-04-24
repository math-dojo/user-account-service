package io.mathdojo.useraccountservice.services;

/**
 * A singleton implementation of the Organisation Service.
 * As this is not thread safe it is recommended to only use this
 * in a Serverless Function implementation
 */
public final class OrganisationServiceSingleton {

    private static OrganisationService INSTANCE;

    public static OrganisationService getInstance() {
        if (INSTANCE == null) {
            System.out.println("Initialising Organisation Service Class");
            INSTANCE = new OrganisationService();
        }
         
        return INSTANCE;
    }

}