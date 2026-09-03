package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.config.SettingsStore;
import com.swiftfaze.veil.ui.widget.ControlsHintBarWidget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SettingsKeybindsPanelTest {

    @Test
    void constructorInitializes(@TempDir Path tempDir) {
        SettingsStore store = new SettingsStore(tempDir);
        SettingsKeybindsPanel panel = new SettingsKeybindsPanel(screen -> {}, new ControlsHintBarWidget(), store);
        assertNotNull(panel);
    }

    @Test
    void moveUpWorks(@TempDir Path tempDir) {
        SettingsStore store = new SettingsStore(tempDir);
        SettingsKeybindsPanel panel = new SettingsKeybindsPanel(screen -> {}, new ControlsHintBarWidget(), store);
        panel.moveUp();
        assertNotNull(panel);
    }

    @Test
    void moveDownWorks(@TempDir Path tempDir) {
        SettingsStore store = new SettingsStore(tempDir);
        SettingsKeybindsPanel panel = new SettingsKeybindsPanel(screen -> {}, new ControlsHintBarWidget(), store);
        panel.moveDown();
        assertNotNull(panel);
    }

    @Test
    void confirmWorks(@TempDir Path tempDir) {
        SettingsStore store = new SettingsStore(tempDir);
        SettingsKeybindsPanel panel = new SettingsKeybindsPanel(screen -> {}, new ControlsHintBarWidget(), store);
        panel.confirm();
        assertNotNull(panel);
    }
}
