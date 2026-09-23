Feature: Audit Trail
 Scenario: Creating a batch writes a traceable audit trail entry
  Given the QMS API is running
  When I POST "/api/batches" with body:
    """
    {"batchNumber":"BATCH-AUDIT-{{ts}}","productId":1,"lotSize":500,"manufacturingDate":"2026-01-01","expiryDate":"2027-01-01"}
    """
  Then the HTTP status is 201
  And I capture the response field "id" as "auditBatchId"
  When I GET "/api/audit-trails/entity/Batch/{{auditBatchId}}"
  Then the HTTP status is 200
  And the response body is JSON
  And the response field "[0].actionType" equals "CREATE"
  And the response field "[0].entityName" equals "Batch"
  And the response field "[0].username" equals "admin"

 Scenario: Recording a deviation writes its own audit trail entry
  Given the QMS API is running
  When I POST "/api/deviations" with body:
    """
    {"deviationNumber":"DEV-AUDIT-{{ts}}","batchId":1,"title":"Deviation for audit trail test","description":"Raised by the Cucumber suite","severity":"MINOR","openedBy":1}
    """
  Then the HTTP status is 201
  And I capture the response field "id" as "auditDeviationId"
  When I GET "/api/audit-trails/entity/Deviation/{{auditDeviationId}}"
  Then the HTTP status is 200
  And the response field "[0].actionType" equals "CREATE"

 Scenario: Listing all audit trail entries
  Given the QMS API is running
  When I GET "/api/audit-trails"
  Then the HTTP status is 200
  And the response body is JSON
