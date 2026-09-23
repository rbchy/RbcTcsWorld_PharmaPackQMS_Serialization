Feature: QA reviews
 Scenario: GET QA reviews
  Given the QMS API is running
  When I GET "/api/qa-reviews"
  Then the HTTP status is 200
  And the response body is JSON

 Scenario: POST a QA review against an existing batch
  Given the QMS API is running
  When I POST "/api/qa-reviews" with body:
    """
    {"batchId":1,"reviewerId":1,"reviewType":"IPQC","decision":"PASS","comments":"Automated QA review test"}
    """
  Then the HTTP status is 201
  And the response body is JSON
  And the response field "decision" equals "PASS"
  And the response field "batchNumber" is present
  And the response field "productCode" is present
