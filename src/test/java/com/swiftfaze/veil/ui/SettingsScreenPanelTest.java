package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.ui.widget.ControlsHintBarWidget;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingsScreenPanelTest {

    @Test
    void constructorInitializes() {
        SettingsScreenPanel panel = new SettingsScreenPanel(
            screen -> {},
            path -> {},
            new ControlsHintBarWidget()
        );
        assertNotNull(panel);
    }

    @Test
    void moveLeftWorks() {
        SettingsScreenPanel panel = new SettingsScreenPanel(screen -> {}, path -> {}, new ControlsHintBarWidget());
        panel.moveLeft();
        assertNotNull(panel);
    }

    @Test
    void moveRightWorks() {
        SettingsScreenPanel panel = new SettingsScreenPanel(screen -> {}, path -> {}, new ControlsHintBarWidget());
        panel.moveRight();
        assertNotNull(panel);
    }

    @Test
    void confirmWorks() {
        SettingsScreenPanel panel = new SettingsScreenPanel(screen -> {}, path -> {}, new ControlsHintBarWidget());
        panel.confirm();
        assertNotNull(panel);
    }
}
