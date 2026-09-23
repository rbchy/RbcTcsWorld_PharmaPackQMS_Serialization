Feature: Authentication
 Scenario: A valid login returns a JWT and the user's roles
  When I POST "/api/auth/login" with body:
    """
    {"username":"admin","password":"admin123"}
    """
  Then the HTTP status is 200
  And the response field "token" is present
  And the response field "username" equals "admin"

 Scenario: An invalid password is rejected
  When I POST "/api/auth/login" with body:
    """
    {"username":"admin","password":"wrong-password"}
    """
  Then the HTTP status is 401

 Scenario: An unauthenticated request to a protected endpoint is rejected
  When I GET "/api/products" without authentication
  Then the HTTP status is 401
