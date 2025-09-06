Feature: Navigate to Tenant Plans

  # Business Purpose: Allow tenants to view and purchase plans that suit their rental needs.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page
    When the user clicks on the top-right "Menu"

  Scenario: Navigate to "Tenant Plans" and subscribe
    When the user clicks on "Tenant Plans"
    Then the "Tenant Plans" page should be displayed
    When the user clicks on "Subscribe" in "Tenant Plans" page
    Then the payment page should be displayed
