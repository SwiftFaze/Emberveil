package com.swiftfaze.veil.ui.widget;

import com.swiftfaze.veil.input.Keybindings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ListWidget<T> extends Widget {

    private final List<T> items = new ArrayList<>();
    private final Function<T, String> itemRenderer;
    private int selectedIndex = 0;
    private Consumer<T> onConfirm = t -> {
    };
    private final List<JLabel> labels = new ArrayList<>();

    public ListWidget(Function<T, String> itemRenderer) {
        this.itemRenderer = itemRenderer;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        bindKeys();
    }

    public void setItems(List<T> items) {
        this.items.clear();
        this.items.addAll(items);
        this.selectedIndex = 0;
        refresh();
    }

    public void setOnConfirm(Consumer<T> onConfirm) {
        this.onConfirm = onConfirm;
    }

    public T getSelectedItem() {
        return items.isEmpty() ? null : items.get(selectedIndex);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void moveUp() {
        if (!items.isEmpty()) {
            selectedIndex = (selectedIndex - 1 + items.size()) % items.size();
            refreshHighlight();
        }
    }

    public void moveDown() {
        if (!items.isEmpty()) {
            selectedIndex = (selectedIndex + 1) % items.size();
            refreshHighlight();
        }
    }

    private void bindKeys() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();

        inputMap.put(Keybindings.MENU_UP, "list-up");
        inputMap.put(Keybindings.MENU_DOWN, "list-down");
        inputMap.put(Keybindings.MENU_CONFIRM, "list-confirm");

        actionMap.put("list-up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveUp();
            }
        });

        actionMap.put("list-down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveDown();
            }
        });

        actionMap.put("list-confirm", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                T selected = getSelectedItem();
                if (selected != null) {
                    onConfirm.accept(selected);
                }
            }
        });
    }

    private void refresh() {
        removeAll();
        labels.clear();

        for (T item : items) {
            JLabel label = new JLabel(itemRenderer.apply(item));
            label.setForeground(WidgetTheme.NORMAL_TEXT);
            label.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 16));
            label.setAlignmentX(LEFT_ALIGNMENT);
            labels.add(label);
            add(label);
        }

        refreshHighlight();
        revalidate();
        repaint();
    }

    private void refreshHighlight() {
        for (int i = 0; i < labels.size(); i++) {
            labels.get(i).setForeground(
                    i == selectedIndex ? WidgetTheme.SELECTED_HIGHLIGHT : WidgetTheme.NORMAL_TEXT
            );
        }
    }
}
