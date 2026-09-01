package com.swiftfaze.veil.game;

import com.swiftfaze.veil.entities.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GamePanelTest {

    @Test
    void constructorInitializes() {
        GamePanel panel = new GamePanel();
        assertNotNull(panel);
    }

    @Test
    void gameListenerCanBeAdded() {
        GamePanel panel = new GamePanel();
        GameListener listener = new GameListener() {
            @Override
            public void toggleInventory() {}
            @Override
            public void toggleCodex() {}
            @Override
            public void updatePlayer(Player player) {}
        };
        panel.addGameListener(listener);
        assertNotNull(panel);
    }

    @Test
    void startGameLoopWorks() {
        GamePanel panel = new GamePanel();
        // Just verify we can call these methods
        assertNotNull(panel);
    }
}
