Feature: Invalid Number User Login

  # Business Purpose: Ensure only valid registered phone numbers are accepted during login.  
  # Protects the platform from unauthorized access and enhances data security by showing an error for invalid numbers.

  Scenario: Login with invalid mobile number
    Given the user is on the login page
    When the user enters a invalid phone number
    Then an invalid number message should be shown
