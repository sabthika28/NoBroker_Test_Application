Feature: Navigate to Upload your Draft

  # Business Purpose: Let users upload pre-drafted agreements for validation and stamping.
  # Saves time and ensures externally created drafts become legally compliant.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page
    When the user clicks on the top-right "Menu"
    And the user clicks on "Rental Agreement"
    Then the Rental Agreement page should be displayed

  Scenario: Navigate to "Upload your Draft" and return to Rental Agreement
    When the user clicks on "Upload your Draft"
    Then the Upload Draft page should be displayed

