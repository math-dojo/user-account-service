package io.mathdojo;

import java.util.function.Function;

import com.microsoft.azure.functions.ExecutionContext;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.mathdojo.model.Greeting;
import io.mathdojo.model.User;

@SpringBootApplication
public class HelloFunction {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(HelloFunction.class, args);
    }

    @Bean
    public Function<User, Greeting> hello(ExecutionContext context) {
        return user -> {
            context.getLogger().info("yo, yo yo in the building homie!!!");
            return new Greeting("Welcome, " + user.getName(), new String[]{"I am some stuff!", "Other Stuff"});
        };
    }
}