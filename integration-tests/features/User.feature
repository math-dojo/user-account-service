Feature: Features related to User Management

  @createUserInOrg
  Scenario: POST to /users with valid body returns created user
    Given I generate a json payload called 'newUserRequest'
    When I make a POST to the function at '/organisations/validOrg/users'
    Then I should get a status code 201
    And the response should be a superset of all the keys and values set from 'newUserRequest'

  @createUserInOrg @errorHandling
  Scenario: POST to /users with invalid body returns bad request
    Given I generate a json payload called 'badNewUserRequest'
    When I make a POST to the function at '/organisations/validOrg/users'
    Then I should get a status code 400

  @getUserFromOrg
  Scenario: GET to /users/{userId} with valid userId returns user
    When I make a GET to the function at '/organisations/validOrg/users/knownUserId'
    Then I should get a status code 200
    And the response should contain a key 'id' with value 'knownUserId'

  @getUserFromOrg @errorHandling
  Scenario: GET to /users/{userId} with invalid userId returns 404
    When I make a GET to the function at '/organisations/validOrg/users/unknownUserId'
    Then I should get a status code 404

  @updateUserById
  Scenario: PUT to /users/{userId} with pre-conditioned knownUserId and valid body returns 204
    Given I generate a json payload called 'userModificationRequest'
    When I make a PUT to the function at '/organisations/knownOrgId/users/knownUserId'
    Then I should get a status code 204
    And the response should have no body

  @updateUserById @errorHandling
  Scenario: PUT to /users/{userId} with pre-conditioned knownOrgId and invalid body returns 400
    Given I generate a json payload called 'badUserModificationRequest'
    When I make a PUT to the function at '/organisations/knownOrgId/users/knownUserId'
    Then I should get a status code 400

  @updateUserById @errorHandling
  Scenario: PUT to /users/{userId} with pre-conditioned unknownOrganisationId returns 404
    Given I generate a json payload called 'userModificationRequest'
    When I make a PUT to the function at '/organisations/unknownOrganisationId/users/knownUserId'
    Then I should get a status code 404

@updateUserById @errorHandling
  Scenario: PUT to /organisations/{organisationId}/users/{userId} with pre-conditioned unknownUserId returns 404
    Given I generate a json payload called 'userModificationRequest'
    When I make a PUT to the function at '/organisations/unknownOrganisationId/users/knownUserId'
    Then I should get a status code 404