Feature: Deviations
 Scenario: GET deviations
  Given the QMS API is running
  When I GET "/api/deviations"
  Then the HTTP status is 200
  And the response body is JSON

 Scenario: POST a deviation against an existing batch
  Given the QMS API is running
  When I POST "/api/deviations" with body:
    """
    {"deviationNumber":"DEV-{{ts}}","batchId":1,"title":"Automated deviation test","description":"Raised by the Cucumber suite","severity":"MINOR","openedBy":1}
    """
  Then the HTTP status is 201
  And the response body is JSON
  And the response field "status" equals "OPEN"
  And the response field "batchNumber" is present
  And the response field "id" is present
