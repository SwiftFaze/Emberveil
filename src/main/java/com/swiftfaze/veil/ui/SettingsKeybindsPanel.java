package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.input.Keybindings;
import com.swiftfaze.veil.ui.widget.WidgetTheme;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SettingsKeybindsPanel extends JPanel {
    private final List<String> actions;
    private int selectedIndex = 0;
    private final Consumer<String> onBack;
    private final Map<String, String> keyBindings;

    private boolean footerFocused = false;
    private int footerIndex = 0;
    private boolean popupOpen = false;
    private static final List<String> FOOTER_ACTIONS = List.of("Apply", "Cancel", "Go back");

    public SettingsKeybindsPanel(Consumer<String> onBack) {
        this.onBack = onBack;
        this.actions = List.of("Move up", "Move down", "Move left", "Move right", "Toggle inventory");
        this.keyBindings = new HashMap<>();

        setBackground(Color.BLACK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setFocusable(false);

        initializeBindings();
        refresh();
    }

    private void initializeBindings() {
        keyBindings.put("Move up", "Up");
        keyBindings.put("Move down", "Down");
        keyBindings.put("Move left", "Left");
        keyBindings.put("Move right", "Right");
        keyBindings.put("Toggle inventory", "I");
    }

    public String getHighlightedActionName() {
        if (selectedIndex < actions.size()) {
            return actions.get(selectedIndex);
        }
        return "";
    }

    public void moveUp() {
        if (footerFocused) {
            if (footerIndex > 0) {
                footerIndex--;
                refresh();
            }
        } else {
            if (selectedIndex > 0) {
                selectedIndex--;
                refresh();
            }
        }
    }

    public void moveDown() {
        if (footerFocused) {
            if (footerIndex < FOOTER_ACTIONS.size() - 1) {
                footerIndex++;
                refresh();
            }
        } else {
            if (selectedIndex < actions.size() - 1) {
                selectedIndex++;
                refresh();
            }
        }
    }

    public void confirm() {
        if (footerFocused) {
            onBack.accept("settings");
        } else {
            popupOpen = true;
        }
    }

    public void back() {
        onBack.accept("settings");
    }

    public String getKeyForAction(String action) {
        return keyBindings.getOrDefault(action, "");
    }

    public void updateKeyForAction(String action, String key) {
        keyBindings.put(action, key);
        popupOpen = false;
        refresh();
    }

    public boolean isPopupOpen() {
        return popupOpen;
    }

    public String getHighlightedFooterAction() {
        return FOOTER_ACTIONS.get(footerIndex);
    }

    public void highlightFooterAction(String action) {
        footerFocused = true;
        footerIndex = FOOTER_ACTIONS.indexOf(action);
    }

    public void pressKey(String key) {
        if (popupOpen) {
            updateKeyForAction(getHighlightedActionName(), key);
        }
    }

    private void refresh() {
        removeAll();
        for (int i = 0; i < actions.size(); i++) {
            String action = actions.get(i);
            String key = keyBindings.getOrDefault(action, "");
            JLabel label = new JLabel(action + ": " + key);
            label.setForeground(i == selectedIndex ? WidgetTheme.SELECTED_TEXT : WidgetTheme.NORMAL_TEXT);
            label.setBackground(i == selectedIndex ? WidgetTheme.SELECTED_HIGHLIGHT : WidgetTheme.BACKGROUND);
            label.setOpaque(true);
            add(label);
        }
        revalidate();
        repaint();
    }

    public List<String> getAllActions() {
        return actions;
    }
}
