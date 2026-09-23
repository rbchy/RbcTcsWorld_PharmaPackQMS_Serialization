package com.pharmapack.automation; import org.junit.platform.suite.api.*; import static io.cucumber.junit.platform.engine.Constants.*;
@Suite @IncludeEngines("cucumber") @SelectClasspathResource("features") @ConfigurationParameter(key=GLUE_PROPERTY_NAME,value="com.pharmapack.automation.steps") public class RunCucumberTest {}
