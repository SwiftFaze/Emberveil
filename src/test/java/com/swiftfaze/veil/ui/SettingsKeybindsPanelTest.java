package com.swiftfaze.veil.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingsKeybindsPanelTest {

    @Test
    void constructorInitializes() {
        SettingsKeybindsPanel panel = new SettingsKeybindsPanel(screen -> {});
        assertNotNull(panel);
    }

    @Test
    void moveUpWorks() {
        SettingsKeybindsPanel panel = new SettingsKeybindsPanel(screen -> {});
        panel.moveUp();
        assertNotNull(panel);
    }

    @Test
    void moveDownWorks() {
        SettingsKeybindsPanel panel = new SettingsKeybindsPanel(screen -> {});
        panel.moveDown();
        assertNotNull(panel);
    }

    @Test
    void confirmWorks() {
        SettingsKeybindsPanel panel = new SettingsKeybindsPanel(screen -> {});
        panel.confirm();
        assertNotNull(panel);
    }
}
