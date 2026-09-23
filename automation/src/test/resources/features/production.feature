Feature: Production
 Scenario: GET production runs
  Given the QMS API is running
  When I GET "/api/production/runs"
  Then the HTTP status is 200
  And the response body is JSON

 Scenario: POST a production run and a production entry against it
  Given the QMS API is running
  When I POST "/api/production/runs" with body:
    """
    {"batchId":1,"lineId":1}
    """
  Then the HTTP status is 201
  And the response field "status" equals "IN_PROGRESS"
  And the response field "batchNumber" is present
  And the response field "lineCode" is present
  And I capture the response field "id" as "runId"
  When I POST "/api/production/entries" with body:
    """
    {"runId":{{runId}},"produced":100,"good":95,"reject":5,"operatorId":1,"remarks":"Automated production entry test"}
    """
  Then the HTTP status is 201
  And the response body is JSON
  And the response field "batchNumber" is present
  When I GET "/api/production/runs/{{runId}}/entries"
  Then the HTTP status is 200
  And the response body is JSON
