package com.pharmapack.automation.ui;

import java.time.Duration;

/**
 * All UI-run settings. Each one is a -D system property, or the same name as an environment variable
 * (upper case, dots -> underscores), e.g. -Dplatform=ios  or  PLATFORM=ios.
 *
 *  platform   chrome | firefox | edge | safari | android | ios | windows      (default chrome)
 *  execution  local | grid | browserstack                                      (default local)
 *  uiBaseUrl  URL of the React UI as seen FROM THE DEVICE                      (default depends on platform)
 */
public final class UiConfig {
    private UiConfig() {}

    public static String platform()  { return prop("platform", "chrome").toLowerCase(); }
    public static String execution() { return prop("execution", "local").toLowerCase(); }
    public static boolean headless() { return Boolean.parseBoolean(prop("headless", "false")); }
    public static Duration timeout() { return Duration.ofSeconds(Long.parseLong(prop("ui.timeout", "20"))); }

    /** The Android emulator reaches the host Mac as 10.0.2.2; BrowserStack Local tunnels bs-local.com to it. */
    public static String uiBaseUrl() {
        String def = "http://localhost:5173";
        if ("browserstack".equals(execution())) def = "http://bs-local.com:5173";
        else if ("android".equals(platform()) && "local".equals(execution())) def = "http://10.0.2.2:5173";
        return prop("uiBaseUrl", def).replaceAll("/+$", "");
    }

    public static String appiumUrl() { return prop("appium.url", "http://127.0.0.1:4723"); }
    public static String gridUrl()   { return prop("grid.url", "http://localhost:4444"); }

    /** Device / OS overrides (Appium and BrowserStack). */
    public static String deviceName(String def)      { return prop("device", def); }
    public static String platformVersion(String def) { return prop("os.version", def); }
    public static String udid()                      { return prop("udid", ""); }

    public static String prop(String key, String def) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) v = System.getenv(key.toUpperCase().replace('.', '_'));
        return (v == null || v.isBlank()) ? def : v.trim();
    }
}
