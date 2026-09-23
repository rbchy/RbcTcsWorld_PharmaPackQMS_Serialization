Feature: GET materials
 Scenario: GET materials
  Given the QMS API is running
  When I GET "/api/master/materials"
  Then the HTTP status is 200
  And the response body is JSON
