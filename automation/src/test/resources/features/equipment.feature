Feature: GET equipment
 Scenario: GET equipment
  Given the QMS API is running
  When I GET "/api/master/equipment"
  Then the HTTP status is 200
  And the response body is JSON
