Feature: CAPA
 Scenario: GET CAPA
  Given the QMS API is running
  When I GET "/api/capa"
  Then the HTTP status is 200
  And the response body is JSON

 Scenario: POST a CAPA action against a freshly created deviation
  Given the QMS API is running
  When I POST "/api/deviations" with body:
    """
    {"deviationNumber":"DEV-CAPA-{{ts}}","batchId":1,"title":"Deviation for CAPA chain test","description":"Raised by the Cucumber suite","severity":"MAJOR","openedBy":1}
    """
  Then the HTTP status is 201
  And I capture the response field "id" as "deviationId"
  When I POST "/api/capa" with body:
    """
    {"deviationId":{{deviationId}},"actionType":"CORRECTIVE","actionDescription":"Automated CAPA test action","ownerId":1,"dueDate":"2026-12-31"}
    """
  Then the HTTP status is 201
  And the response body is JSON
  And the response field "status" equals "OPEN"
  And the response field "deviationNumber" is present
  And the response field "batchNumber" is present
