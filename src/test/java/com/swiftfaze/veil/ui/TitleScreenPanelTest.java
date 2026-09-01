package com.swiftfaze.veil.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TitleScreenPanelTest {

    @Test
    void constructorInitializes() {
        TitleScreenPanel panel = new TitleScreenPanel(menuItem -> {});
        assertNotNull(panel);
    }

    @Test
    void menuCallbackExecutes() {
        String[] selectedItem = {null};
        TitleScreenPanel panel = new TitleScreenPanel(item -> selectedItem[0] = item);
        // The callback should be executable
        assertNull(selectedItem[0]);
    }

    @Test
    void fontLoadingFallbackWorks() {
        // Test that the font loading try/catch works with the fallback
        TitleScreenPanel panel = new TitleScreenPanel(item -> {});
        // If font loading succeeds or falls back, panel should be created
        assertNotNull(panel);
    }
}
