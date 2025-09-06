Feature: Navigate to Blog and Post Free Property Ad

  # Business Purpose: Allow users to explore blog articles and post a free property ad from the blog page.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page
    When the user clicks on the top-right "Menu"

  Scenario: Navigate to Blog and Post Free Property Ad, then return home
    When the user clicks on "Blog" in the menu
    Then the Blog page should be displayed
    When the user clicks on "Post Free Property Ad" on Blog page
    Then the Post Free Property Ad page should be displayed
    When the user returns to home from Blog
    Then the home page should be displayed
