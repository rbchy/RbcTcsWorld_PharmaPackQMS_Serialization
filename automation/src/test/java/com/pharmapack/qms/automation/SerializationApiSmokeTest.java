package com.pharmapack.qms.automation;

import com.pharmapack.automation.support.ApiAuth;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.pharmapack.automation.support.ApiAuth.API;
import static org.hamcrest.Matchers.*;

@Tag("serialization")
class SerializationApiSmokeTest {

    @Test
    void lineStatusEndpointShouldExposePlcAndDevices() {
        ApiAuth.given()
            .when()
            .get(API + "/line/status")
            .then()
            .statusCode(200)
            .body("plcState", notNullValue())
            .body("devices", notNullValue());
    }

    @Test
    void lineStatusRequiresAuthentication() {
        io.restassured.RestAssured.given()
            .when()
            .get(API + "/line/status")
            .then()
            .statusCode(401);
    }

    @Test
    void lineCanBeStarted() {
        ApiAuth.given()
            .when()
            .post(API + "/line/start")
            .then()
            .statusCode(200)
            .body("plcState", anyOf(equalTo("RUNNING"), equalTo("FAULT")));
    }
}
