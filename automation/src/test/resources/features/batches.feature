Feature: GET batches
 Scenario: GET batches
  Given the QMS API is running
  When I GET "/api/batches"
  Then the HTTP status is 200
  And the response body is JSON
  And the list field "batchNumber" contains "BATCH-DEMO-001"
  And the list field "batchNumber" contains "BATCH-DEMO-002"
