package com.swiftfaze.veil.mods;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A mod-shaped set of widget colors, loaded from a {@code theme.json} file (see
 * {@code ModLoader.loadThemes}). Mirrors the id/collision/"overrides" model every other content
 * type (tiles, classes, items, quests) already uses, but as a single-file-per-mod registration
 * rather than a directory of many files.
 */
public final class WidgetColorTheme {

    /**
     * Every color key a theme must define, matching the 10 colors {@code WidgetTheme} exposes as
     * static fields. A theme missing any of these fails to load (see {@code ModLoadException}).
     */
    public static final Set<String> REQUIRED_KEYS = Set.of(
            "SELECTED_HIGHLIGHT",
            "SELECTED_TEXT",
            "NORMAL_TEXT",
            "DIMMED_TEXT",
            "BACKGROUND",
            "INVALID_HIGHLIGHT",
            "VALID_HIGHLIGHT",
            "TABLE_HEADER_BACKGROUND",
            "TABLE_BORDER",
            "SCROLLBAR_THUMB"
    );

    private final String id;
    private final Map<String, Color> colorsByKey;

    public WidgetColorTheme(String id, Map<String, Color> colorsByKey) {
        this.id = id;
        this.colorsByKey = new LinkedHashMap<>(colorsByKey);
    }

    public String id() {
        return id;
    }

    public Color color(String key) {
        return colorsByKey.get(key);
    }
}
