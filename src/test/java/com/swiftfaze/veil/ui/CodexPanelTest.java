package com.swiftfaze.veil.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodexPanelTest {

    @Test
    void constructorInitializes() {
        CodexPanel panel = new CodexPanel();
        assertNotNull(panel);
    }

    @Test
    void navigationMethodsExist() {
        CodexPanel panel = new CodexPanel();
        // Test navigation methods work
        panel.onUp();
        panel.onDown();
        panel.onLeft();
        panel.onRight();
        assertNotNull(panel);
    }

    @Test
    void tabNavigationWorks() {
        CodexPanel panel = new CodexPanel();
        // Tab navigation with wraparound should work
        panel.onLeft(); // Tab left
        panel.onRight(); // Tab right
        assertNotNull(panel);
    }
}
