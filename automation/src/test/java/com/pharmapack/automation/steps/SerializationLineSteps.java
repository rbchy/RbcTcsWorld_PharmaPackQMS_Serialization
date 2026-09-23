package com.pharmapack.automation.steps;

import com.pharmapack.automation.support.ApiAuth;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import static com.pharmapack.automation.support.ApiAuth.API;
import static org.junit.jupiter.api.Assertions.*;

/** Step definitions for features/serialization_line.feature (PLC line + print -> vision -> commission). */
public class SerializationLineSteps {

    private static final long DEMO_BATCH_ID = 1L; // BATCH-DEMO-001 from database/seed.sql

    private String serial;
    private String expectedLot;
    private String expectedExpiry;
    private Response verifyResponse;

    @Given("the packaging line is RUNNING")
    public void lineIsRunning() {
        Response r = ApiAuth.given().post(API + "/line/start");
        assertEquals(200, r.statusCode(), "POST /line/start — body: " + r.asString());
        assertEquals("RUNNING", r.jsonPath().getString("plcState"),
                "PLC did not reach RUNNING (is a device OFFLINE/FAULT?) — body: " + r.asString());
    }

    @Given("a CREATED serial exists for a valid batch")
    public void createdSerialExists() {
        Response r = ApiAuth.given()
                .body("{\"batchId\":" + DEMO_BATCH_ID + ",\"quantity\":1,\"aggregationLevel\":\"UNIT\"}")
                .post(API + "/serialization/commission");
        assertEquals(201, r.statusCode(), "Provision serial — body: " + r.asString());
        serial = r.jsonPath().getString("[0].serialNumber");
        assertNotNull(serial, "No serialNumber returned: " + r.asString());
        assertEquals("CREATED", r.jsonPath().getString("[0].status"));

        // Expected lot/expiry come from the batch master data, exactly as the vision service computes them.
        Response batch = ApiAuth.given().get(API + "/batches/" + DEMO_BATCH_ID);
        assertEquals(200, batch.statusCode(), "GET batch — body: " + batch.asString());
        expectedLot = batch.jsonPath().getString("batchNumber");
        expectedExpiry = batch.jsonPath().getString("expiryDate");
    }

    @When("the serial is printed")
    public void serialIsPrinted() {
        Response r = ApiAuth.given().post(API + "/serialization/print/" + serial);
        assertEquals(200, r.statusCode(), "Print — body: " + r.asString());
        assertEquals("PRINTED", currentStatus());
    }

    @When("the vision simulator reads the correct DataMatrix, lot and expiry")
    public void visionReadsCorrect() {
        verify(serial, expectedLot, expectedExpiry);
    }

    @When("the vision simulator reads a wrong DataMatrix")
    public void visionReadsWrongDataMatrix() {
        verify(serial + "-WRONG", expectedLot, expectedExpiry);
    }

    @When("the serial is commissioned")
    public void serialIsCommissioned() {
        Response r = ApiAuth.given().post(API + "/serialization/commission/" + serial);
        assertEquals(200, r.statusCode(), "Commission — body: " + r.asString());
    }

    @Then("the serial status should be COMMISSIONED")
    public void statusIsCommissioned() {
        assertEquals("COMMISSIONED", currentStatus());
        // Audit trail: the event history must record the full print -> vision -> commission chain.
        Response events = ApiAuth.given().get(API + "/serialization/" + serial + "/events");
        assertEquals(200, events.statusCode(), "Event history — body: " + events.asString());
        java.util.List<String> types = events.jsonPath().getList("eventType");
        for (String t : new String[]{"CREATED", "PRINTED", "VISION_VERIFIED", "COMMISSIONED"}) {
            assertTrue(types.contains(t), "Missing " + t + " event in history: " + types);
        }
    }

    @Then("the vision result should be FAIL")
    public void visionResultIsFail() {
        assertEquals(200, verifyResponse.statusCode(), "Verify — body: " + verifyResponse.asString());
        Response history = ApiAuth.given().get(API + "/serialization/" + serial + "/vision");
        assertEquals(200, history.statusCode(), "Vision history — body: " + history.asString());
        assertEquals("FAIL", history.jsonPath().getString("[0].decision"), "Latest vision record: " + history.asString());
        assertFalse(history.jsonPath().getBoolean("[0].barcodePass"), "Barcode check should have failed");
    }

    @Then("the serial should not be commissioned")
    public void serialNotCommissioned() {
        assertNotEquals("COMMISSIONED", currentStatus());
        // Negative check: the API itself must refuse to commission a rejected serial.
        Response r = ApiAuth.given().post(API + "/serialization/commission/" + serial);
        assertEquals(400, r.statusCode(), "Commissioning a REJECTED serial must be refused — body: " + r.asString());
        assertNotEquals("COMMISSIONED", currentStatus());
    }

    // ---- helpers ----

    private void verify(String barcode, String lot, String expiry) {
        String body = "{\"serialNumber\":\"" + serial + "\",\"actualBarcode\":\"" + barcode + "\","
                + "\"actualLot\":" + json(lot) + ",\"actualExpiry\":" + json(expiry) + "}";
        verifyResponse = ApiAuth.given().body(body).post(API + "/serialization/verify");
        assertEquals(200, verifyResponse.statusCode(), "Verify — body: " + verifyResponse.asString());
    }

    private String currentStatus() {
        Response r = ApiAuth.given().get(API + "/serialization/" + serial);
        assertEquals(200, r.statusCode(), "GET serial — body: " + r.asString());
        return r.jsonPath().getString("status");
    }

    private static String json(String s) { return s == null ? "null" : "\"" + s + "\""; }
}
