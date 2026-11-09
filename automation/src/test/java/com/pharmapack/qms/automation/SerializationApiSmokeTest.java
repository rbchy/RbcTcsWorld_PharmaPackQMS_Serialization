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
            .body("plcState", oneOf("STOPPED", "RUNNING", "FAULT", "EMERGENCY_STOP"))
            .body("devices.keySet()", hasItems(DEVICES));
    }

    @Test
    void lineStatusRequiresAuthentication() {
        io.restassured.RestAssured.given()
            .when()
            .get(API + "/line/status")
            .then()
            .statusCode(401);
    }

    private static final String[] DEVICES = {"PRINTER-01", "VISION-01", "SCANNER-01", "REJECT-01"};

    private static void allDevices(String state) {
        for (String d : DEVICES) {
            ApiAuth.given().when().put(API + "/line/devices/" + d + "?state=" + state).then().statusCode(200);
        }
    }

    @Test
    void lineStartsWhenEveryDeviceIsReady() {
        allDevices("READY");
        ApiAuth.given()
            .when()
            .post(API + "/line/start")
            .then()
            .statusCode(200)
            .body("plcState", equalTo("RUNNING"));
    }

    @Test
    void aFaultyDeviceStopsTheLineAndBlocksStart() {
        allDevices("READY");
        try {
            ApiAuth.given().when().put(API + "/line/devices/VISION-01?state=FAULT")
                .then().statusCode(200).body("plcState", equalTo("FAULT"));
            ApiAuth.given().when().post(API + "/line/start")
                .then().statusCode(200).body("plcState", equalTo("FAULT"));
        } finally {
            allDevices("READY");
            ApiAuth.given().when().post(API + "/line/start").then().body("plcState", equalTo("RUNNING"));
        }
    }
}
