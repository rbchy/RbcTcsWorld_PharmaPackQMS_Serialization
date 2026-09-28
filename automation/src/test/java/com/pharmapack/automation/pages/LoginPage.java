package com.pharmapack.automation.pages;

import com.pharmapack.automation.ui.UiConfig;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) { super(driver); }

    public LoginPage open() {
        driver.get(UiConfig.uiBaseUrl() + "/");
        // A previous session's JWT lives in localStorage; clear it so every scenario starts at the login screen.
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
        driver.navigate().refresh();
        visible("login-username");
        return this;
    }

    public void loginAs(String username, String password) {
        type("login-username", username);
        type("login-password", password);
        click("login-submit");
    }

    public String errorText() { return visible("login-error").getText().trim(); }
}
