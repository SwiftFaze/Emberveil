package com.swiftfaze.veil.mods;

import com.swiftfaze.veil.entities.buildings.Building;
import com.swiftfaze.veil.entities.player.Stats;
import com.swiftfaze.veil.entities.player.classes.PlayerClass;
import com.swiftfaze.veil.world.Tile;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals(registry.getTile("core:stone"), blueprint[0][0], "top-left corner is a stone wall");
        assertEquals(registry.getTile("core:door"), blueprint[6][3], "door sits in the middle of the south wall");
    }

    @Test
    void loadsAllCoreTilesFromDisk() {
        ModRegistry registry = ModLoader.load(Paths.get("mods"));

        Tile grass = registry.getTile("core:grass");
        assertNotNull(grass, "core:grass should be loaded from mods/core/tiles/");
        assertTrue(grass.isWalkable());
        assertEquals('⡐', grass.getSymbol());
    }

    @Test
    void loadsCoreWarriorClassFromDisk() {
        ModRegistry registry = ModLoader.load(Paths.get("mods"));
        PlayerClass warrior = registry.getPlayerClass("core:warrior");

        assertNotNull(warrior, "core:warrior should be loaded from mods/core/classes/");
        assertEquals("Warrior", warrior.getName());

        Stats stats = statsAtLevel(warrior, 0);
        assertEquals(15, stats.getStrength());
        assertEquals(120, stats.getMaxHp());
    }

    @Test
    void loadsCoreMageClassFromDisk() {
        ModRegistry registry = ModLoader.load(Paths.get("mods"));
        PlayerClass mage = registry.getPlayerClass("core:mage");

        assertNotNull(mage, "core:mage should be loaded from mods/core/classes/");
        assertEquals("Mage", mage.getName());

        Stats stats = statsAtLevel(mage, 0);
        assertEquals(16, stats.getIntelligence());
        assertEquals(70, stats.getMaxHp());
        assertEquals(100, stats.getMaxMana());
    }

    private Stats statsAtLevel(PlayerClass playerClass, int level) {
        Stats stats = new Stats();
        playerClass.applyStatsAtLevel(stats, level);
        return stats;
    }
}
