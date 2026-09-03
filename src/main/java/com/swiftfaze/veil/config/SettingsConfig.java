package com.swiftfaze.veil.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Plain mutable data holder for all persisted settings, Gson-serializable.
 * Serves as the single source of truth for hardcoded defaults via its
 * no-arg constructor.
 */
public class SettingsConfig {
    private int brightness = 5;
    private String fullscreen = "Windowed";
    private String font = "Monospaced";
    private String theme = "Default";
    private int volume = 5;
    private Map<String, String> keybinds = new LinkedHashMap<>(Map.of(
            "Move up", "Up",
            "Move down", "Down",
            "Move left", "Left",
            "Move right", "Right",
            "Toggle inventory", "I"));

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public String getFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(String fullscreen) {
        this.fullscreen = fullscreen;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public Map<String, String> getKeybinds() {
        return keybinds;
    }

    public void setKeybinds(Map<String, String> keybinds) {
        this.keybinds = new LinkedHashMap<>(keybinds);
    }

    /**
     * Resets ONLY the main-screen settings (brightness, fullscreen, font,
     * theme, volume) to their defaults. Does NOT touch keybinds, which has
     * its own separate Reset to Defaults on the Keybinds page.
     */
    public void resetToDefaults() {
        this.brightness = 5;
        this.fullscreen = "Windowed";
        this.font = "Monospaced";
        this.theme = "Default";
        this.volume = 5;
    }
}
