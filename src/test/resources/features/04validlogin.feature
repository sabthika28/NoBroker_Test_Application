Feature: Valid User Login

  # Business Purpose: Allow customers to securely log in using valid credentials.  
  # Ensures authorized access to the platform, enabling users to manage their activities and services seamlessly.

  Scenario: Login with valid credentials
    Given the user is on the login page
    When the user enters a valid phone number
    And the user enters the valid OTP
    Then the user should be logged in successfully
