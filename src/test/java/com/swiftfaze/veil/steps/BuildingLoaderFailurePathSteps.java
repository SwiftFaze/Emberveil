package com.swiftfaze.veil.steps;

import com.swiftfaze.veil.entities.buildings.BuildingLoader;
import com.swiftfaze.veil.exceptions.BuildingException;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BuildingLoaderFailurePathSteps {

    private BuildingException thrown;

    @When("a building is loaded from the missing resource {string}")
    public void aBuildingIsLoadedFromTheMissingResource(String fileName) {
        thrown = assertThrows(BuildingException.class, () -> BuildingLoader.load(fileName));
    }

    @When("a building is loaded from the malformed resource {string}")
    public void aBuildingIsLoadedFromTheMalformedResource(String fileName) {
        thrown = assertThrows(BuildingException.class, () -> BuildingLoader.load(fileName));
    }

    @Then("a BuildingException is thrown wrapping the underlying cause")
    public void aBuildingExceptionIsThrownWrappingTheUnderlyingCause() {
        assertNotNull(thrown.getCause());
    }
}
