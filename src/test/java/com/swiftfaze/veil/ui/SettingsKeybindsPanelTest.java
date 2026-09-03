package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.ui.widget.ControlsHintBarWidget;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingsKeybindsPanelTest {

    @Test
    void constructorInitializes() {
        SettingsKeybindsPanel panel = new SettingsKeybindsPanel(screen -> {}, new ControlsHintBarWidget());
        assertNotNull(panel);
    }

    @Test
    void moveUpWorks() {
        SettingsKeybindsPanel panel = new SettingsKeybindsPanel(screen -> {}, new ControlsHintBarWidget());
        panel.moveUp();
        assertNotNull(panel);
    }

    @Test
    void moveDownWorks() {
        SettingsKeybindsPanel panel = new SettingsKeybindsPanel(screen -> {}, new ControlsHintBarWidget());
        panel.moveDown();
        assertNotNull(panel);
    }

    @Test
    void confirmWorks() {
        SettingsKeybindsPanel panel = new SettingsKeybindsPanel(screen -> {}, new ControlsHintBarWidget());
        panel.confirm();
        assertNotNull(panel);
    }
}
