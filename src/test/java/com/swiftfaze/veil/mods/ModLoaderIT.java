package com.swiftfaze.veil.mods;

import com.swiftfaze.veil.entities.buildings.Building;
import com.swiftfaze.veil.world.Tile;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test: exercises real disk I/O + JSON parsing against the
 * repo's real mods/ directory, unlike the unit tests.
 * Runs only via {@code mvn verify} (Failsafe), not {@code mvn test}.
 */
class ModLoaderIT {

    @Test
    void loadsCoreSmallHouseBlueprintFromDisk() {
        ModRegistry registry = ModLoader.load(Paths.get("mods"));
        Building building = registry.getBuilding("core:small_house_01");

        assertNotNull(building, "core:small_house_01 should be loaded from mods/core/buildings/");

        Tile[][] blueprint = building.getBlueprint();

        assertEquals(7, blueprint.length, "fixture is 7 rows tall");
        assertEquals(7, blueprint[0].length, "fixture is 7 columns wide");
        assertEquals(Tile.STONE, blueprint[0][0], "top-left corner is a stone wall");
        assertEquals(Tile.DOOR, blueprint[6][3], "door sits in the middle of the south wall");
    }
}
