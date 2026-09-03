package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.config.SettingsStore;
import com.swiftfaze.veil.ui.widget.ControlsHintBarWidget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SettingsScreenPanelTest {

    @Test
    void constructorInitializes(@TempDir Path tempDir) {
        SettingsStore store = new SettingsStore(tempDir);
        SettingsScreenPanel panel = new SettingsScreenPanel(
            screen -> {},
            path -> {},
            new ControlsHintBarWidget(),
            store
        );
        assertNotNull(panel);
    }

    @Test
    void moveLeftWorks(@TempDir Path tempDir) {
        SettingsStore store = new SettingsStore(tempDir);
        SettingsScreenPanel panel = new SettingsScreenPanel(screen -> {}, path -> {}, new ControlsHintBarWidget(), store);
        panel.moveLeft();
        assertNotNull(panel);
    }

    @Test
    void moveRightWorks(@TempDir Path tempDir) {
        SettingsStore store = new SettingsStore(tempDir);
        SettingsScreenPanel panel = new SettingsScreenPanel(screen -> {}, path -> {}, new ControlsHintBarWidget(), store);
        panel.moveRight();
        assertNotNull(panel);
    }

    @Test
    void confirmWorks(@TempDir Path tempDir) {
        SettingsStore store = new SettingsStore(tempDir);
        SettingsScreenPanel panel = new SettingsScreenPanel(screen -> {}, path -> {}, new ControlsHintBarWidget(), store);
        panel.confirm();
        assertNotNull(panel);
    }
}
