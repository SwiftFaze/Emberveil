package com.swiftfaze.veil.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
}
