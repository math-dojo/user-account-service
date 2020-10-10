package io.mathdojo.useraccountservice.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.mathdojo.useraccountservice.services.SystemService;

@Configuration
public class DBConfiguration {
    public @Bean MongoClient mongoClient() {
        SystemService service = new SystemService();
        ConnectionString connectionString = new ConnectionString(service.getMongoUrl());
        return MongoClients.create(connectionString);
    }
}
