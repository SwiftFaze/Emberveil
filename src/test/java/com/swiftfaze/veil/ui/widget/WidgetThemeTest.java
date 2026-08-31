package com.swiftfaze.veil.ui.widget;

import com.swiftfaze.veil.mods.WidgetColorTheme;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * WidgetTheme's fields are mutable statics shared across the whole test JVM (see
 * WidgetTheme.applyTheme's javadoc), so this test snapshots and restores them around every
 * case to avoid leaking a mutation into other tests running in the same fork.
 */
class WidgetThemeTest {

    private Map<String, Color> originalColors;

    @BeforeEach
    void snapshotOriginalColors() {
        originalColors = currentColors();
    }

    @AfterEach
    void restoreOriginalColors() {
        WidgetTheme.applyTheme(new WidgetColorTheme("test:snapshot", originalColors));
    }

    @Test
    void applyThemeOverwritesAllTenStaticsFromTheGivenTheme() {
        Map<String, Color> colors = new LinkedHashMap<>();
        colors.put("SELECTED_HIGHLIGHT", new Color(10, 20, 30));
        colors.put("SELECTED_TEXT", new Color(11, 21, 31));
        colors.put("NORMAL_TEXT", new Color(12, 22, 32));
        colors.put("DIMMED_TEXT", new Color(13, 23, 33));
        colors.put("BACKGROUND", new Color(14, 24, 34));
        colors.put("INVALID_HIGHLIGHT", new Color(15, 25, 35));
        colors.put("VALID_HIGHLIGHT", new Color(16, 26, 36));
        colors.put("TABLE_HEADER_BACKGROUND", new Color(17, 27, 37));
        colors.put("TABLE_BORDER", new Color(18, 28, 38));
        colors.put("SCROLLBAR_THUMB", new Color(19, 29, 39));
        WidgetColorTheme theme = new WidgetColorTheme("test:theme", colors);

        WidgetTheme.applyTheme(theme);

        assertEquals(new Color(10, 20, 30), WidgetTheme.SELECTED_HIGHLIGHT);
        assertEquals(new Color(11, 21, 31), WidgetTheme.SELECTED_TEXT);
        assertEquals(new Color(12, 22, 32), WidgetTheme.NORMAL_TEXT);
        assertEquals(new Color(13, 23, 33), WidgetTheme.DIMMED_TEXT);
        assertEquals(new Color(14, 24, 34), WidgetTheme.BACKGROUND);
        assertEquals(new Color(15, 25, 35), WidgetTheme.INVALID_HIGHLIGHT);
        assertEquals(new Color(16, 26, 36), WidgetTheme.VALID_HIGHLIGHT);
        assertEquals(new Color(17, 27, 37), WidgetTheme.TABLE_HEADER_BACKGROUND);
        assertEquals(new Color(18, 28, 38), WidgetTheme.TABLE_BORDER);
        assertEquals(new Color(19, 29, 39), WidgetTheme.SCROLLBAR_THUMB);
    }

    private Map<String, Color> currentColors() {
        Map<String, Color> colors = new LinkedHashMap<>();
        colors.put("SELECTED_HIGHLIGHT", WidgetTheme.SELECTED_HIGHLIGHT);
        colors.put("SELECTED_TEXT", WidgetTheme.SELECTED_TEXT);
        colors.put("NORMAL_TEXT", WidgetTheme.NORMAL_TEXT);
        colors.put("DIMMED_TEXT", WidgetTheme.DIMMED_TEXT);
        colors.put("BACKGROUND", WidgetTheme.BACKGROUND);
        colors.put("INVALID_HIGHLIGHT", WidgetTheme.INVALID_HIGHLIGHT);
        colors.put("VALID_HIGHLIGHT", WidgetTheme.VALID_HIGHLIGHT);
        colors.put("TABLE_HEADER_BACKGROUND", WidgetTheme.TABLE_HEADER_BACKGROUND);
        colors.put("TABLE_BORDER", WidgetTheme.TABLE_BORDER);
        colors.put("SCROLLBAR_THUMB", WidgetTheme.SCROLLBAR_THUMB);
        return colors;
    }
}
