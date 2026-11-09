Feature: Reconciliations
 Scenario: GET reconciliations
  Given the QMS API is running
  When I GET "/api/reconciliations"
  Then the HTTP status is 200
  And the response body is JSON

 Scenario: POST a balanced reconciliation for an existing batch
  Given the QMS API is running
  When I POST "/api/reconciliations" with body:
    """
    {"batchId":2,"startingQuantity":100,"goodQuantity":80,"rejectQuantity":10,"unusedQuantity":10,"calculatedBy":1,"remarks":"Automated reconciliation test"}
    """
  Then the HTTP status is 201
  And the response body is JSON
  And the response field "status" equals "RECONCILED"
  And the response field "variance" equals the number "0"
  And the response field "reconciledQuantity" equals the number "100"
  And the response field "batchNumber" is present

 Scenario: A quantity that does not add up requires an investigation
  Given the QMS API is running
  When I POST "/api/batches" with body:
    """
    {"batchNumber":"BATCH-RECON-{{ts}}","productId":1,"lotSize":500,"manufacturingDate":"2026-01-01","expiryDate":"2027-01-01"}
    """
  Then the HTTP status is 201
  And I capture the response field "id" as "reconBatchId"
  When I POST "/api/reconciliations" with body:
    """
    {"batchId":{{reconBatchId}},"startingQuantity":500,"goodQuantity":480,"rejectQuantity":12,"unusedQuantity":5,"calculatedBy":1,"remarks":"3 units unaccounted for"}
    """
  Then the HTTP status is 201
  And the response field "status" equals "INVESTIGATION_REQUIRED"
  And the response field "variance" equals the number "3"
  And the response field "reconciledQuantity" equals the number "497"
