package com.pharmapack.automation.ui;

import org.openqa.selenium.WebDriver;

/** One driver per thread, so scenarios can later run in parallel. */
public final class DriverManager {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private DriverManager() {}

    public static WebDriver start() {
        WebDriver d = DriverFactory.create();
        DRIVER.set(d);
        return d;
    }

    public static WebDriver get() {
        WebDriver d = DRIVER.get();
        if (d == null) throw new IllegalStateException("No WebDriver — is the scenario tagged @ui?");
        return d;
    }

    public static boolean isStarted() { return DRIVER.get() != null; }

    public static void quit() {
        WebDriver d = DRIVER.get();
        DRIVER.remove();
        if (d != null) {
            try { d.quit(); } catch (Exception ignored) { /* session may already be gone */ }
        }
    }
}
