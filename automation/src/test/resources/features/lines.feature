Feature: GET lines
 Scenario: GET lines
  Given the QMS API is running
  When I GET "/api/master/lines"
  Then the HTTP status is 200
  And the response body is JSON
  And the list field "lineCode" contains "LINE-01"
  And the list field "lineCode" contains "LINE-02"
