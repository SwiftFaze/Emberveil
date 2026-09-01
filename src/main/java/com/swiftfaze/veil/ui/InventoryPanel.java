package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.entities.items.Item;
import com.swiftfaze.veil.input.Keybindings;
import com.swiftfaze.veil.ui.widget.ListWidget;
import com.swiftfaze.veil.ui.widget.PopupWidget;
import com.swiftfaze.veil.ui.widget.TableWidget;
import com.swiftfaze.veil.ui.widget.WidgetTheme;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class InventoryPanel extends PopupWidget {

    private record FieldRow(String field, String value) {
    }

    /**
     * Which region of the popup currently owns Up/Down (real Swing focus always stays on the
     * inherited Close button — see PopupWidget). The details pane (FIELDS, then EFFECTS) is one
     * continuous region: Down falls off the end of one table into the start of the next, Up does
     * the reverse, and Left from either always exits straight back to ITEM_LIST.
     */
    private enum Focus { ITEM_LIST, FIELDS, EFFECTS }

    private final ListWidget<Item> itemList;
    private final JPanel detailsPanel;
    private final JScrollPane detailsScrollPane;
    private final TableWidget<FieldRow> fieldsTable;
    private final JLabel effectsLabel;
    private final TableWidget<Item.Effect> effectsTable;
    private final DropConfirmationPopup dropConfirmationPopup;
    private Focus focus = Focus.ITEM_LIST;

    public InventoryPanel() {
        Border bottomLine = BorderFactory.createMatteBorder(0, 0, 2, 0, WidgetTheme.BORDER);
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        setBorder(BorderFactory.createCompoundBorder(bottomLine, padding));

        addContent(makeTitleLabel());

        itemList = new ListWidget<>(Item::getName);
        itemList.setWrapAround(false);
        itemList.setOnSelectionChange(this::updateDetails);

        detailsPanel = ListDetailLayoutUtility.buildDetailsPanel();

        fieldsTable = new TableWidget<>(List.of("Field", "Value"), List.of(FieldRow::field, FieldRow::value));
        ListDetailLayoutUtility.configureDetailsTable(fieldsTable);

        effectsLabel = ListDetailLayoutUtility.makeEffectsLabel();
        effectsTable = ListDetailLayoutUtility.buildEffectsTable();

        dropConfirmationPopup = new DropConfirmationPopup();
        dropConfirmationPopup.setOnDismiss(this::requestFocusInWindow);

        detailsScrollPane = ListDetailLayoutUtility.buildScrollPane(detailsPanel);
        addContent(ListDetailLayoutUtility.buildBody(ListDetailLayoutUtility.buildScrollPane(itemList), detailsScrollPane));
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

    public TableWidget<FieldRow> getFieldsTable() {
        return fieldsTable;
    }

    public TableWidget<Item.Effect> getEffectsTable() {
        return effectsTable;
    }

    public DropConfirmationPopup getDropConfirmationPopup() {
        return dropConfirmationPopup;
    }

    public boolean isFieldsTableFocused() {
        return focus == Focus.FIELDS;
    }

    public boolean isEffectsTableFocused() {
        return focus == Focus.EFFECTS;
    }

    public boolean isItemListFocused() {
        return focus == Focus.ITEM_LIST;
    }

    @Override
    protected void onUp() {
        switch (focus) {
            case ITEM_LIST -> itemList.moveUp();
            case FIELDS -> fieldsTable.moveUp();
            case EFFECTS -> {
                if (effectsTable.isAtFirstRow()) {
                    enterFields();
                    fieldsTable.moveToEnd();
                } else {
                    effectsTable.moveUp();
                }
            }
        }
    }

    @Override
    protected void onDown() {
        switch (focus) {
            case ITEM_LIST -> itemList.moveDown();
            case FIELDS -> {
                if (fieldsTable.isAtLastRow()) {
                    if (effectsTable.getRowCount() > 0) {
                        enterEffects();
                        effectsTable.moveToStart();
                    }
                } else {
                    fieldsTable.moveDown();
                }
            }
            case EFFECTS -> effectsTable.moveDown();
        }
    }

    @Override
    protected void onLeft() {
        if (focus != Focus.ITEM_LIST) {
            focus = Focus.ITEM_LIST;
            fieldsTable.setSelectable(false);
            effectsTable.setSelectable(false);
        }
    }

    @Override
    protected void onRight() {
        if (focus == Focus.ITEM_LIST) {
            enterFields();
            fieldsTable.moveToStart();
        }
    }

    private void enterFields() {
        focus = Focus.FIELDS;
        fieldsTable.setSelectable(true);
        effectsTable.setSelectable(false);
    }

    private void enterEffects() {
        focus = Focus.EFFECTS;
        fieldsTable.setSelectable(false);
        effectsTable.setSelectable(true);
    }

    private JLabel makeTitleLabel() {
        JLabel titleLabel = new JLabel("Inventory");
        titleLabel.setForeground(WidgetTheme.NORMAL_TEXT);
        titleLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return titleLabel;
    }

    private void updateDetails(Item item) {
        detailsPanel.removeAll();
        focus = Focus.ITEM_LIST;
        if (item == null) {
            detailsPanel.add(detailLabel("(no item selected)"));
        } else {
            fieldsTable.setRows(fieldRows(item));
            fieldsTable.setSelectable(false);
            effectsTable.setRows(item.getEffects());
            effectsTable.setSelectable(false);
            detailsPanel.add(fieldsTable);
            detailsPanel.add(effectsLabel);
            detailsPanel.add(effectsTable);
        }
        detailsPanel.revalidate();
        detailsPanel.repaint();
        // Reset the viewport to the top on every rebuild — otherwise switching items while
        // scrolled down leaves the new item's content (and its header row) starting mid-scroll,
        // since JScrollPane doesn't do this automatically when its view's content changes.
        detailsScrollPane.getViewport().setViewPosition(new Point(0, 0));
    }

    private List<FieldRow> fieldRows(Item item) {
        List<FieldRow> rows = new java.util.ArrayList<>(List.of(
                new FieldRow("ID", item.getId()),
                new FieldRow("Name", item.getName()),
                new FieldRow("Glyph", String.valueOf(item.getGlyph())),
                new FieldRow("Type", item.getType()),
                new FieldRow("Slot", item.getSlot())
        ));
        Item.BaseDamage damage = item.getBaseDamage();
        if (damage.max() > 0) {
            rows.add(new FieldRow("Base Damage (Min)", String.valueOf(damage.min())));
            rows.add(new FieldRow("Base Damage (Max)", String.valueOf(damage.max())));
        }
        return rows;
    }

    private JLabel detailLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(WidgetTheme.NORMAL_TEXT);
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
                Item selected = itemList.getSelectedItem();
                if (selected != null) {
                    dropConfirmationPopup.open(selected.getName());
                }
            }
        });
    }
}
