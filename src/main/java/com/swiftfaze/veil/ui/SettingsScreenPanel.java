package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.input.Keybindings;
import com.swiftfaze.veil.ui.widget.RadioGroupWidget;
import com.swiftfaze.veil.ui.widget.SliderWidget;
import com.swiftfaze.veil.ui.widget.WidgetTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SettingsScreenPanel extends JPanel {
    private static final Font ROW_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 16);

    private final List<SettingsRow> rows;
    private final JPanel rowsPanel;
    private int selectedIndex = 0;
    private final Consumer<String> onBack;
    private final Consumer<String> onOpenFolder;

    private static class SettingsRow {
        String name;
        Object widget; // SliderWidget, RadioGroupWidget<String>, or null for actions

        SettingsRow(String name, Object widget) {
            this.name = name;
            this.widget = widget;
        }

        String displayValue() {
            if (widget instanceof SliderWidget slider) {
                return slider.getDisplayText();
            } else if (widget instanceof RadioGroupWidget<?> radio) {
                return String.valueOf(radio.getHighlightedOption());
            }
            return "";
        }
    }

    public SettingsScreenPanel(Consumer<String> onBack, Consumer<String> onOpenFolder) {
        this.onBack = onBack;
        this.onOpenFolder = onOpenFolder;
        this.rows = new ArrayList<>();

        setBackground(Color.BLACK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2),
                BorderFactory.createEmptyBorder(20, 40, 20, 40)));
        setFocusable(true);

        JLabel header = new JLabel("Settings");
        header.setForeground(Color.WHITE);
        header.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        rowsPanel = new JPanel();
        rowsPanel.setBackground(Color.BLACK);
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        rowsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalGlue());
        add(header);
        add(Box.createVerticalStrut(20));
        add(rowsPanel);
        add(Box.createVerticalGlue());

        initializeRows();
        refresh();
        bindKeys();
    }

    private void bindKeys() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();

        inputMap.put(Keybindings.MENU_UP, "settings-up");
        inputMap.put(Keybindings.MENU_DOWN, "settings-down");
        inputMap.put(Keybindings.MENU_LEFT, "settings-left");
        inputMap.put(Keybindings.MENU_RIGHT, "settings-right");
        inputMap.put(Keybindings.MENU_CONFIRM, "settings-confirm");
        inputMap.put(Keybindings.MENU_CANCEL, "settings-cancel");

        actionMap.put("settings-up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveUp();
            }
        });
        actionMap.put("settings-down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveDown();
            }
        });
        actionMap.put("settings-left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveLeft();
            }
        });
        actionMap.put("settings-right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveRight();
            }
        });
        actionMap.put("settings-confirm", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirm();
            }
        });
        actionMap.put("settings-cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                back();
            }
        });
    }

    private void initializeRows() {
        rows.clear();

        rows.add(new SettingsRow("Brightness", new SliderWidget(0, 10, 1, 5)));

        RadioGroupWidget<String> fullscreenRadio = new RadioGroupWidget<>(s -> s, true);
        fullscreenRadio.setOptions(List.of("Windowed", "Fullscreen"));
        fullscreenRadio.selectOption(0);
        rows.add(new SettingsRow("Fullscreen", fullscreenRadio));

        RadioGroupWidget<String> fontRadio = new RadioGroupWidget<>(s -> s, true);
        fontRadio.setOptions(List.of("Monospaced", "Serif", "SansSerif"));
        fontRadio.selectOption(0);
        rows.add(new SettingsRow("Font", fontRadio));

        rows.add(new SettingsRow("Volume", new SliderWidget(0, 10, 1, 5)));
        rows.add(new SettingsRow("Keybinds", null));
        rows.add(new SettingsRow("Open Game Folder", null));
        rows.add(new SettingsRow("Open Mod Folder", null));
        rows.add(new SettingsRow("About", null));
        rows.add(new SettingsRow("Reset to Defaults", null));
    }

    public String getHighlightedItemName() {
        return rows.get(selectedIndex).name;
    }

    public void moveUp() {
        if (selectedIndex > 0) {
            selectedIndex--;
            refresh();
        }
    }

    public void moveDown() {
        if (selectedIndex < rows.size() - 1) {
            selectedIndex++;
            refresh();
        }
    }

    public void moveLeft() {
        SettingsRow row = rows.get(selectedIndex);
        if (row.widget instanceof SliderWidget slider) {
            slider.moveLeft();
        } else if (row.widget instanceof RadioGroupWidget<?> radio) {
            radio.moveLeft();
        }
        refresh();
    }

    public void moveRight() {
        SettingsRow row = rows.get(selectedIndex);
        if (row.widget instanceof SliderWidget slider) {
            slider.moveRight();
        } else if (row.widget instanceof RadioGroupWidget<?> radio) {
            radio.moveRight();
        }
        refresh();
    }

    public void confirm() {
        SettingsRow row = rows.get(selectedIndex);
        switch (row.name) {
            case "Keybinds" -> onBack.accept("keybinds");
            case "Open Game Folder" -> onOpenFolder.accept("game");
            case "Open Mod Folder" -> {
                onOpenFolder.accept("mods");
            }
            case "About", "Reset to Defaults" -> {
                // Placeholder actions
            }
        }
    }

    public void back() {
        onBack.accept("title");
    }

    public String getItemValue(String itemName) {
        for (SettingsRow row : rows) {
            if (row.name.equals(itemName)) {
                return row.displayValue();
            }
        }
        return "";
    }

    public int getSliderValue(String itemName) {
        for (SettingsRow row : rows) {
            if (row.name.equals(itemName) && row.widget instanceof SliderWidget slider) {
                return slider.getValue();
            }
        }
        return 0;
    }

    public String getRadioValue(String itemName) {
        for (SettingsRow row : rows) {
            if (row.name.equals(itemName) && row.widget instanceof RadioGroupWidget<?> radio) {
                return String.valueOf(radio.getHighlightedOption());
            }
        }
        return "";
    }

    private void refresh() {
        rowsPanel.removeAll();
        for (int i = 0; i < rows.size(); i++) {
            SettingsRow row = rows.get(i);
            String text = row.widget == null ? row.name : row.name + ": " + row.displayValue();
            JLabel label = new JLabel(text);
            label.setFont(ROW_FONT);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            label.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            label.setForeground(i == selectedIndex ? WidgetTheme.SELECTED_TEXT : WidgetTheme.NORMAL_TEXT);
            label.setBackground(i == selectedIndex ? WidgetTheme.SELECTED_HIGHLIGHT : WidgetTheme.BACKGROUND);
            label.setOpaque(true);
            rowsPanel.add(label);
        }
        revalidate();
        repaint();
    }

    public List<String> getAllItemNames() {
        List<String> names = new ArrayList<>();
        for (SettingsRow row : rows) {
            names.add(row.name);
        }
        return names;
    }
}
