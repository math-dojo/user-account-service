Feature: Features related to User Management

  @createUserInOrg
  Scenario: POST to /organisations/{organisationId}/users with valid body returns created user
    Given I generate a json payload called 'newUserRequest'
    When I make a POST to the function at '/organisations/validOrg/users'
    Then I should get a status code 201
    And the response should be a superset of all the keys and values set from 'newUserRequest'

  @createUserInOrg @errorHandling
  Scenario: POST to /organisations/{organisationId}/users with invalid body returns bad request
    Given I generate a json payload called 'badNewUserRequest'
    When I make a POST to the function at '/organisations/validOrg/users'
    Then I should get a status code 400

  @getUserFromOrg
  Scenario: GET to /organisations/{organisationId}/users/{userId} with valid userId returns user
    When I make a GET to the function at '/organisations/validOrg/users/knownUserId'
    Then I should get a status code 200
    And the response should contain a key 'userId' with value 'knownUserId'

  @getUserFromOrg @errorHandling
  Scenario: GET to /organisations/{organisationId}/users/{userId} with invalid userId returns 404
    When I make a GET to the function at '/organisations/validOrg/users/unknownUserId'
    Then I should get a status code 404
