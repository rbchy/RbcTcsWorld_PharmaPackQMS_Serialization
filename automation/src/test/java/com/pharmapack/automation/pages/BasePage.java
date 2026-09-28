package com.pharmapack.automation.pages;

import com.pharmapack.automation.ui.UiConfig;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * Shared helpers that behave the same on desktop browsers, Android Chrome and iOS Safari.
 * Elements are found by data-testid, which the React UI exposes for automation.
 */
public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, UiConfig.timeout());
    }

    protected static By tid(String testId) { return By.cssSelector("[data-testid='" + testId + "']"); }

    protected WebElement visible(String testId) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(tid(testId)));
    }

    protected List<WebElement> all(String testId) { return driver.findElements(tid(testId)); }

    protected boolean isShown(String testId) {
        return all(testId).stream().anyMatch(WebElement::isDisplayed);
    }

    /** Scroll into view first (small phone screens, horizontally scrolling nav), then click; JS click as fallback. */
    protected void click(WebElement e) {
        scrollIntoView(e);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(e)).click();
        } catch (ElementNotInteractableException ex) { // includes ElementClickInterceptedException
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", e);
        }
    }

    protected void click(String testId) { click(visible(testId)); }

    protected void type(String testId, String text) {
        WebElement e = visible(testId);
        scrollIntoView(e);
        e.clear();
        e.sendKeys(text);
    }

    /**
     * Pick an option by its visible text. Done with JS + a bubbling 'change' event because native pickers on
     * iOS Safari / Android Chrome cannot be driven with Selenium's Select; React's onChange still fires.
     */
    protected void selectByText(String testId, String text) {
        WebElement select = visible(testId);
        wait.until(d -> select.findElements(By.tagName("option")).stream().anyMatch(o -> o.getText().trim().equals(text)));
        Object ok = ((JavascriptExecutor) driver).executeScript(
                "const s = arguments[0];"
              + "const o = Array.from(s.options).find(x => x.text.trim() === arguments[1]);"
              + "if (!o) return false; s.value = o.value;"
              + "s.dispatchEvent(new Event('change', { bubbles: true })); return true;", select, text);
        if (!Boolean.TRUE.equals(ok)) throw new NoSuchElementException("Option '" + text + "' not found in " + testId);
    }

    protected void scrollIntoView(WebElement e) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center', inline:'center'});", e);
    }
}
