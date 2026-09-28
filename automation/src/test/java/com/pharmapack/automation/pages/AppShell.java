package com.pharmapack.automation.pages;

import org.openqa.selenium.WebDriver;

/** Header + left/top navigation that every logged-in screen shares. */
public class AppShell extends BasePage {

    public AppShell(WebDriver driver) { super(driver); }

    public boolean isLoaded() { return visible("nav-dashboard").isDisplayed() && visible("logout").isDisplayed(); }

    /** tab = the visible tab name, e.g. "Serialization", "Audit Trail". */
    public void goTo(String tab) { click("nav-" + tab.toLowerCase().replaceAll("\\s+", "-")); }

    public void logout() { click("logout"); }

    public String notice() { return visible("notice").getText().trim(); }
}
