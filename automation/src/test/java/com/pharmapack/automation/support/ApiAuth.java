package com.pharmapack.automation.support;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * One place for API base URL + JWT login, shared by Cucumber steps and plain JUnit/REST Assured tests.
 * Override with -DbaseUrl=http://host:8080/api -Dqms.user=admin -Dqms.password=admin123
 */
public final class ApiAuth {

    public static final String API =
            System.getProperty("baseUrl", "http://localhost:8080/api").replaceAll("/+$", "");

    private static final String USER = System.getProperty("qms.user", "admin");
    private static final String PASSWORD = System.getProperty("qms.password", "admin123");

    private static volatile String token;

    private ApiAuth() {}

    /** Logs in once per JVM and caches the JWT (token lifetime is 8h, far longer than a test run). */
    public static synchronized String token() {
        if (token == null) {
            Response r = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body("{\"username\":\"" + USER + "\",\"password\":\"" + PASSWORD + "\"}")
                    .post(API + "/auth/login");
            if (r.statusCode() != 200) {
                throw new IllegalStateException("Login as '" + USER + "' failed (HTTP " + r.statusCode()
                        + ") — is the backend running and seeded? Body: " + r.asString());
            }
            token = r.jsonPath().getString("token");
            if (token == null) throw new IllegalStateException("Login returned no token: " + r.asString());
        }
        return token;
    }

    /** REST Assured request pre-loaded with the Bearer token and JSON content type. */
    public static RequestSpecification given() {
        return RestAssured.given()
                .header("Authorization", "Bearer " + token())
                .contentType(ContentType.JSON);
    }
}
