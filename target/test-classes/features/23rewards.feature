Feature: Navigate to Rewards Page

  # Business Purpose: Allow users to explore rewards and benefits available in NoBroker.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page

  Scenario: Navigate to Rewards and scroll through items
    When the user clicks on the "Rewards" icon
    Then the Rewards page should be displayed
    And the user scrolls through the Rewards page
