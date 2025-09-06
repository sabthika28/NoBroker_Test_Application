Feature: Navigate to Your Ongoing Agreements

  # Business Purpose: Allow users to track agreements that are currently in progress.
  # Improves transparency and reduces support queries by providing real-time visibility.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page
    When the user clicks on the top-right "Menu"
    And the user clicks on "Rental Agreement"
    Then the Rental Agreement page should be displayed

  Scenario: Navigate to "Your Ongoing Agreements" and return to Rental Agreement
    When the user clicks on "Your Ongoing Agreements"
    Then the Ongoing Agreements page should be displayed

