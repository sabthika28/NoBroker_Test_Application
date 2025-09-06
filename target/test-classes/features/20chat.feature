Feature: Navigate to My Chats

  # Business purpose:
  # Verify that a logged-in user can access the "My Chats" section
  # from the profile dropdown. This ensures the dropdown navigation
  # works and that the chat page content is displayed.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page

  Scenario: Open My Chats from the profile dropdown
    When the user opens My Chats from the dropdown
    Then the My Chats page should be displayed
