package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.entities.items.Item;
import com.swiftfaze.veil.input.Keybindings;
import com.swiftfaze.veil.ui.widget.ListWidget;
import com.swiftfaze.veil.ui.widget.PopupWidget;
import com.swiftfaze.veil.ui.widget.TableWidget;
import com.swiftfaze.veil.ui.widget.TerminalScrollBarUI;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class InventoryPanel extends PopupWidget {

    private static final int BODY_HEIGHT = 280;

    private final ListWidget<Item> itemList;
    private final JPanel detailsPanel;
    private final TableWidget<Item.Effect> effectsTable;
    private final DropConfirmationPopup dropConfirmationPopup;
    private boolean effectsTableHasFocus = false;

    public InventoryPanel() {
        Border bottomLine = BorderFactory.createMatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY);
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        setBorder(BorderFactory.createCompoundBorder(bottomLine, padding));

        addContent(makeTitleLabel());

        itemList = new ListWidget<>(Item::getName);
        itemList.setWrapAround(false);
        itemList.setOnSelectionChange(this::updateDetails);

        Border detailsDivider = BorderFactory.createMatteBorder(0, 2, 0, 0, Color.LIGHT_GRAY);
        Border detailsPadding = BorderFactory.createEmptyBorder(0, 10, 0, 0);
        detailsPanel = new JPanel();
        detailsPanel.setBackground(Color.BLACK);
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(detailsDivider, detailsPadding));

        effectsTable = new TableWidget<>(List.of(
            e -> "+" + e.stat(),
            e -> "(" + e.calc() + ")"
        ));
        effectsTable.setWrapAround(true);

        dropConfirmationPopup = new DropConfirmationPopup();
        dropConfirmationPopup.setOnDismiss(() -> getCloseButton().requestFocusInWindow());

        addContent(buildBody(buildScrollPane(itemList), detailsPanel));
        bindDropKey();
    }

    public void showItems(List<Item> items) {
        itemList.setItems(items);
    }

    public int getSelectedIndex() {
        return itemList.getSelectedIndex();
    }

    public Item getSelectedItem() {
        return itemList.getSelectedItem();
    }

    public DropConfirmationPopup getDropConfirmationPopup() {
        return dropConfirmationPopup;
    }

    public boolean isEffectsTableFocused() {
        return effectsTableHasFocus;
    }

    @Override
    protected void onUp() {
        if (effectsTableHasFocus) {
            effectsTable.moveUp();
        } else {
            itemList.moveUp();
        }
    }

    @Override
    protected void onDown() {
        if (effectsTableHasFocus) {
            effectsTable.moveDown();
        } else {
            itemList.moveDown();
        }
    }

    @Override
    protected void onLeft() {
        if (effectsTableHasFocus) {
            effectsTableHasFocus = false;
        }
    }

    @Override
    protected void onRight() {
        if (!effectsTableHasFocus && effectsTable.getSelectedRow() != null) {
            effectsTableHasFocus = true;
        }
    }

    private JLabel makeTitleLabel() {
        JLabel titleLabel = new JLabel("Inventory");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return titleLabel;
    }

    private JScrollPane buildScrollPane(JComponent view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new TerminalScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        return scrollPane;
    }

    private JPanel buildBody(JComponent left, JComponent right) {
        JPanel body = new JPanel(new GridLayout(1, 2, 20, 0));
        body.setBackground(Color.BLACK);
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.setPreferredSize(new Dimension(Integer.MAX_VALUE, BODY_HEIGHT));
        body.setMaximumSize(new Dimension(Integer.MAX_VALUE, BODY_HEIGHT));
        body.add(left);
        body.add(right);
        return body;
    }

    private void updateDetails(Item item) {
        detailsPanel.removeAll();
        effectsTableHasFocus = false;
        for (String line : detailLines(item)) {
            detailsPanel.add(detailLabel(line));
        }
        effectsTable.setRows(item == null ? List.of() : item.getEffects());
        detailsPanel.add(effectsTable);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private List<String> detailLines(Item item) {
        if (item == null) {
            return List.of("(no item selected)");
        }
        List<String> lines = new java.util.ArrayList<>(List.of(
                item.getName(),
                "Type: " + item.getType(),
                "Slot: " + item.getSlot()
        ));
        Item.BaseDamage damage = item.getBaseDamage();
        if (damage.max() > 0) {
            lines.add("Damage: " + damage.min() + "-" + damage.max());
        }
        return lines;
    }

    private JLabel detailLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void bindDropKey() {
        InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = getActionMap();
        inputMap.put(Keybindings.DROP_ITEM, Keybindings.ACTION_DROP_ITEM);
        actionMap.put(Keybindings.ACTION_DROP_ITEM, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (itemList.getSelectedItem() != null) {
                    dropConfirmationPopup.open();
                }
            }
        });
    }
}
