Feature: GET products
 Scenario: GET products
  Given the QMS API is running
  When I GET "/api/products"
  Then the HTTP status is 200
  And the response body is JSON
