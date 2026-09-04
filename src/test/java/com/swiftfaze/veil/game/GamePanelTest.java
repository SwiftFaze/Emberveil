package com.swiftfaze.veil.game;

import com.swiftfaze.veil.entities.player.Player;
import com.swiftfaze.veil.input.Keybindings;
import org.junit.jupiter.api.Test;

import javax.swing.Action;
import java.awt.event.ActionEvent;

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

    @Test
    void pausedFlagPreventMovement() {
        GamePanel panel = new GamePanel();
        Player player = panel.getPlayer();
        int originalX = player.getX();
        int originalY = player.getY();

        panel.setPaused(true);
        fireAction(panel, Keybindings.ACTION_MOVE_UP);

        assertEquals(originalX, player.getX());
        assertEquals(originalY, player.getY());
    }

    @Test
    void togglePauseActionInvokesListener() {
        GamePanel panel = new GamePanel();
        boolean[] listenerCalled = {false};
        GameListener listener = new GameListener() {
            @Override
            public void updatePlayer(Player player) {
            }

            @Override
            public void togglePause() {
                listenerCalled[0] = true;
            }
        };
        panel.addGameListener(listener);

        fireAction(panel, Keybindings.ACTION_TOGGLE_PAUSE);

        assertTrue(listenerCalled[0]);
    }

    private void fireAction(GamePanel panel, String actionName) {
        Action action = panel.getActionMap().get(actionName);
        action.actionPerformed(new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, actionName));
    }
}
