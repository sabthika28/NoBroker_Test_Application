Feature: NoBroker Help Center Complaint Flow

  # Business Purpose: Allow customers to raise complaints related to specific services through the Help Center.  
  # Ensures that users can report issues like service quality quickly, improving customer support and satisfaction.

  Scenario: Open Help Center and raise a complaint
    When User clicks on Help Center button
    And User selects "Carpentry" service
    And User selects complaint issue "Quality"
    Then User closes the Help Center panel
