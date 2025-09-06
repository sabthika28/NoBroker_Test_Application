Feature: Navigate to Payments History

  # Business Purpose: Allow users to manage rent payments and view their payment history.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page

  Scenario: Navigate to Pay Rent and view Payments History
    When the user clicks on Pay Rent
    Then the Payments via Credit Card page should be displayed
    When the user clicks on My Payments
    Then the Payments History page should be displayed
