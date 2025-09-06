Feature: OTP Timeout User Login

  # Business Purpose: Ensure expired OTPs are rejected and users can request a new OTP.

  Background:
    Given the user is on the login page

  Scenario: OTP expires and shows timeout
    When the user enters a valid phone number
    And the user requests an OTP
    And waits until the OTP expires
    Then an otp expired message should be shown
