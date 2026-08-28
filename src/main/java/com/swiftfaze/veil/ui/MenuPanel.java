package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.input.Keybindings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class MenuPanel extends TerminalPanel {

    private static final List<String> ITEMS = List.of(
            "I - Inventory", "H - Help", "J - Journal", "M - Map", "P - Character", "O - Stats"
    );
    private static final Color SELECTED_COLOR = Color.decode("#eeb392");
    private static final int INVENTORY_INDEX = 0;

    private final SelectableMenu menu = new SelectableMenu(ITEMS.size());
    private final JLabel[] labels = new JLabel[ITEMS.size()];
    private Runnable onInventoryConfirmed;

    public MenuPanel() {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setFocusable(true);

        for (int i = 0; i < ITEMS.size(); i++) {
            labels[i] = makeLabel(ITEMS.get(i));
            add(labels[i]);
        }

        bindKeys();
        refreshHighlight();
    }

    public void setOnInventoryConfirmed(Runnable onInventoryConfirmed) {
        this.onInventoryConfirmed = onInventoryConfirmed;
    }

    private void bindKeys() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();

        inputMap.put(Keybindings.MENU_UP, Keybindings.ACTION_MENU_UP);
        inputMap.put(Keybindings.MENU_DOWN, Keybindings.ACTION_MENU_DOWN);
        inputMap.put(Keybindings.MENU_CONFIRM, Keybindings.ACTION_MENU_CONFIRM);

        actionMap.put(Keybindings.ACTION_MENU_UP, new MoveSelectionAction(menu::moveUp));
        actionMap.put(Keybindings.ACTION_MENU_DOWN, new MoveSelectionAction(menu::moveDown));
        actionMap.put(Keybindings.ACTION_MENU_CONFIRM, new ConfirmSelectionAction());
    }

    private void refreshHighlight() {
        for (int i = 0; i < labels.length; i++) {
            labels[i].setForeground(i == menu.selected() ? SELECTED_COLOR : Color.WHITE);
        }
    }

    private void confirmSelection() {
        if (menu.selected() == INVENTORY_INDEX && onInventoryConfirmed != null) {
            onInventoryConfirmed.run();
        }
    }

    private class MoveSelectionAction extends AbstractAction {
        private final Runnable move;

        MoveSelectionAction(Runnable move) {
            this.move = move;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            move.run();
            refreshHighlight();
        }
    }

    private class ConfirmSelectionAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            confirmSelection();
        }
    }
}
