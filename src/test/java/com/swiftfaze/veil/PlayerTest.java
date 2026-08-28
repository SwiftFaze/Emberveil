package com.swiftfaze.veil;

import com.swiftfaze.veil.entities.player.Player;
import com.swiftfaze.veil.world.Tile;
import com.swiftfaze.veil.world.WorldScene;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerTest {

    private WorldScene sceneOf(int width, int height) {
        return new WorldScene(width, height) {
        };
    }

    @Test
    void movingRightIncreasesXWhenTileIsWalkable() {
        WorldScene scene = sceneOf(10, 10);
        scene.fillAll(Tile.GRASS);
        Player player = new Player(5, 5);

        player.moveRight(scene);

        assertEquals(6, player.getX());
        assertEquals(5, player.getY());
    }

    @Test
    void movingUpIncreasesYWhenTileIsWalkable() {
        WorldScene scene = sceneOf(10, 10);
        scene.fillAll(Tile.GRASS);
        Player player = new Player(5, 5);

        player.moveUp(scene);

        assertEquals(5, player.getX());
        assertEquals(4, player.getY());
    }

    @Test
    void movingIntoANonWalkableTileDoesNotMovePlayer() {
        WorldScene scene = sceneOf(10, 10);
        scene.fillAll(Tile.GRASS);
        scene.fillRegion(6, 5, 1, 1, Tile.WATER);
        Player player = new Player(5, 5);

        player.moveRight(scene);

        assertEquals(5, player.getX());
        assertEquals(5, player.getY());
    }
}
