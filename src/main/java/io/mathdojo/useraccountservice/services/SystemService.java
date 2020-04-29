package io.mathdojo.useraccountservice.services;

/**
 * Facade class responsible for reading system environment variables
 * from the function execution environment. The main purpose of this
 * class is to retrieve system environment variables prior to the 
 * start up of the spring-cloud-function.
 */
public class SystemService {

    public String getFunctionEnv() {
        String retrievedEnv = System.getenv("MATH_DOJO_ENV_NAME");
        switch ((null == retrievedEnv) ? "" : retrievedEnv) {
            case "local":
                return retrievedEnv;
            case "non-production":
                return retrievedEnv;
            case "pre-production":
                return retrievedEnv;
            case "production":
                return retrievedEnv;
            default:
                return null;
        }
    }

}