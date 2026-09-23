Feature: AQL
 Scenario: GET AQL plans
  Given the QMS API is running
  When I GET "/api/aql/plans"
  Then the HTTP status is 200
  And the response body is JSON

 Scenario: GET AQL inspections
  Given the QMS API is running
  When I GET "/api/aql/inspections"
  Then the HTTP status is 200
  And the response body is JSON

 Scenario: POST an AQL inspection within the acceptance number
  Given the QMS API is running
  When I POST "/api/aql/inspections" with body:
    """
    {"batchId":1,"planId":1,"inspectorId":1,"sampleSize":125,"defectsFound":1,"remarks":"Automated AQL inspection test"}
    """
  Then the HTTP status is 201
  And the response body is JSON
  And the response field "result" equals "ACCEPT"
  And the response field "batchNumber" is present
  And the response field "planCode" is present
