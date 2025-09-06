Feature: Navigate to Seller Plans

  # Business Purpose: Provide sellers with plans to maximize reach.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page
    When the user clicks on the top-right "Menu"

  Scenario: Navigate to "Seller Plans" and subscribe
    When the user clicks on "Seller Plans"
    Then the "Seller Plans" page should be displayed
    When the user clicks on "Subscribe" in "Seller Plans" page
    Then the payment page should be displayed
