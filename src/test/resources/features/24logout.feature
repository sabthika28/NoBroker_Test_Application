Feature: User Sign Out from Profile Dropdown

  # Business Purpose: Allow logged-in users to safely log out of NoBroker.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page

  Scenario: User logs out from the profile dropdown
    When the user clicks on the top-right "Profile" icon
    And the user clicks on "Sign Out" from the dropdown
    Then the login menu should be displayed
