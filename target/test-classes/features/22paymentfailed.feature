Feature: Seller Plans subscribe and cancel flow

  # Business Purpose: Validate Seller Plan subscription and cancellation flow
  # including handling of failed payment message.

  Background:
    Given the user is logged in
    And the user is on the NoBroker home page
    When the user clicks on the top-right Menu

  Scenario: Subscribe to Seller Plan then cancel payment
    When the user clicks on Seller Plans
    Then the Seller Plans page should be displayed
    When the user clicks on Subscribe on Seller Plans page
    Then the Seller Plans payment page should be displayed
    When the user clicks on Back from payment page
    Then the Cancel Payment option should be displayed
    When the user clicks on Yes to cancel payment
    Then the payment failed message should be displayed on Seller Plans page
