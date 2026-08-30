package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.input.Keybindings;
import com.swiftfaze.veil.ui.widget.FocusManager;
import com.swiftfaze.veil.ui.widget.ListWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class MenuPanel extends TerminalPanel {

    private static final List<String> ITEMS = List.of(
            "I - Inventory", "H - Help", "J - Journal", "M - Map", "P - Character", "O - Stats"
    );

    private final ListWidget<String> listWidget;
    private Runnable onInventoryConfirmed;
    private Runnable onCancel;
    private FocusManager focusManager;

    public MenuPanel() {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setFocusable(true);

        listWidget = new ListWidget<>(s -> s);
        listWidget.setItems(ITEMS);
        listWidget.setOnConfirm(this::confirmSelection);
        add(listWidget);

        bindKeys();
    }

    public void setOnInventoryConfirmed(Runnable onInventoryConfirmed) {
        this.onInventoryConfirmed = onInventoryConfirmed;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public void setFocusManager(FocusManager focusManager) {
        this.focusManager = focusManager;
    }

    private boolean isBlockedByModalPopup() {
        return focusManager != null && focusManager.isPopupFocused();
    }

    private void bindKeys() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();

        inputMap.put(Keybindings.MENU_UP, Keybindings.ACTION_MENU_UP);
        inputMap.put(Keybindings.MENU_DOWN, Keybindings.ACTION_MENU_DOWN);
        inputMap.put(Keybindings.MENU_CONFIRM, Keybindings.ACTION_MENU_CONFIRM);
        inputMap.put(Keybindings.MENU_CANCEL, Keybindings.ACTION_MENU_CANCEL);

        actionMap.put(Keybindings.ACTION_MENU_UP, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isBlockedByModalPopup()) {
                    return;
                }
                listWidget.moveUp();
            }
        });

        actionMap.put(Keybindings.ACTION_MENU_DOWN, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isBlockedByModalPopup()) {
                    return;
                }
                listWidget.moveDown();
            }
        });

        actionMap.put(Keybindings.ACTION_MENU_CONFIRM, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isBlockedByModalPopup()) {
                    return;
                }
                confirmSelection(listWidget.getSelectedItem());
            }
        });

        actionMap.put(Keybindings.ACTION_MENU_CANCEL, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isBlockedByModalPopup()) {
                    return;
                }
                if (onCancel != null) {
                    onCancel.run();
                }
            }
        });
    }

    private void confirmSelection(String selected) {
        if (selected != null && selected.equals(ITEMS.get(0)) && onInventoryConfirmed != null) {
            onInventoryConfirmed.run();
        }
    }
}
