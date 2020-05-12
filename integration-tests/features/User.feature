Feature: Features related to User Management

  @createOrganisations
  Scenario: POST to /organisations/{organisationId}/users with valid body returns created user
    Given I generate a json payload called 'newUserRequest'
    When I make a POST to the function at '/organisations/validOrg/users'
    Then I should get a status code 201
    And the response should be a superset of all the keys and values set from 'newUserRequest'

  @createOrganisations @errorHandling
  Scenario: POST to /organisations/{organisationId}/users with invalid body returns bad request
    Given I generate a json payload called 'badNewUserRequest'
    When I make a POST to the function at '//organisations/validOrg/users'
    Then I should get a status code 400
