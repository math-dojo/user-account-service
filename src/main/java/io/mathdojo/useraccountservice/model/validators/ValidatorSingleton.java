package io.mathdojo.useraccountservice.model.validators;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class ValidatorSingleton {
    private static Validator INSTANCE;

    private static Validator getInstance() {
        if (INSTANCE == null) {
            System.out.println("Initialising Validator");
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            INSTANCE = factory.getValidator();
             
        }
         
        return INSTANCE;
    }

    public static <T> void validateObject(Object o) {
        
        Set<ConstraintViolation<Object>> violations = getInstance().validate(o);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
