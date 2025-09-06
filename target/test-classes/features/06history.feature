Feature: History - Search history

  # Business Purpose: Provide customers with easy access to their previous property searches.  
  # Ensures convenience and saves time by allowing users to revisit earlier searches without re-entering details.

  Scenario: View previous searches after new search
    Given the user is on the NoBroker home page
    When the user performs a search for "Navalur, Chennai"
    And the user returns to the home page
    Then the previous search "Navalur, Chennai" should appear in search history
