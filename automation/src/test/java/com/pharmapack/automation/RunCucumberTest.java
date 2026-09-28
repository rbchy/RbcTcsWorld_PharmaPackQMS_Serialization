package com.pharmapack.automation;

import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.pharmapack.automation.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value =
        "pretty,"
      + "html:target/cucumber-reports/cucumber.html,"
      + "json:target/cucumber-reports/cucumber.json,"
      + "junit:target/cucumber-reports/cucumber.xml,"
      + "timeline:target/cucumber-reports/timeline,"
      + "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
@ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
public class RunCucumberTest {}
