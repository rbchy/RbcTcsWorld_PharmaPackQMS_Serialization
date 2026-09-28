package com.pharmapack.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/** "Packaging Line / L3 Serialization Manager" screen. */
public class SerializationPage extends BasePage {

    public SerializationPage(WebDriver driver) { super(driver); }

    public SerializationPage waitUntilLoaded() { visible("plc-state"); return this; }

    // ---- line HMI
    public void startLine() { click("line-start"); }
    public void stopLine()  { click("line-stop"); }

    public String waitForPlcState(String expected) {
        wait.until(ExpectedConditions.textToBe(tid("plc-state"), expected));
        return visible("plc-state").getText().trim();
    }

    // ---- generate serials
    public void generateSerials(String batchNumber, int quantity) {
        selectByText("serial-batch", batchNumber);
        type("serial-quantity", String.valueOf(quantity));
        click("serial-generate");
        // Wait for the API round-trip to finish: success notice, or fail fast with the UI's own error message.
        String result = wait.until(d -> {
            var n = all("notice");
            if (n.isEmpty()) return null;
            String kind = n.get(0).getDomAttribute("data-kind");
            String text = n.get(0).getText().trim();
            if ("error".equals(kind)) return "ERROR: " + text;
            return text.toLowerCase().contains("commissioned") ? text : null;
        });
        if (result.startsWith("ERROR: ")) throw new AssertionError("UI refused to generate serials — " + result);
    }

    // ---- serialized units table
    /** Select the batch in the units table and wait until it shows at least {@code minRows} rows. */
    public void showUnitsFor(String batchNumber, int minRows) {
        // Re-select on every poll: each selection re-fetches the list, so a fetch that raced the save is retried.
        wait.until(d -> {
            selectByText("units-batch", batchNumber);
            return rows().size() >= minRows;
        });
    }

    public List<WebElement> rows() { return all("unit-row"); }

    /** Rows are ordered by id, so the newest serial is the last row. */
    public WebElement newestRow() {
        List<WebElement> r = rows();
        if (r.isEmpty()) throw new IllegalStateException("No serialized units shown");
        WebElement row = r.get(r.size() - 1);
        scrollIntoView(row);
        return row;
    }

    public String newestSerial() { return newestRow().getDomAttribute("data-serial"); }

    public String statusOf(String serial) { return row(serial).getDomAttribute("data-status"); }

    public String waitForStatus(String serial, String expected) {
        wait.until(d -> expected.equals(row(serial).getDomAttribute("data-status")));
        return statusOf(serial);
    }

    public boolean printEnabled(String serial)      { return row(serial).findElement(tid("unit-print")).isEnabled(); }
    public boolean commissionEnabled(String serial) { return row(serial).findElement(tid("unit-commission")).isEnabled(); }

    public void print(String serial) { click(row(serial).findElement(tid("unit-print"))); }

    private WebElement row(String serial) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='unit-row'][data-serial='" + serial + "']")));
    }
}
