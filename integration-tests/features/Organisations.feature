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

  @getOrganisationsById
  Scenario: GET to /organisations with pre-conditioned knownOrgId returns a known org
    When I make a GET to the function at '/organisations/knownOrgId'
    Then I should get a status code 200

  @getOrganisationsById @errorHandling
  Scenario: GET to /organisations with pre-conditioned unknownOrganisationId returns 404
    When I make a GET to the function at '/organisations/unknownOrganisationId'
    Then I should get a status code 404

  @deleteOrganisationsById
  Scenario: DELETE to /organisations with pre-conditioned knownOrgId returns a known org
    When I make a DELETE to the function at '/organisations/knownOrgId'
    Then I should get a status code 204

  @deleteOrganisationsById @errorHandling
  Scenario: DELETE to /organisations with pre-conditioned unknownOrganisationId returns 404
    When I make a DELETE to the function at '/organisations/unknownOrganisationId'
    Then I should get a status code 404

  @updateOrganisationsById
  Scenario: PUT to /organisations/{someOrgId} with valid body returns 204
    Given I generate a json payload called 'orgModificationRequest'
    When I make a PUT to the function at '/organisations/knownOrgId'
    Then I should get a status code 204
    And the response should have no body
