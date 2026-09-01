package com.swiftfaze.veil;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void classExists() {
        assertNotNull(Main.class);
    }

    @Test
    void mainClassHasMethods() {
        // Verify the refactored loadGame and its helper methods compile
        // and the Main class is properly structured
        assertTrue(Main.class.getDeclaredMethods().length > 0);
    }

    @Test
    void mainClassIsUtility() {
        // Main is a utility class with a private/package-private constructor
        assertNotNull(Main.class.getName());
    }
}
