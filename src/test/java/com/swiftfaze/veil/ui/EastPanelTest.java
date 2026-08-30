package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.entities.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EastPanelTest {

    @Test
    void toggleInventoryFlipsVisibility() {
        EastPanel eastPanel = new EastPanel();
        boolean initiallyVisible = eastPanel.getInventoryPanel().isVisible();

        eastPanel.toggleInventory();

        assertNotEquals(initiallyVisible, eastPanel.getInventoryPanel().isVisible());
    }

    @Test
    void toggleInventoryTwiceRestoresOriginalVisibility() {
        EastPanel eastPanel = new EastPanel();
        boolean initiallyVisible = eastPanel.getInventoryPanel().isVisible();

        eastPanel.toggleInventory();
        eastPanel.toggleInventory();

        assertEquals(initiallyVisible, eastPanel.getInventoryPanel().isVisible());
    }

    @Test
    void openingInventoryDoesNotRestoreGameFocus() {
        EastPanel eastPanel = new EastPanel();
        eastPanel.getInventoryPanel().setVisible(false);
        boolean[] restored = {false};
        eastPanel.setRestoreGameFocusAction(() -> restored[0] = true);

        eastPanel.toggleInventory(); // opens, since it started hidden

        assertFalse(restored[0]);
    }

    @Test
    void closingInventoryRestoresGameFocus() {
        EastPanel eastPanel = new EastPanel();
        eastPanel.getInventoryPanel().setVisible(false);
        boolean[] restored = {false};
        eastPanel.setRestoreGameFocusAction(() -> restored[0] = true);

        eastPanel.toggleInventory(); // opens
        eastPanel.toggleInventory(); // closes

        assertTrue(restored[0]);
    }

    @Test
    void updatePlayerDoesNotThrow() {
        EastPanel eastPanel = new EastPanel();

        assertDoesNotThrow(() -> eastPanel.updatePlayer(new Player(0, 0)));
    }
}
