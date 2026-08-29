package com.swiftfaze.veil.steps;

import com.swiftfaze.veil.Camera;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CameraBehaviorSteps {

    private Camera camera;

    @Given("a camera with a viewport {int} tiles wide and {int} tiles tall")
    public void aCameraWithAViewportTilesWideAndTilesTall(int width, int height) {
        camera = new Camera(width, height);
    }

    @When("the camera centers on position \\({int}, {int})")
    public void theCameraCentersOnPosition(int x, int y) {
        camera.centerOn(x, y);
    }

    @Given("the camera has centered on position \\({int}, {int})")
    public void theCameraHasCenteredOnPosition(int x, int y) {
        camera.centerOn(x, y);
    }

    @Then("the camera's offset is \\({int}, {int})")
    public void theCamerasOffsetIs(int x, int y) {
        assertEquals(x, camera.getX());
        assertEquals(y, camera.getY());
    }
}
