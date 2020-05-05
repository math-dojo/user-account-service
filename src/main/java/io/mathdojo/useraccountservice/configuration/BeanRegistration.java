package io.mathdojo.useraccountservice.configuration;

import java.util.function.Consumer;
import java.util.function.Function;

import com.microsoft.azure.functions.ExecutionContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.mathdojo.useraccountservice.model.DummyUser;
import io.mathdojo.useraccountservice.model.Greeting;
import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;
import io.mathdojo.useraccountservice.services.OrganisationServiceSingleton;

@Configuration
public class BeanRegistration {

    public BeanRegistration() {
        OrganisationServiceSingleton.getInstance();
    }    

    @Bean
    public Function<AccountRequest, Organisation> createOrganisation(ExecutionContext context) {
        
        return accountRequest -> {
            context.getLogger().info("About to create a new org fam!!");
            return OrganisationServiceSingleton.getInstance().createNewOrganisation(accountRequest);
        };
    }

    @Bean
    public Function<String, Organisation> getOrganisationById(ExecutionContext context) {
        
        return organisationId -> {
            context.getLogger().info("About to retrieve a known org fam!!");
            return OrganisationServiceSingleton.getInstance().getOrganisationById(organisationId);
        };
    }

    @Bean
    public Function<DummyUser, Greeting> hello(final ExecutionContext context) {
        return user -> {
            context.getLogger().info("yo, yo yo in the building homie!!!");
            return new Greeting("Welcome, " + user.getName(), new String[]{"I am some stuff!", "Other Stuff"});
        };
    }

    @Bean 
    public Consumer<String> deleteOrganisationById(final ExecutionContext context) {
        return organisationId -> {
            context.getLogger().info("About to delete organisation: "+organisationId);
            OrganisationServiceSingleton.getInstance().deleteOrganisationWithId(organisationId);
        };
    }

    @Bean
    public Function<AccountRequest, Organisation> updateOrganisationById(ExecutionContext context) {
        
        return accountRequest -> {
            context.getLogger().info(String.format("About to create a org with id: %s", accountRequest.getIdOfAccountToModify()));
            return OrganisationServiceSingleton.getInstance().updateOrganisationWithId(accountRequest.getIdOfAccountToModify(), accountRequest);
        };
    }
}
