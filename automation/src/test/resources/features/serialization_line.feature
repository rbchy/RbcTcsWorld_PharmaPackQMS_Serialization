Feature: Pharmaceutical packaging serialization line

  Scenario: Generate, print, verify and commission a serialized carton
    Given the packaging line is RUNNING
    And a CREATED serial exists for a valid batch
    When the serial is printed
    And the vision simulator reads the correct DataMatrix, lot and expiry
    And the serial is commissioned
    Then the serial status should be COMMISSIONED

  Scenario: Wrong DataMatrix is rejected
    Given the packaging line is RUNNING
    And a CREATED serial exists for a valid batch
    When the vision simulator reads a wrong DataMatrix
    Then the vision result should be FAIL
    And the serial should not be commissioned
