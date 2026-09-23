Feature: Serialization and Aggregation
 # Phase 4: POST /commission only PROVISIONS serials (status CREATED). A serial becomes
 # COMMISSIONED only after print -> vision PASS -> POST /commission/{serial}.
 Scenario: Provision unit-level serial numbers for a batch
  Given the QMS API is running
  When I POST "/api/serialization/commission" with body:
    """
    {"batchId":1,"quantity":3,"aggregationLevel":"UNIT"}
    """
  Then the HTTP status is 201
  And the response body is JSON
  And the response field "[0].aggregationLevel" equals "UNIT"
  And the response field "[0].status" equals "CREATED"

 Scenario: Commission a CASE-level serial and aggregate units under it
  Given the QMS API is running
  When I GET "/api/batches/1"
  Then the HTTP status is 200
  And I capture the response field "batchNumber" as "lot"
  And I capture the response field "expiryDate" as "expiry"
  When I POST "/api/serialization/commission" with body:
    """
    {"batchId":1,"quantity":1,"aggregationLevel":"CASE"}
    """
  Then the HTTP status is 201
  And I capture the response field "[0].serialNumber" as "caseSerial"
  When I POST "/api/serialization/commission" with body:
    """
    {"batchId":1,"quantity":2,"aggregationLevel":"UNIT"}
    """
  Then the HTTP status is 201
  And I capture the response field "[0].serialNumber" as "unitSerial1"
  And I capture the response field "[1].serialNumber" as "unitSerial2"
  When I POST "/api/serialization/print/{{unitSerial1}}" with body:
    """
    {}
    """
  Then the HTTP status is 200
  When I POST "/api/serialization/verify" with body:
    """
    {"serialNumber":"{{unitSerial1}}","actualBarcode":"{{unitSerial1}}","actualLot":"{{lot}}","actualExpiry":"{{expiry}}"}
    """
  Then the HTTP status is 200
  And the response field "decision" equals "PASS"
  When I POST "/api/serialization/commission/{{unitSerial1}}" with body:
    """
    {}
    """
  Then the HTTP status is 200
  When I POST "/api/serialization/print/{{unitSerial2}}" with body:
    """
    {}
    """
  Then the HTTP status is 200
  When I POST "/api/serialization/verify" with body:
    """
    {"serialNumber":"{{unitSerial2}}","actualBarcode":"{{unitSerial2}}","actualLot":"{{lot}}","actualExpiry":"{{expiry}}"}
    """
  Then the HTTP status is 200
  And the response field "decision" equals "PASS"
  When I POST "/api/serialization/commission/{{unitSerial2}}" with body:
    """
    {}
    """
  Then the HTTP status is 200
  # Only COMMISSIONED serials may now be aggregated (see SerializationController.aggregate) —
  # both units go through print -> vision PASS -> commission above before being packed into the case.
  When I POST "/api/serialization/aggregate" with body:
    """
    {"parentSerialNumber":"{{caseSerial}}","childSerialNumbers":["{{unitSerial1}}","{{unitSerial2}}"]}
    """
  Then the HTTP status is 200
  And the response field "aggregationLevel" equals "CASE"
  And the response field "serialNumber" equals "{{caseSerial}}"

 Scenario: Aggregating a higher-level unit under a lower-level one is rejected
  Given the QMS API is running
  When I POST "/api/serialization/commission" with body:
    """
    {"batchId":1,"quantity":1,"aggregationLevel":"PALLET"}
    """
  Then the HTTP status is 201
  And I capture the response field "[0].serialNumber" as "palletSerial"
  When I POST "/api/serialization/commission" with body:
    """
    {"batchId":1,"quantity":1,"aggregationLevel":"CASE"}
    """
  Then the HTTP status is 201
  And I capture the response field "[0].serialNumber" as "caseSerial2"
  When I POST "/api/serialization/aggregate" with body:
    """
    {"parentSerialNumber":"{{caseSerial2}}","childSerialNumbers":["{{palletSerial}}"]}
    """
  Then the HTTP status is 400

 Scenario: List serialized units for a batch
  Given the QMS API is running
  When I GET "/api/serialization/batch/1"
  Then the HTTP status is 200
  And the response body is JSON

 # GMP rule (RUNBOOK step 9): a serial may be commissioned only after a PASS vision result.
 Scenario: Commissioning a printed serial without vision verification is rejected
  Given the QMS API is running
  When I POST "/api/serialization/commission" with body:
    """
    {"batchId":1,"quantity":1,"aggregationLevel":"UNIT"}
    """
  Then the HTTP status is 201
  And I capture the response field "[0].serialNumber" as "unverifiedSerial"
  When I POST "/api/serialization/print/{{unverifiedSerial}}" with body:
    """
    {}
    """
  Then the HTTP status is 200
  When I POST "/api/serialization/commission/{{unverifiedSerial}}" with body:
    """
    {}
    """
  Then the HTTP status is 400
  When I GET "/api/serialization/{{unverifiedSerial}}"
  Then the response field "status" equals "PRINTED"
