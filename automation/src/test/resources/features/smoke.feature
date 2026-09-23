Feature: QMS API smoke
Scenario: Products endpoint
 Given the API is running
 When I request products
 Then the response is successful
