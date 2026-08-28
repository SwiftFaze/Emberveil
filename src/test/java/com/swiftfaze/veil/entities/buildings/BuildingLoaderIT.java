package com.swiftfaze.veil.entities.buildings;

import com.swiftfaze.veil.world.Tile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration test: exercises real disk I/O + JSON parsing, unlike the unit tests.
 * Runs only via {@code mvn verify} (Failsafe), not {@code mvn test}.
 */
class BuildingLoaderIT {

    @Test
    void loadsSmallHouseBlueprintFromDisk() {
        Building building = BuildingLoader.load("small_house_01.json");

        Tile[][][] blueprint = building.getBlueprint();

        assertEquals(1, blueprint.length, "fixture defines one floor");
        assertEquals(7, blueprint[0].length, "fixture is 7 rows tall");
        assertEquals(7, blueprint[0][0].length, "fixture is 7 columns wide");
        assertEquals(Tile.STONE, blueprint[0][0][0], "top-left corner is a stone wall");
        assertEquals(Tile.DOOR, blueprint[0][6][3], "door sits in the middle of the south wall");
    }
}
