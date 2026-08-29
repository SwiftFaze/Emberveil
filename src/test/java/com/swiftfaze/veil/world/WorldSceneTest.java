package com.swiftfaze.veil.world;

import com.swiftfaze.veil.entities.buildings.Building;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class WorldSceneTest {

    private WorldScene sceneOf(int width, int height) {
        return new WorldScene(width, height) {
        };
    }

    @Test
    void createBorderFillsAllFourEdgesButNotTheInterior() {
        WorldScene scene = sceneOf(5, 5);

        scene.createBorder(5, 5, Tile.WALL);

        assertEquals(Tile.WALL, scene.getTile(0, 0));
        assertEquals(Tile.WALL, scene.getTile(4, 0));
        assertEquals(Tile.WALL, scene.getTile(0, 4));
        assertEquals(Tile.WALL, scene.getTile(4, 4));
        assertEquals(Tile.WALL, scene.getTile(2, 0));
        assertEquals(Tile.WALL, scene.getTile(0, 2));
        assertEquals(Tile.EMPTY, scene.getTile(2, 2));
    }

    @Test
    void isWalkableReturnsFalseOutOfBounds() {
        WorldScene scene = sceneOf(5, 5);

        assertFalse(scene.isWalkable(-1, 0));
        assertFalse(scene.isWalkable(0, -1));
        assertFalse(scene.isWalkable(5, 0));
        assertFalse(scene.isWalkable(0, 5));
    }

    @Test
    void getTileReturnsNullOutOfBounds() {
        WorldScene scene = sceneOf(5, 5);

        assertNull(scene.getTile(-1, 0));
        assertNull(scene.getTile(0, 5));
    }

    @Test
    void getTileReturnsActualTileInBounds() {
        WorldScene scene = sceneOf(5, 5);
        scene.fillRegion(2, 2, 1, 1, Tile.STONE);

        assertEquals(Tile.STONE, scene.getTile(2, 2));
    }

    @Test
    void getWidthAndHeightReturnConstructorValues() {
        WorldScene scene = sceneOf(7, 9);

        assertEquals(7, scene.getWidth());
        assertEquals(9, scene.getHeight());
    }

    @Test
    void positionSymbolAndColorAreFixedDefaults() {
        WorldScene scene = sceneOf(5, 5);

        assertEquals(0, scene.getX());
        assertEquals(0, scene.getY());
        assertEquals(' ', scene.getSymbol());
        assertEquals(Color.WHITE, scene.getColor());
    }

    @Test
    void renderDrawsNonEmptyTilesWithoutThrowing() {
        WorldScene scene = sceneOf(5, 5);
        scene.fillRegion(1, 1, 1, 1, Tile.GRASS);
        Graphics2D g2d = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();

        assertDoesNotThrow(() -> scene.render(g2d, 15, 15, 0, 0));
    }

    @Test
    void renderWorldSkipsEmptyTilesWithoutThrowing() {
        WorldScene scene = sceneOf(5, 5);
        Graphics2D g2d = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();

        assertDoesNotThrow(() -> scene.renderWorld(g2d, 15, 15, 0, 0));
    }

    @Test
    void fillAllSetsEveryTileToTheGivenType() {
        WorldScene scene = sceneOf(4, 3);

        scene.fillAll(Tile.GRASS);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 3; y++) {
                assertEquals(Tile.GRASS, scene.getTile(x, y));
            }
        }
    }

    @Test
    void placeBuildingStampsBlueprintAtWorldOffsetWithoutTransposingAxes() {
        WorldScene scene = sceneOf(10, 10);
        Tile[][] blueprint = {
                {Tile.WALL, Tile.WOOD},
                {Tile.STONE, Tile.DOOR}
        };
        Building building = new Building(blueprint);
        building.setWorldX(3);
        building.setWorldY(4);

        scene.placeBuilding(building);

        assertEquals(Tile.WALL, scene.getTile(3, 4));
        assertEquals(Tile.WOOD, scene.getTile(4, 4));
        assertEquals(Tile.STONE, scene.getTile(3, 5));
        assertEquals(Tile.DOOR, scene.getTile(4, 5));
    }

    @Test
    void placeBuildingOverwritesTilesAlreadyInTheScene() {
        WorldScene scene = sceneOf(10, 10);
        scene.fillRegion(6, 6, 1, 1, Tile.WATER);
        Building building = new Building(new Tile[][]{{Tile.WALL}});
        building.setWorldX(6);
        building.setWorldY(6);

        scene.placeBuilding(building);

        assertEquals(Tile.WALL, scene.getTile(6, 6));
    }

    @Test
    void placeBuildingWithEmptyBlueprintLeavesTheSceneUnchanged() {
        WorldScene scene = sceneOf(10, 10);
        scene.fillAll(Tile.GRASS);
        Building building = new Building(new Tile[0][0]);
        building.setWorldX(2);
        building.setWorldY(2);

        assertDoesNotThrow(() -> scene.placeBuilding(building));

        assertEquals(Tile.GRASS, scene.getTile(2, 2));
    }
}
