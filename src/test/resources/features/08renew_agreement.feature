Feature: Navigate to Renew Your Agreement

  # Business Purpose: Provide customers with a quick way to extend their rental agreements online.
  # Ensures continuity of tenancy without manual paperwork or service interruptions.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page
    When the user clicks on the top-right "Menu"
    And the user clicks on "Rental Agreement"
    Then the Rental Agreement page should be displayed

  Scenario: Navigate to "Renew Your Agreement" and return to Rental Agreement
    When the user clicks on "Renew Your Agreement"
    Then the Renewal page should be displayed
