---
page_type: sample
languages:
- java
products:
- azure
description: "This is a sample applicaiton to showcase the use of Spring Cloud Function on top of Azure Functions."
urlFragment: hello-spring-function-azure
---

# User Account Service

This is the user account service for the Math-Dojo platform. It is deployed as a serverless function on Azure.

## Features

This is just a "Hello, world", but it uses domain objects so it's easy to extend to do something more complex.

## Getting Started

### Prerequisites

1. Java SDK 1.8+ - The project uses maven wrapper.
2. Docker - The project requires a working mongoDb installation. By default it will look for a database on localhost:27017.

### Local Installation

- Clone the project
- Grab the mongo image from Docker: `docker pull mongo`
- Start the container on 27017: 
```ps
docker run -p 27017:27017 --name user-acc-svc-mongo -d mongo:latest
```
- Build the project and run its unit tests **for windows**
```ps
$env:SPRING_DATA_MONGODB_URI = 'mongodb://localhost:27017/test'; .\mvnw -ntp clean package
```
- Build the project and run its unit tests **for linux**
```sh
export SPRING_DATA_MONGODB_URI='mongodb://localhost:27017/test' && ./mvnw -ntp clean package
```

Note: the `src\main\azure\local.settings.json` file is not read during unit tests, this why the mongoUri needs to be passed in as a separate environment variable.

### Quickstart

Once the application is built, you can run it locally using the Azure Function Maven plug-in:

```sh
./mvnw azure-functions:run
```

❗❗❗ **IMPORTANT** ❗❗❗ - The Azure Functions Worker will take any environment vars in the current shell session into the app. These **will** take precedence over anything set in `src\main\azure\local.settings.json`.

And you can test it using a cURL command:

```sh
curl --location --request PUT 'http://localhost:7071/api/organisations/unknownOrganisationId/users/knownUserId/permissions' \
--header 'Content-Type: application/json' \
--data-raw '{
    "permissions": ["CONSUMER", "CREATOR"]
}'
```

## Deploying to Azure Function

This is done via the Azure Pipelines yaml file in the root
