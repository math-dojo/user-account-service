Feature: Features related to Organisation Management

  Scenario: POST to /organisations with valid body returns created org
    Given I generate a json payload called 'newOrgRequest'
    When I make a POST to the function at '/organisations'
    Then I should get a status code 201
    And the response should contain all the keys and values set from 'newOrgRequest'


