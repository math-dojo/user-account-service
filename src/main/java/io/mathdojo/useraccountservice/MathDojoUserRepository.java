package io.mathdojo.useraccountservice;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import io.mathdojo.useraccountservice.model.User;

@Repository
public interface MathDojoUserRepository extends MongoRepository<User, String> {

}