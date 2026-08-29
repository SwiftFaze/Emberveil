package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.entities.items.Item;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;

public class InventoryPanel extends TerminalPanel {

    public InventoryPanel() {
        Border bottomLine = BorderFactory.createMatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY);
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);

        setBorder(BorderFactory.createCompoundBorder(bottomLine, padding));

        add(makeLabel("Inventory"));
    }

    public void showItems(List<Item> items) {
        if (items.isEmpty()) {
            add(makeLabel("(empty)"));
        } else {
            int i = 1;
            for (Item item : items) {
                add(makeLabel(i + ". " + item.getName()));
                i++;
            }
        }
        revalidate();
        repaint();
    }
}
