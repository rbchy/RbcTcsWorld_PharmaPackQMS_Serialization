@ui
Feature: PharmaPack QMS web UI on desktop, Windows, Android and iOS
  The same scenarios run on every target:  -Dplatform=chrome|firefox|edge|safari|windows|android|ios

  Scenario: Invalid credentials are rejected
    Given I open the PharmaPack QMS login page
    When I log in as "admin" with password "wrong-password"
    Then I see a login error

  @smoke
  Scenario: A valid user reaches the dashboard
    Given I open the PharmaPack QMS login page
    When I log in as "admin" with password "admin123"
    Then the main navigation is shown

  # DEF-05 regression: START LINE used to blank the page (lineStatus.devices undefined)
  @gmp @severity=critical
  Scenario: Operator starts and stops the packaging line from the HMI
    Given I am logged in as "admin" with password "admin123"
    And I open the Serialization screen
    When I press START LINE
    Then the PLC state shows "RUNNING"
    When I press STOP
    Then the PLC state shows "STOPPED"

  # DEF-01 UI side: Commission stays disabled until the serial has a PASS vision result
  @gmp @severity=critical
  Scenario: A new serial can be printed from the UI but not commissioned before vision
    Given I am logged in as "admin" with password "admin123"
    And I open the Serialization screen
    And I press START LINE
    And the PLC state shows "RUNNING"
    When I generate 1 serial for batch "BATCH-DEMO-001"
    Then the new serial shows status "CREATED"
    And its Commission button is disabled
    When I print the new serial
    Then the new serial shows status "PRINTED"
    And its Print button is disabled
    And its Commission button is disabled
