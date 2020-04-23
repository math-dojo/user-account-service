package io.mathdojo.useraccountservice;

import java.util.UUID;
import java.util.function.Function;

import com.microsoft.azure.functions.ExecutionContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.mathdojo.useraccountservice.model.DummyUser;
import io.mathdojo.useraccountservice.model.Greeting;
import io.mathdojo.useraccountservice.model.Organisation;
import io.mathdojo.useraccountservice.model.requestobjects.AccountRequest;
import io.mathdojo.useraccountservice.services.OrganisationService;

@SpringBootApplication
public class UserAccountServiceApplication {

    @Autowired
    private OrganisationService organisationService;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(UserAccountServiceApplication.class, args);
    }

    @Bean
    public Function<AccountRequest, Organisation> createOrganisation(ExecutionContext context) {
        return accountRequest -> {
            context.getLogger().info("About to create a new org fam!!");
            return new Organisation(UUID.randomUUID().toString(),false, 
            "orgName", "https://somewhere.com/image.png");
        };
    }

    @Bean
    public Function<DummyUser, Greeting> hello(ExecutionContext context) {
        return user -> {
            context.getLogger().info("yo, yo yo in the building homie!!!");
            return new Greeting("Welcome, " + user.getName(), new String[]{"I am some stuff!", "Other Stuff"});
        };
    }

}