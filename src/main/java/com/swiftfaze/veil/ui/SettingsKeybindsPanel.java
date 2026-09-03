package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.ui.widget.ControlsHintBarWidget;
import com.swiftfaze.veil.ui.widget.TableWidget;
import com.swiftfaze.veil.ui.widget.WidgetTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SettingsKeybindsPanel extends JPanel implements HintAware {
    private static final Font ROW_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 16);
    private static final List<String> FOOTER_ACTIONS = List.of("Go back", "Reset to Defaults", "Cancel", "Apply");
    private static final List<ControlsHintBarWidget.Hint> TABLE_HINTS =
            List.of(new ControlsHintBarWidget.Hint("enter", "Rebind"), new ControlsHintBarWidget.Hint("escape", "Back"));
    private static final List<ControlsHintBarWidget.Hint> FOOTER_HINTS = List.of(
            new ControlsHintBarWidget.Hint("up", "Back to table"),
            new ControlsHintBarWidget.Hint("left", "Previous"),
            new ControlsHintBarWidget.Hint("right", "Next"),
            new ControlsHintBarWidget.Hint("enter", "Select"),
            new ControlsHintBarWidget.Hint("escape", "Back"));
    private static final List<ControlsHintBarWidget.Hint> CAPTURE_HINTS =
            List.of(new ControlsHintBarWidget.Hint("any key", "Set binding"));

    private record ActionRow(String action, String key) {
    }

    private final List<String> actions;
    private final Map<String, String> keyBindings;
    private final Consumer<String> onBack;
    private final ControlsHintBarWidget hintBar;
    private final TableWidget<ActionRow> actionsTable;
    private final JPanel footerPanel;

    private boolean footerFocused = false;
    private int footerIndex = 0;
    private boolean popupOpen = false;

    public SettingsKeybindsPanel(Consumer<String> onBack, ControlsHintBarWidget hintBar) {
        this.onBack = onBack;
        this.hintBar = hintBar;
        this.actions = List.of("Move up", "Move down", "Move left", "Move right", "Toggle inventory");
        this.keyBindings = new HashMap<>();

        setBackground(WidgetTheme.BACKGROUND);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WidgetTheme.BORDER, 2),
                BorderFactory.createEmptyBorder(20, 40, 20, 40)));
        setFocusable(true);

        JLabel header = new JLabel("Keybinds");
        header.setForeground(WidgetTheme.NORMAL_TEXT);
        header.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        actionsTable = new TableWidget<>(List.of("Action", "Key"), List.of(ActionRow::action, ActionRow::key));
        actionsTable.setWrapAround(false);
        actionsTable.setAlignmentX(Component.CENTER_ALIGNMENT);

        footerPanel = new JPanel();
        footerPanel.setBackground(WidgetTheme.BACKGROUND);
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.X_AXIS));
        footerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        add(Box.createVerticalGlue());
        add(header);
        add(Box.createVerticalStrut(20));
        add(actionsTable);
        add(footerPanel);
        add(Box.createVerticalGlue());

        setDefaultBindings();
        actionsTable.setRows(buildRows());
        refreshFooter();
        addKeyListener(new KeybindsKeyListener());
        refreshHints();
    }

    private void setDefaultBindings() {
        keyBindings.put("Move up", "Up");
        keyBindings.put("Move down", "Down");
        keyBindings.put("Move left", "Left");
        keyBindings.put("Move right", "Right");
        keyBindings.put("Toggle inventory", "I");
    }

    private List<ActionRow> buildRows() {
        List<ActionRow> rows = new ArrayList<>();
        for (String action : actions) {
            rows.add(new ActionRow(action, keyBindings.getOrDefault(action, "")));
        }
        return rows;
    }

    /**
     * Restores every action's default binding without disturbing the table's current
     * selection - unlike setRows(), which always resets selection to the first row, and
     * would be visible the next time Up leaves the footer back into the action list.
     */
    private void resetBindingsToDefaults() {
        setDefaultBindings();
        for (int i = 0; i < actions.size(); i++) {
            String action = actions.get(i);
            actionsTable.updateRow(i, new ActionRow(action, keyBindings.get(action)));
        }
    }

    public String getHighlightedActionName() {
        ActionRow row = actionsTable.getSelectedRow();
        return row == null ? "" : row.action();
    }

    public void moveUp() {
        if (footerFocused) {
            footerFocused = false;
            actionsTable.setSelectable(true);
        } else {
            actionsTable.moveUp();
        }
        refreshFooter();
        refreshHints();
    }

    public void moveDown() {
        if (footerFocused) {
            return;
        }
        if (actionsTable.isAtLastRow()) {
            footerFocused = true;
            footerIndex = 0;
            actionsTable.setSelectable(false);
        } else {
            actionsTable.moveDown();
        }
        refreshFooter();
        refreshHints();
    }

    public void moveFooterLeft() {
        if (footerFocused && footerIndex > 0) {
            footerIndex--;
            refreshFooter();
            refreshHints();
        }
    }

    public void moveFooterRight() {
        if (footerFocused && footerIndex < FOOTER_ACTIONS.size() - 1) {
            footerIndex++;
            refreshFooter();
            refreshHints();
        }
    }

    public void confirm() {
        if (!footerFocused) {
            popupOpen = true;
            applyArmedStyle();
        } else if ("Reset to Defaults".equals(getHighlightedFooterAction())) {
            resetBindingsToDefaults();
        } else {
            onBack.accept("settings");
        }
        refreshHints();
    }

    public void back() {
        onBack.accept("settings");
    }

    public String getKeyForAction(String action) {
        return keyBindings.getOrDefault(action, "");
    }

    public void updateKeyForAction(String action, String key) {
        keyBindings.put(action, key);
        actionsTable.updateRow(actions.indexOf(action), new ActionRow(action, key));
        popupOpen = false;
        applyArmedStyle();
        refreshHints();
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
        actionsTable.setSelectable(false);
        refreshFooter();
        refreshHints();
    }

    public void pressKey(String key) {
        if (popupOpen) {
            updateKeyForAction(getHighlightedActionName(), key);
        }
    }

    /**
     * Flags the selected action row as "armed" (green accent border, every other row
     * dimmed) while its press-any-key popup is open, mirroring RadioGroupWidget's
     * confirmed-option border convention.
     */
    private void applyArmedStyle() {
        actionsTable.setSelectedRowAccentColor(popupOpen ? WidgetTheme.VALID_HIGHLIGHT : null);
        actionsTable.setOtherRowsDimmed(popupOpen);
    }

    private void refreshFooter() {
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

    private List<ControlsHintBarWidget.Hint> computeHints() {
        if (popupOpen) {
            return CAPTURE_HINTS;
        }
        return footerFocused ? FOOTER_HINTS : TABLE_HINTS;
    }

    @Override
    public void refreshHints() {
        hintBar.setHints(computeHints());
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
            handleNavigationKey(e.getKeyCode());
        }

        private void handleNavigationKey(int keyCode) {
            switch (keyCode) {
                case KeyEvent.VK_UP -> moveUp();
                case KeyEvent.VK_DOWN -> moveDown();
                case KeyEvent.VK_LEFT -> moveFooterLeft();
                case KeyEvent.VK_RIGHT -> moveFooterRight();
                case KeyEvent.VK_ENTER -> confirm();
                case KeyEvent.VK_ESCAPE -> back();
            }
        }
    }
}
