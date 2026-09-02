package com.swiftfaze.veil.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PopupToggleListenerTest {

    @Test
    void toggleInventoryOpensTheInventoryPopup() {
        InventoryPanel inventoryPanel = new InventoryPanel();
        CodexPanel codexPanel = new CodexPanel();
        PopupToggleListener listener = new PopupToggleListener(inventoryPanel, codexPanel);

        listener.toggleInventory();

        assertTrue(inventoryPanel.isVisible());
    }

    @Test
    void togglingInventoryTwiceClosesItAgain() {
        InventoryPanel inventoryPanel = new InventoryPanel();
        CodexPanel codexPanel = new CodexPanel();
        PopupToggleListener listener = new PopupToggleListener(inventoryPanel, codexPanel);

        listener.toggleInventory();
        listener.toggleInventory();

        assertFalse(inventoryPanel.isVisible());
    }

    @Test
    void toggleCodexOpensTheCodexPopup() {
        InventoryPanel inventoryPanel = new InventoryPanel();
        CodexPanel codexPanel = new CodexPanel();
        PopupToggleListener listener = new PopupToggleListener(inventoryPanel, codexPanel);

        listener.toggleCodex();

        assertTrue(codexPanel.isVisible());
    }

    @Test
    void openingCodexWhileInventoryIsOpenClosesInventoryFirst() {
        InventoryPanel inventoryPanel = new InventoryPanel();
        CodexPanel codexPanel = new CodexPanel();
        PopupToggleListener listener = new PopupToggleListener(inventoryPanel, codexPanel);

        listener.toggleInventory();
        listener.toggleCodex();

        assertFalse(inventoryPanel.isVisible());
        assertTrue(codexPanel.isVisible());
    }

    @Test
    void openingInventoryWhileCodexIsOpenClosesCodexFirst() {
        InventoryPanel inventoryPanel = new InventoryPanel();
        CodexPanel codexPanel = new CodexPanel();
        PopupToggleListener listener = new PopupToggleListener(inventoryPanel, codexPanel);

        listener.toggleCodex();
        listener.toggleInventory();

        assertFalse(codexPanel.isVisible());
        assertTrue(inventoryPanel.isVisible());
    }

    @Test
    void updatePlayerDoesNotThrow() {
        InventoryPanel inventoryPanel = new InventoryPanel();
        CodexPanel codexPanel = new CodexPanel();
        PopupToggleListener listener = new PopupToggleListener(inventoryPanel, codexPanel);

        listener.updatePlayer(null);
    }
}
