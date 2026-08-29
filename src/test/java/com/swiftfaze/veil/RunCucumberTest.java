package com.swiftfaze.veil;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

/**
 * Feature files for areas not yet implemented are tagged {@code @pending}
 * (see specs/features/README.md's per-area sequencing) so their undefined
 * steps don't fail the build before that area's implementation + step
 * wiring lands.
 */
@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.swiftfaze.veil.steps")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @pending")
public class RunCucumberTest {
}
