package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.entities.items.Item;
import com.swiftfaze.veil.ui.widget.PopupWidget;
import com.swiftfaze.veil.ui.widget.ListWidget;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;

public class InventoryPanel extends PopupWidget {

    private final ListWidget<Item> itemList;

    public InventoryPanel() {
        Border bottomLine = BorderFactory.createMatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY);
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        setBorder(BorderFactory.createCompoundBorder(bottomLine, padding));

        JLabel titleLabel = new JLabel("Inventory");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        addContent(titleLabel);

        itemList = new ListWidget<>(Item::getName);

        JScrollPane scrollPane = new JScrollPane(itemList);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setPreferredSize(new Dimension(400, 280));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        addContent(scrollPane);
    }

    public void showItems(List<Item> items) {
        itemList.setItems(items);
    }
}
