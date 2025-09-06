Feature: Navigate to Owner Plans

  # Business Purpose: Help owners quickly rent/sell their property.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page
    When the user clicks on the top-right "Menu"

  Scenario: Navigate to "Owner Plans" and subscribe
    When the user clicks on "Owner Plans"
    Then the "Owner Plans" page should be displayed
    When the user clicks on "Subscribe" in "Owner Plans" page
    Then the payment page should be displayed
