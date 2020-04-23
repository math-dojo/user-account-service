package io.mathdojo.useraccountservice.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;

@Service
public class OrganisationService {

    public String aString = "hii";

    public Organisation createNewOrganisation(AccountRequest request) {
        return new Organisation(UUID.randomUUID().toString(),false, 
            "orgName", "https://somewhere.com/image.png");
    }
}