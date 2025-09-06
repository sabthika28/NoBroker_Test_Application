Feature: Navigate to Buyer Plans

  # Business Purpose: Provide buyers with premium tools and listings.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page
    When the user clicks on the top-right "Menu"

  Scenario: Navigate to "Buyer Plans" and subscribe
    When the user clicks on "Buyer Plans"
    Then the "Buyer Plans" page should be displayed
    When the user clicks on "Subscribe" in "Buyer Plans" page
    Then the payment page should be displayed
