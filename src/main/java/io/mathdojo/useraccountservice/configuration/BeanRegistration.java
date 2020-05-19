package io.mathdojo.useraccountservice.configuration;

import java.util.function.Function;

import com.microsoft.azure.functions.ExecutionContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.mathdojo.useraccountservice.model.DummyUser;
import io.mathdojo.useraccountservice.model.Greeting;
import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.User;
import io.mathdojo.useraccountservice.model.requestobjects.AccountModificationRequest;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;
import io.mathdojo.useraccountservice.services.OrganisationService;
import reactor.core.publisher.Flux;

@Configuration
public class BeanRegistration {

    @Autowired
    public OrganisationService organisationService;

    @Bean
    public Function<Flux<AccountRequest>, Flux<Organisation>> createOrganisation(ExecutionContext context) {

        return accountRequestFluxEntity -> {
            return accountRequestFluxEntity.map(accountRequest -> {
                context.getLogger().info("About to create a new org fam!!");
                return organisationService.createNewOrganisation(accountRequest);
            });
        };
    }

    @Bean
    public Function<Flux<String>, Flux<Organisation>> getOrganisationById(ExecutionContext context) {

        return organisationIdFluxEntity -> {
            return organisationIdFluxEntity.map(organisationId -> {
                context.getLogger().info("About to retrieve a known org fam!!");
                return organisationService.getOrganisationById(organisationId);
            });
        };
    }

    @Bean
    public Function<DummyUser, Greeting> hello(final ExecutionContext context) {
        return user -> {
            context.getLogger().info("yo, yo yo in the building homie!!!");
            return new Greeting("Welcome, " + user.getName(), new String[] { "I am some stuff!", "Other Stuff" });
        };
    }

    @Bean
    public Function<Flux<String>, Flux<String>> deleteOrganisationById(final ExecutionContext context) {
        return organisationIdFluxEntity -> {
            return organisationIdFluxEntity.map(orgId -> {
                context.getLogger().info("About to delete organisation: " + orgId);
                return organisationService.deleteOrganisationWithId(orgId);
            });
        };
    }

    @Bean
    public Function<Flux<AccountModificationRequest>, Flux<Organisation>> updateOrganisationById(
            ExecutionContext context) {

        return accountRequestFluxEntity -> {
            return accountRequestFluxEntity.map(accountRequest -> {
                context.getLogger()
                        .info(String.format("About to update an org with id: %s", accountRequest.getAccountId()));
                return organisationService.updateOrganisationWithId(accountRequest.getAccountId(), accountRequest);
            });
        };
    }

    @Bean
    public Function<Flux<AccountModificationRequest>, Flux<User>> createUserInOrg(ExecutionContext context) {
        return newUserRequestFluxEntity -> {
            return newUserRequestFluxEntity.map(newUserRequest -> {
                context.getLogger()
                        .info(String.format("About to create a new user in org: %s", newUserRequest.getParentOrgId()));
                return organisationService.createUserInOrg(newUserRequest.getParentOrgId(), newUserRequest);
            });
        };
    }

    @Bean
    public Function<Flux<AccountModificationRequest>, Flux<User>> getUserInOrg(ExecutionContext context) {
        return retrieveUserRequestFluxEntity -> {
            return retrieveUserRequestFluxEntity.map(retrieveUserRequest -> {
                context.getLogger().info(String.format("About to retrieve user %s from org: %s",
                        retrieveUserRequest.getAccountId(), retrieveUserRequest.getParentOrgId()));
                return organisationService.getUserInOrg(retrieveUserRequest.getParentOrgId(), retrieveUserRequest.getAccountId());
            });
        };
    }
}
