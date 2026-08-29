package com.swiftfaze.veil.steps;

import com.swiftfaze.veil.entities.buildings.Building;
import com.swiftfaze.veil.world.Tile;
import com.swiftfaze.veil.world.WorldScene;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorldScenePopulationAndBuildingPlacementSteps {

    private WorldScene scene;
    private Tile[][] pendingBlueprint;
    private Building building;

    @Given("an empty world scene {int} tiles wide and {int} tiles tall")
    public void anEmptyWorldSceneTilesWideAndTilesTall(int width, int height) {
        scene = new WorldScene(width, height) {
        };
    }

    @Given("the scene is filled with grass")
    public void theSceneIsFilledWithGrass() {
        scene.fillAll(Tile.GRASS);
    }

    @Then("^every tile in the scene is (GRASS|WALL|WOOD|STONE|DOOR)$")
    public void everyTileInTheSceneIs(String tileName) {
        Tile expected = Tile.valueOf(tileName);
        for (int x = 0; x < scene.getWidth(); x++) {
            for (int y = 0; y < scene.getHeight(); y++) {
                assertEquals(expected, scene.getTile(x, y));
            }
        }
    }

    @Given("tile \\({int}, {int}) is water")
    public void tileIsWater(int x, int y) {
        scene.fillRegion(x, y, 1, 1, Tile.WATER);
    }

    @Given("a building with the following blueprint:")
    public void aBuildingWithTheFollowingBlueprint(DataTable table) {
        List<List<String>> rows = table.asLists();
        pendingBlueprint = new Tile[rows.size()][];
        for (int y = 0; y < rows.size(); y++) {
            List<String> row = rows.get(y);
            pendingBlueprint[y] = new Tile[row.size()];
            for (int x = 0; x < row.size(); x++) {
                pendingBlueprint[y][x] = Tile.valueOf(row.get(x).trim());
            }
        }
    }

    @Given("a building with an empty blueprint")
    public void aBuildingWithAnEmptyBlueprint() {
        pendingBlueprint = new Tile[0][0];
    }

    @Given("the building's world position is set to \\({int}, {int})")
    public void theBuildingsWorldPositionIsSetTo(int worldX, int worldY) {
        building = new Building(pendingBlueprint);
        building.setWorldX(worldX);
        building.setWorldY(worldY);
    }

    @When("the building is placed in the scene")
    public void theBuildingIsPlacedInTheScene() {
        scene.placeBuilding(building);
    }

    @Then("^tile \\((\\d+), (\\d+)\\) is (GRASS|WALL|WOOD|STONE|DOOR)$")
    public void tileIs(int x, int y, String tileName) {
        assertEquals(Tile.valueOf(tileName), scene.getTile(x, y));
    }
}
