package com.swiftfaze.veil.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingsScreenPanelTest {

    @Test
    void constructorInitializes() {
        SettingsScreenPanel panel = new SettingsScreenPanel(
            screen -> {},
            path -> {}
        );
        assertNotNull(panel);
    }

    @Test
    void moveLeftWorks() {
        SettingsScreenPanel panel = new SettingsScreenPanel(screen -> {}, path -> {});
        panel.moveLeft();
        assertNotNull(panel);
    }

    @Test
    void moveRightWorks() {
        SettingsScreenPanel panel = new SettingsScreenPanel(screen -> {}, path -> {});
        panel.moveRight();
        assertNotNull(panel);
    }

    @Test
    void confirmWorks() {
        SettingsScreenPanel panel = new SettingsScreenPanel(screen -> {}, path -> {});
        panel.confirm();
        assertNotNull(panel);
    }
}
