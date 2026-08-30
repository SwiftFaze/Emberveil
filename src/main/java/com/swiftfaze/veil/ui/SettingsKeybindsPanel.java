package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.ui.widget.WidgetTheme;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SettingsKeybindsPanel extends JPanel {
    private static final Font ROW_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 16);
    private static final List<String> FOOTER_ACTIONS = List.of("Go back", "Reset to Defaults", "Cancel", "Apply");

    private final List<String> actions;
    private final Map<String, String> keyBindings;
    private final Consumer<String> onBack;
    private final JPanel actionsPanel;
    private final JPanel footerPanel;

    private int selectedIndex = 0;
    private boolean footerFocused = false;
    private int footerIndex = 0;
    private boolean popupOpen = false;

    public SettingsKeybindsPanel(Consumer<String> onBack) {
        this.onBack = onBack;
        this.actions = List.of("Move up", "Move down", "Move left", "Move right", "Toggle inventory");
        this.keyBindings = new HashMap<>();

        setBackground(Color.BLACK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2),
                BorderFactory.createEmptyBorder(20, 40, 20, 40)));
        setFocusable(true);

        JLabel header = new JLabel("Keybinds");
        header.setForeground(Color.WHITE);
        header.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        actionsPanel = new JPanel();
        actionsPanel.setBackground(Color.BLACK);
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        actionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        footerPanel = new JPanel();
        footerPanel.setBackground(Color.BLACK);
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.X_AXIS));
        footerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        add(Box.createVerticalGlue());
        add(header);
        add(Box.createVerticalStrut(20));
        add(actionsPanel);
        add(footerPanel);
        add(Box.createVerticalGlue());

        initializeBindings();
        refresh();
        addKeyListener(new KeybindsKeyListener());
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
            footerFocused = false;
            selectedIndex = actions.size() - 1;
        } else if (selectedIndex > 0) {
            selectedIndex--;
        }
        refresh();
    }

    public void moveDown() {
        if (footerFocused) {
            return;
        }
        if (selectedIndex < actions.size() - 1) {
            selectedIndex++;
        } else {
            footerFocused = true;
            footerIndex = 0;
        }
        refresh();
    }

    public void moveFooterLeft() {
        if (footerFocused && footerIndex > 0) {
            footerIndex--;
            refresh();
        }
    }

    public void moveFooterRight() {
        if (footerFocused && footerIndex < FOOTER_ACTIONS.size() - 1) {
            footerIndex++;
            refresh();
        }
    }

    public void confirm() {
        if (!footerFocused) {
            popupOpen = true;
            refresh();
        } else if ("Reset to Defaults".equals(getHighlightedFooterAction())) {
            initializeBindings();
            refresh();
        } else {
            onBack.accept("settings");
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
        refresh();
    }

    public void pressKey(String key) {
        if (popupOpen) {
            updateKeyForAction(getHighlightedActionName(), key);
        }
    }

    private void refresh() {
        actionsPanel.removeAll();
        for (int i = 0; i < actions.size(); i++) {
            String action = actions.get(i);
            String key = keyBindings.getOrDefault(action, "");
            JLabel label = new JLabel(action + ": " + key);
            label.setFont(ROW_FONT);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            boolean highlighted = !footerFocused && i == selectedIndex;
            boolean armed = highlighted && popupOpen;
            Border emptyPadding = BorderFactory.createEmptyBorder(2, 8, 2, 8);
            label.setBorder(armed
                    ? BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(WidgetTheme.VALID_HIGHLIGHT, 2), emptyPadding)
                    : BorderFactory.createCompoundBorder(
                            BorderFactory.createEmptyBorder(2, 2, 2, 2), emptyPadding));
            label.setForeground(highlighted ? WidgetTheme.SELECTED_TEXT : WidgetTheme.NORMAL_TEXT);
            label.setBackground(highlighted ? WidgetTheme.SELECTED_HIGHLIGHT : WidgetTheme.BACKGROUND);
            label.setOpaque(true);
            actionsPanel.add(label);
        }

        footerPanel.removeAll();
        for (int i = 0; i < FOOTER_ACTIONS.size(); i++) {
            JLabel label = new JLabel(FOOTER_ACTIONS.get(i));
            label.setFont(ROW_FONT);
            label.setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 12));
            boolean highlighted = footerFocused && i == footerIndex;
            label.setForeground(highlighted ? WidgetTheme.SELECTED_TEXT : WidgetTheme.NORMAL_TEXT);
            label.setBackground(highlighted ? WidgetTheme.SELECTED_HIGHLIGHT : WidgetTheme.BACKGROUND);
            label.setOpaque(true);
            footerPanel.add(label);
            if (i < FOOTER_ACTIONS.size() - 1) {
                footerPanel.add(Box.createHorizontalStrut(20));
            }
        }

        revalidate();
        repaint();
    }

    public List<String> getAllActions() {
        return actions;
    }

    private class KeybindsKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (popupOpen) {
                pressKey(KeyEvent.getKeyText(e.getKeyCode()));
                return;
            }
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP -> moveUp();
                case KeyEvent.VK_DOWN -> moveDown();
                case KeyEvent.VK_LEFT -> moveFooterLeft();
                case KeyEvent.VK_RIGHT -> moveFooterRight();
                case KeyEvent.VK_ENTER -> confirm();
                case KeyEvent.VK_ESCAPE -> back();
                default -> {
                    // No other keys are meaningful outside the popup.
                }
            }
        }
    }
}
