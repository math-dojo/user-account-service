package io.mathdojo.useraccountservice.configuration;

import java.util.function.Function;

import com.microsoft.azure.functions.ExecutionContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import io.mathdojo.useraccountservice.model.DummyUser;
import io.mathdojo.useraccountservice.model.Greeting;
import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;
import io.mathdojo.useraccountservice.services.OrganisationService;

@Configuration
public class ServiceConfig {

    @Autowired
    public OrganisationService organisationService;

    public ServiceConfig(){
        this.organisationService = new OrganisationService();
    }

    @Bean
    public Function<AccountRequest, Organisation> createOrganisation(ExecutionContext context) {
        System.out.println("Now about to create the function");
        System.out.println("Organisation Service is "+organisationService.toString());
        return accountRequest -> {
            context.getLogger().info("About to create a new org fam!!");
            /*
             * return new
             * Organisation(UUID.randomUUID().toString(),accountRequest.isAccountVerified(),
             * accountRequest.getName(), accountRequest.getProfileImageLink());
             */
            return organisationService.createNewOrganisation(accountRequest);
        };
    }

    @Bean
    public Function<DummyUser, Greeting> hello(final ExecutionContext context) {
        return user -> {
            context.getLogger().info("yo, yo yo in the building homie!!!");
            return new Greeting("Welcome, " + user.getName(), new String[]{"I am some stuff!", "Other Stuff"});
        };
    }
}
