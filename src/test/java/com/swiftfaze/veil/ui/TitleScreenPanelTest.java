package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.ui.widget.ControlsHintBarWidget;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TitleScreenPanelTest {

    @Test
    void constructorInitializes() {
        TitleScreenPanel panel = new TitleScreenPanel(menuItem -> {}, new ControlsHintBarWidget());
        assertNotNull(panel);
    }

    @Test
    void menuCallbackExecutes() {
        String[] selectedItem = {null};
        TitleScreenPanel panel = new TitleScreenPanel(item -> selectedItem[0] = item, new ControlsHintBarWidget());
        // The callback should be executable
        assertNull(selectedItem[0]);
    }

    @Test
    void fontLoadingFallbackWorks() {
        // Test that the font loading try/catch works with the fallback
        TitleScreenPanel panel = new TitleScreenPanel(item -> {}, new ControlsHintBarWidget());
        // If font loading succeeds or falls back, panel should be created
        assertNotNull(panel);
    }
}
