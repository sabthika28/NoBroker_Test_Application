Feature: Invalid OTP User Login

  # Business Purpose: Prevent unauthorized access by validating OTP during login.  
  # Ensures platform security by displaying an error message when users enter an incorrect OTP.

  Scenario: Login with invalid OTP
    Given the user is on the login page
    When the user enters a valid phone number
    And the user enters the invalid OTP
    Then an invalid otp message should be shown
