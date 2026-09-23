package com.pharmapack.automation.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ApiSteps {

    private static final String BASE = "http://localhost:8080";

    Response r;
    final Map<String, Object> ctx = new HashMap<>();
    private String authToken;

    /**
     * The API now requires a Bearer token on every /api/** endpoint except /api/auth/login
     * (see backend SecurityConfig). Logging in as the demo "admin" account here, once per
     * scenario, keeps every existing feature file working unchanged — no .feature file needs
     * to know about auth at all; get()/postWithBody() attach the token automatically below.
     */
    @Before
    public void login() {
        Response loginResponse = RestAssured.given()
                .contentType("application/json")
                .body("{\"username\":\"admin\",\"password\":\"admin123\"}")
                .post(BASE + "/api/auth/login");
        assertEquals(200, loginResponse.statusCode(),
                "Could not log in as the demo admin user before the scenario — is the backend up and seeded? Response: " + loginResponse.getBody().asString());
        authToken = loginResponse.jsonPath().getString("token");
        assertNotNull(authToken, "Login succeeded but no token was returned");
    }

    @Given("the QMS API is running")
    public void running() { r = authed(RestAssured.given()).get(BASE + "/api/products"); }

    @Given("the API is running")
    public void apiRunning() { r = authed(RestAssured.given()).get(BASE + "/api/products"); }

    @When("I GET {string}")
    public void get(String path) { r = authed(RestAssured.given()).get(BASE + resolve(path)); }

    @When("I GET {string} without authentication")
    public void getWithoutAuth(String path) { r = RestAssured.get(BASE + resolve(path)); }

    @When("I request products")
    public void requestProducts() { r = authed(RestAssured.given()).get(BASE + "/api/products"); }

    @When("I POST {string} with body:")
    public void postWithBody(String path, String body) {
        r = authed(RestAssured.given().contentType("application/json").body(resolve(body))).post(BASE + resolve(path));
    }

    @Then("the HTTP status is {int}")
    public void status(int code) { assertEquals(code, r.statusCode(), "Response body was: " + r.getBody().asString()); }

    @Then("the response body is JSON")
    public void json() { assertTrue(r.contentType().contains("json")); }

    @Then("the response is successful")
    public void successful() { assertTrue(r.statusCode() >= 200 && r.statusCode() < 300, "Expected a 2xx response but got " + r.statusCode()); }

    @Then("the response field {string} equals {string}")
    public void fieldEquals(String field, String expected) {
        Object v = r.jsonPath().get(field);
        assertEquals(resolve(expected), String.valueOf(v), "Field '" + field + "' — response body was: " + r.getBody().asString());
    }

    @Then("the response field {string} is present")
    public void fieldPresent(String field) {
        assertNotNull(r.jsonPath().get(field), "Expected field '" + field + "' to be present — response body was: " + r.getBody().asString());
    }

    @Then("I capture the response field {string} as {string}")
    public void capture(String field, String name) {
        Object v = r.jsonPath().get(field);
        assertNotNull(v, "Cannot capture missing field '" + field + "' — response body was: " + r.getBody().asString());
        ctx.put(name, v);
    }

    /** Replaces {{ts}} with the current epoch millis (for unique values) and {{name}} with any previously captured field. */
    private String resolve(String body) {
        String out = body.replace("{{ts}}", String.valueOf(System.currentTimeMillis()));
        for (Map.Entry<String, Object> e : ctx.entrySet()) {
            out = out.replace("{{" + e.getKey() + "}}", String.valueOf(e.getValue()));
        }
        return out;
    }

    private io.restassured.specification.RequestSpecification authed(io.restassured.specification.RequestSpecification spec) {
        return authToken == null ? spec : spec.header("Authorization", "Bearer " + authToken);
    }
}
