Feature: Navigate Profile left menu

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page

  Scenario: Open Profile and navigate left-side headings
    When the user opens their profile from the username dropdown
    And the user navigates through each left-side profile heading
    Then all left-side profile sections should be reachable
