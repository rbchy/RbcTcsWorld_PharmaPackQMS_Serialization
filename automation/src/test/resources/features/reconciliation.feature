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
  And the response field "batchNumber" is present
