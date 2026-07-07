package com.swiftfaze.emberveil;

import com.swiftfaze.emberveil.entities.player.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    @Test
    void movingRightIncreasesX() {
        Player player = new Player(5, 5);
        player.moveRight();
        assertEquals(6, player.getX());
    }
}
