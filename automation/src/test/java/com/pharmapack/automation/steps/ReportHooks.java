package com.pharmapack.automation.steps;

import com.pharmapack.automation.support.ApiAuth;
import io.cucumber.java.BeforeAll;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Report wiring that runs once per test run. */
public class ReportHooks {

    @BeforeAll
    public static void setUpReporting() throws IOException {
        // Every REST Assured request/response becomes an attachment on the Allure step that made it.
        RestAssured.filters(new AllureRestAssured()
                .setRequestAttachmentName("Request")
                .setResponseAttachmentName("Response"));

        // Allure "Environment" widget
        Path results = Path.of(System.getProperty("allure.results.directory", "target/allure-results"));
        Files.createDirectories(results);
        Properties env = new Properties();
        env.setProperty("Application", "PharmaPack QMS - Serialization (Phase 4)");
        env.setProperty("API.Base.URL", ApiAuth.API);
        env.setProperty("Java", System.getProperty("java.version"));
        env.setProperty("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        env.setProperty("Tester", "RB Chowdhury");
        env.setProperty("Cucumber.Tags", System.getProperty("cucumber.filter.tags", "(all)"));
        env.setProperty("UI.Platform", com.pharmapack.automation.ui.UiConfig.platform());
        env.setProperty("UI.Execution", com.pharmapack.automation.ui.UiConfig.execution());
        env.setProperty("UI.Base.URL", com.pharmapack.automation.ui.UiConfig.uiBaseUrl());
        try (Writer w = Files.newBufferedWriter(results.resolve("environment.properties"))) {
            env.store(w, null);
        }
        // Allure "Categories": split real product defects from test/infra problems
        try (var in = ReportHooks.class.getResourceAsStream("/allure-categories.json")) {
            if (in != null) Files.copy(in, results.resolve("categories.json"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
