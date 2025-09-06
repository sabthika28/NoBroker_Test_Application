Feature: Navigate to E-Stamped Agreement

  # Business Purpose: Provide users with a digital option to generate legally stamped agreements.
  # Eliminates the need to visit physical offices, making the process faster and secure.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page
    When the user clicks on the top-right "Menu"
    And the user clicks on "Rental Agreement"
    Then the Rental Agreement page should be displayed

  Scenario: Navigate to "E-Stamped Agreement" and return to Rental Agreement
    When the user clicks on "E-Stamped Agreement"
    Then the E-Stamped Agreement form page should be displayed

