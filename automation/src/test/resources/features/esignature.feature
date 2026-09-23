Feature: Electronic Signatures
 Scenario: Sign a QA decision record
  Given the QMS API is running
  When I POST "/api/esignatures" with body:
    """
    {"entityName":"QAReview","entityId":"1","actionType":"APPROVE","signatureReason":"Automated Cucumber test signature"}
    """
  Then the HTTP status is 201
  And the response body is JSON
  And the response field "username" equals "admin"
  And the response field "entityName" equals "QAReview"
  And the response field "actionType" equals "APPROVE"

 Scenario: A signature requires entityName, entityId and actionType
  Given the QMS API is running
  When I POST "/api/esignatures" with body:
    """
    {"signatureReason":"Missing the required fields"}
    """
  Then the HTTP status is 400

 Scenario: List signatures recorded for an entity
  Given the QMS API is running
  When I GET "/api/esignatures/entity/QAReview/1"
  Then the HTTP status is 200
  And the response body is JSON

 Scenario: List all signatures
  Given the QMS API is running
  When I GET "/api/esignatures"
  Then the HTTP status is 200
  And the response body is JSON
