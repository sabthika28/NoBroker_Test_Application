Feature: Navigate to Paperless Rental Agreement with Aadhaar E-Sign

  # Business Purpose: Enable a fully digital rental agreement using Aadhaar-based authentication.
  # Simplifies signing and reduces turnaround time by avoiding physical paperwork.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page
    When the user clicks on the top-right "Menu"
    And the user clicks on "Rental Agreement"
    Then the Rental Agreement page should be displayed

  Scenario: Navigate to "Paperless Rental Agreement with Aadhaar E-Sign" and return to Rental Agreement
    When the user clicks on "Paperless Rental Agreement with Aadhaar E-Sign"
    Then the Aadhaar E-Sign Agreement page should be displayed
