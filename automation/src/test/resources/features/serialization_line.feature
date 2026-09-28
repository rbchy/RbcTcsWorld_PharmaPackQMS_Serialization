Feature: Pharmaceutical packaging serialization line

  @gmp @severity=critical @regression
  Scenario: Generate, print, verify and commission a serialized carton
    Given the packaging line is RUNNING
    And a CREATED serial exists for a valid batch
    When the serial is printed
    And the vision simulator reads the correct DataMatrix, lot and expiry
    And the serial is commissioned
    Then the serial status should be COMMISSIONED
    And the audit trail records CREATED, PRINTED, VISION_VERIFIED and COMMISSIONED events

  @gmp @severity=critical @negative
  Scenario: Wrong DataMatrix is rejected
    Given the packaging line is RUNNING
    And a CREATED serial exists for a valid batch
    When the serial is printed
    And the vision simulator reads a wrong DataMatrix
    Then the vision result should be FAIL
    And the serial should not be commissioned

  # DEF-03: a camera cannot inspect a code that was never printed
  @gmp @severity=critical @negative
  Scenario: Vision verification of an unprinted serial is refused
    Given the packaging line is RUNNING
    And a CREATED serial exists for a valid batch
    When the vision simulator is asked to verify the serial without printing it
    Then the request is refused with HTTP 400
    And the serial status should be CREATED

  # DEF-04: print / vision / commission are line operations and need a RUNNING PLC
  @gmp @severity=critical @negative @stops-line
  Scenario: Printing is refused while the packaging line is stopped
    Given a CREATED serial exists for a valid batch
    And the packaging line is STOPPED
    When I try to print the serial
    Then the request is refused with HTTP 409
    And the serial status should be CREATED

  @gmp @severity=critical @negative @stops-line
  Scenario: Commissioning is refused while the packaging line is stopped
    Given the packaging line is RUNNING
    And a CREATED serial exists for a valid batch
    And the serial is printed
    And the vision simulator reads the correct DataMatrix, lot and expiry
    And the packaging line is STOPPED
    When I try to commission the serial
    Then the request is refused with HTTP 409
    And the serial status should be VISION_VERIFIED
