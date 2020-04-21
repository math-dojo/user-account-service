package io.mathdojo.useraccountservice;

import java.util.function.Function;

import com.microsoft.azure.functions.ExecutionContext;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.mathdojo.useraccountservice.model.Greeting;
import io.mathdojo.useraccountservice.model.DummyUser;

@SpringBootApplication
public class UserAccountServiceApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(UserAccountServiceApplication.class, args);
    }

    @Bean
    public Function<DummyUser, Greeting> hello(ExecutionContext context) {
        return user -> {
            context.getLogger().info("yo, yo yo in the building homie!!!");
            return new Greeting("Welcome, " + user.getName(), new String[]{"I am some stuff!", "Other Stuff"});
        };
    }
}