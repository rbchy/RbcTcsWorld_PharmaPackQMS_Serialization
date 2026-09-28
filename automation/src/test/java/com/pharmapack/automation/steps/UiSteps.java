package com.pharmapack.automation.steps;

import com.pharmapack.automation.pages.AppShell;
import com.pharmapack.automation.pages.LoginPage;
import com.pharmapack.automation.pages.SerializationPage;
import com.pharmapack.automation.support.ApiAuth;
import com.pharmapack.automation.ui.DriverManager;
import com.pharmapack.automation.ui.UiConfig;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.Map;

import static com.pharmapack.automation.support.ApiAuth.API;
import static org.junit.jupiter.api.Assertions.*;

/** Step definitions for @ui scenarios. The same steps run on every -Dplatform (web, Android, iOS, Windows). */
public class UiSteps {

    private WebDriver driver;
    private String currentSerial;

    @Before(value = "@ui", order = 10)
    public void startBrowser(Scenario scenario) {
        checkUiIsUp();
        driver = DriverManager.start();
        scenario.log("Platform: " + UiConfig.platform() + " | execution: " + UiConfig.execution()
                + " | UI: " + UiConfig.uiBaseUrl());
    }

    @After(value = "@ui", order = 10)
    public void stopBrowser(Scenario scenario) {
        if (!DriverManager.isStarted()) return;
        try {
            // Screenshot on every UI scenario (failed ones are the important ones); embedded in Cucumber HTML + Allure.
            byte[] png = ((TakesScreenshot) DriverManager.get()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(png, "image/png", (scenario.isFailed() ? "FAILED - " : "") + UiConfig.platform());
            if (scenario.isFailed()) scenario.log("URL at failure: " + DriverManager.get().getCurrentUrl());
        } catch (Exception e) {
            scenario.log("Screenshot not captured: " + e.getMessage());
        } finally {
            DriverManager.quit();
        }
    }

    // ------------------------------------------------------------------ login
    @Given("I open the PharmaPack QMS login page")
    public void openLogin() { new LoginPage(driver).open(); }

    @When("I log in as {string} with password {string}")
    public void logIn(String user, String password) { new LoginPage(driver).loginAs(user, password); }

    @Given("I am logged in as {string} with password {string}")
    public void loggedIn(String user, String password) {
        new LoginPage(driver).open().loginAs(user, password);
        assertTrue(new AppShell(driver).isLoaded(), "Main navigation not shown after login");
    }

    @Then("I see a login error")
    public void loginError() {
        String err = new LoginPage(driver).errorText();
        assertFalse(err.isBlank(), "Login error message should not be empty");
    }

    @Then("the main navigation is shown")
    public void navShown() { assertTrue(new AppShell(driver).isLoaded()); }

    // ------------------------------------------------------------------ serialization HMI
    @Given("I open the Serialization screen")
    public void openSerialization() {
        new AppShell(driver).goTo("Serialization");
        new SerializationPage(driver).waitUntilLoaded();
    }

    @When("I press START LINE")
    public void pressStart() { new SerializationPage(driver).startLine(); }

    @When("I press STOP")
    public void pressStop() { new SerializationPage(driver).stopLine(); }

    @Then("the PLC state shows {string}")
    public void plcShows(String expected) {
        assertEquals(expected, new SerializationPage(driver).waitForPlcState(expected));
    }

    @When("I generate {int} serial(s) for batch {string}")
    public void generate(int qty, String batchNumber) {
        int before = serialCountViaApi(batchNumber);
        SerializationPage page = new SerializationPage(driver);
        page.generateSerials(batchNumber, qty);
        page.showUnitsFor(batchNumber, before + qty);
        currentSerial = page.newestSerial();
        assertNotNull(currentSerial, "Newest serial row has no data-serial attribute");
    }

    @Then("the new serial shows status {string}")
    public void newSerialStatus(String expected) {
        assertEquals(expected, new SerializationPage(driver).waitForStatus(currentSerial, expected));
    }

    @Then("its Commission button is disabled")
    public void commissionDisabled() {
        assertFalse(new SerializationPage(driver).commissionEnabled(currentSerial),
                "GMP: Commission must stay disabled until a PASS vision result (serial " + currentSerial + ")");
    }

    @Then("its Print button is disabled")
    public void printDisabled() {
        assertFalse(new SerializationPage(driver).printEnabled(currentSerial));
    }

    @When("I print the new serial")
    public void printNewSerial() { new SerializationPage(driver).print(currentSerial); }

    // ------------------------------------------------------------------ helpers
    private static boolean uiChecked;

    /** Fail fast with a clear message instead of a browser ERR_CONNECTION_REFUSED (only for URLs this machine can reach). */
    private static synchronized void checkUiIsUp() {
        String url = UiConfig.uiBaseUrl();
        if (uiChecked || !(url.contains("localhost") || url.contains("127.0.0.1"))) return;
        try {
            // HTTP/1.1 only: Java's default HTTP/2 "Upgrade: h2c" header makes Vite (Node) treat the request as a
            // WebSocket upgrade and never answer. Plus a hard request timeout so this can never hang.
            var client = java.net.http.HttpClient.newBuilder()
                    .version(java.net.http.HttpClient.Version.HTTP_1_1)
                    .connectTimeout(java.time.Duration.ofSeconds(3)).build();
            client.send(java.net.http.HttpRequest.newBuilder(java.net.URI.create(url + "/"))
                            .timeout(java.time.Duration.ofSeconds(5)).GET().build(),
                    java.net.http.HttpResponse.BodyHandlers.discarding());
            uiChecked = true;
        } catch (Exception e) {
            throw new IllegalStateException("The React UI is not running at " + url
                    + " — start it first:  cd frontend && npm install && npm run dev", e);
        }
    }
    /** Test-data lookup through the API, so the UI check knows how many rows to expect. */
    private int serialCountViaApi(String batchNumber) {
        Response batches = ApiAuth.given().get(API + "/batches");
        assertEquals(200, batches.statusCode(), batches.asString());
        List<Map<String, Object>> list = batches.jsonPath().getList("$");
        Object id = list.stream().filter(b -> batchNumber.equals(b.get("batchNumber")))
                .findFirst().orElseThrow(() -> new AssertionError("Batch not found: " + batchNumber)).get("id");
        return ApiAuth.given().get(API + "/serialization/batch/" + id).jsonPath().getList("$").size();
    }
}
