package com.pharmapack.automation.ui;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.pharmapack.automation.ui.UiConfig.*;

/**
 * One entry point that returns a WebDriver for any target:
 *
 *  | platform | local                                   | grid                | browserstack              |
 *  |----------|-----------------------------------------|---------------------|---------------------------|
 *  | chrome   | ChromeDriver (Selenium Manager)         | Grid node           | Windows 11 Chrome         |
 *  | firefox  | FirefoxDriver                           | Grid node           | Windows 11 Firefox        |
 *  | edge     | EdgeDriver                              | Grid node           | Windows 11 Edge           |
 *  | safari   | SafariDriver (macOS only)               | Grid node           | macOS Safari              |
 *  | windows  | EdgeDriver when run ON a Windows PC     | Windows Edge node   | Windows 11 Edge           |
 *  | android  | Appium UiAutomator2 + Chrome (emulator) | Appium-in-Grid      | real Android + Chrome     |
 *  | ios      | Appium XCUITest + Safari (simulator)    | Appium-in-Grid      | real iPhone + Safari      |
 *
 * Browser drivers are downloaded automatically by Selenium Manager - no chromedriver setup needed.
 */
public final class DriverFactory {
    private DriverFactory() {}

    public static WebDriver create() {
        String platform = platform();
        return switch (execution()) {
            case "local" -> local(platform);
            case "grid" -> remote(gridUrl(), gridCapabilities(platform));
            case "browserstack" -> remote("https://hub.browserstack.com/wd/hub", browserStackCapabilities(platform));
            default -> throw new IllegalArgumentException("Unknown -Dexecution=" + execution() + " (use local | grid | browserstack)");
        };
    }

    public static boolean isMobile() {
        return "android".equals(platform()) || "ios".equals(platform());
    }

    // ------------------------------------------------------------------ local
    private static WebDriver local(String platform) {
        return switch (platform) {
            case "chrome" -> new ChromeDriver(chrome());
            case "firefox" -> new FirefoxDriver(firefox());
            case "edge" -> new EdgeDriver(edge());
            case "safari" -> new SafariDriver(new SafariOptions());          // one-time: `safaridriver --enable`
            case "windows" -> {
                if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                    throw new IllegalStateException("platform=windows runs Edge on Windows. Run the suite on a Windows PC, "
                            + "or use -Dexecution=grid (Windows node) or -Dexecution=browserstack from this Mac.");
                }
                yield new EdgeDriver(edge());
            }
            case "android" -> remote(appiumUrl(), android());
            case "ios" -> remote(appiumUrl(), ios());
            default -> throw new IllegalArgumentException("Unknown -Dplatform=" + platform
                    + " (use chrome | firefox | edge | safari | windows | android | ios)");
        };
    }

    // ------------------------------------------------------------------ desktop options
    private static ChromeOptions chrome() {
        ChromeOptions o = new ChromeOptions();
        o.addArguments("--window-size=1440,900", "--disable-search-engine-choice-screen");
        if (headless()) o.addArguments("--headless=new");
        return o;
    }

    private static FirefoxOptions firefox() {
        FirefoxOptions o = new FirefoxOptions();
        if (headless()) o.addArguments("-headless");
        return o;
    }

    private static EdgeOptions edge() {
        EdgeOptions o = new EdgeOptions();
        o.addArguments("--window-size=1440,900");
        if (headless()) o.addArguments("--headless=new");
        return o;
    }

    // ------------------------------------------------------------------ Appium (mobile web)
    /** Android emulator/device, Chrome browser. Start Appium with: appium --allow-insecure chromedriver_autodownload */
    private static MutableCapabilities android() {
        MutableCapabilities c = new MutableCapabilities();
        c.setCapability("platformName", "Android");
        c.setCapability("browserName", "Chrome");
        c.setCapability("appium:automationName", "UiAutomator2");
        c.setCapability("appium:deviceName", deviceName("Android Emulator"));
        optional(c, "appium:platformVersion", platformVersion(""));
        optional(c, "appium:udid", udid());
        c.setCapability("appium:chromedriverAutodownload", true);
        c.setCapability("appium:newCommandTimeout", 300);
        return c;
    }

    /** iOS simulator/device, Safari browser. */
    private static MutableCapabilities ios() {
        MutableCapabilities c = new MutableCapabilities();
        c.setCapability("platformName", "iOS");
        c.setCapability("browserName", "Safari");
        c.setCapability("appium:automationName", "XCUITest");
        c.setCapability("appium:deviceName", deviceName("iPhone 16"));
        optional(c, "appium:platformVersion", platformVersion(""));
        optional(c, "appium:udid", udid());
        c.setCapability("appium:wdaLaunchTimeout", 180_000);
        c.setCapability("appium:newCommandTimeout", 300);
        return c;
    }

    // ------------------------------------------------------------------ Selenium Grid
    private static MutableCapabilities gridCapabilities(String platform) {
        return switch (platform) {
            case "chrome" -> chrome();
            case "firefox" -> firefox();
            case "edge" -> edge();
            case "safari" -> new SafariOptions();
            case "windows" -> { EdgeOptions o = edge(); o.setPlatformName("Windows"); yield o; }
            case "android" -> android();
            case "ios" -> ios();
            default -> throw new IllegalArgumentException("Unknown -Dplatform=" + platform);
        };
    }

    // ------------------------------------------------------------------ BrowserStack
    /** Needs BROWSERSTACK_USERNAME / BROWSERSTACK_ACCESS_KEY and the BrowserStack Local tunnel running on this Mac. */
    private static MutableCapabilities browserStackCapabilities(String platform) {
        String user = prop("browserstack.username", null);
        String key = prop("browserstack.access.key", null);
        if (user == null || key == null) {
            throw new IllegalStateException("Set BROWSERSTACK_USERNAME and BROWSERSTACK_ACCESS_KEY (env vars) for -Dexecution=browserstack");
        }
        Map<String, Object> bs = new HashMap<>();
        bs.put("userName", user);
        bs.put("accessKey", key);
        bs.put("local", true);                          // app runs on this Mac -> BrowserStack Local tunnel
        bs.put("projectName", "PharmaPack QMS");
        bs.put("buildName", prop("build.name", "PharmaPack QMS UI"));
        bs.put("sessionName", "UI - " + platform);
        bs.put("debug", true);

        MutableCapabilities c = new MutableCapabilities();
        switch (platform) {
            case "chrome", "firefox", "edge", "windows" -> {
                c.setCapability("browserName", "windows".equals(platform) ? "Edge" : capitalize(platform));
                bs.put("os", "Windows");
                bs.put("osVersion", platformVersion("11"));
            }
            case "safari" -> {
                c.setCapability("browserName", "Safari");
                bs.put("os", "OS X");
                bs.put("osVersion", platformVersion("Sonoma"));
            }
            case "android" -> {
                c.setCapability("browserName", "chrome");
                bs.put("deviceName", deviceName("Samsung Galaxy S23"));
                bs.put("osVersion", platformVersion("13.0"));
                bs.put("realMobile", "true");
            }
            case "ios" -> {
                c.setCapability("browserName", "safari");
                bs.put("deviceName", deviceName("iPhone 15"));
                bs.put("osVersion", platformVersion("17"));
                bs.put("realMobile", "true");
            }
            default -> throw new IllegalArgumentException("Unknown -Dplatform=" + platform);
        }
        c.setCapability("bstack:options", bs);
        return c;
    }

    // ------------------------------------------------------------------ helpers
    private static WebDriver remote(String url, MutableCapabilities caps) {
        try {
            return new RemoteWebDriver(URI.create(url).toURL(), caps);
        } catch (Exception e) {
            throw new IllegalStateException("Could not start a session at " + url + " for platform=" + platform()
                    + " — is the server (Appium / Grid / BrowserStack Local) running? " + e.getMessage(), e);
        }
    }

    private static void optional(MutableCapabilities c, String name, String value) {
        if (value != null && !value.isBlank()) c.setCapability(name, value);
    }

    private static String capitalize(String s) { return Character.toUpperCase(s.charAt(0)) + s.substring(1); }
}
