Feature: Navigate to Careers

  # Business purpose:
  # Provide a deterministic smoke test that validates users can reach the
  # NoBroker Careers landing page and view content by scrolling.
  # This avoids fragile external auth flows while still verifying that
  # the Careers content is reachable.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page
    When the user clicks on the top-right "Menu"
    And the user clicks on "Careers"
    Then the Career page should be displayed

  Scenario: Scroll Careers page to view "How did we Start"
    When the user scrolls down to view the Careers content
    Then the How did we Start section should be visible
