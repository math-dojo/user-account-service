Feature: Features related to Organisation Management

  @createOrganisations
  Scenario: POST to /organisations with valid body returns created org
    Given I generate a json payload called 'newOrgRequest'
    When I make a POST to the function at '/organisations'
    Then I should get a status code 201
    And the response should be a superset of all the keys and values set from 'newOrgRequest'

  @createOrganisations @errorHandling
  Scenario: POST to /organisations with invalid body returns bad request
    Given I generate a json payload called 'badNewOrgRequest'
    When I make a POST to the function at '/organisations'
    Then I should get a status code 400
