package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.config.SettingsStore;
import com.swiftfaze.veil.ui.widget.ControlsHintBarWidget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

    @Test
    void backDefaultsToTitle(@TempDir Path tempDir) {
        SettingsStore store = new SettingsStore(tempDir);
        List<String> capturedScreen = new ArrayList<>();
        SettingsScreenPanel panel = new SettingsScreenPanel(
            screen -> capturedScreen.add(screen),
            path -> {},
            new ControlsHintBarWidget(),
            store
        );

        panel.back();

        assertTrue(capturedScreen.contains("title"));
    }

    @Test
    void backUsesSetBackTarget(@TempDir Path tempDir) {
        SettingsStore store = new SettingsStore(tempDir);
        List<String> capturedScreen = new ArrayList<>();
        SettingsScreenPanel panel = new SettingsScreenPanel(
            screen -> capturedScreen.add(screen),
            path -> {},
            new ControlsHintBarWidget(),
            store
        );

        panel.setBackTarget("pause");
        panel.back();

        assertTrue(capturedScreen.contains("pause"));
    }
}
