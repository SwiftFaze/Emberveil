package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.entities.items.Item;
import com.swiftfaze.veil.entities.player.classes.PlayerClass;
import com.swiftfaze.veil.input.Keybindings;
import com.swiftfaze.veil.ui.widget.ListWidget;
import com.swiftfaze.veil.ui.widget.PopupWidget;
import com.swiftfaze.veil.ui.widget.TableWidget;
import com.swiftfaze.veil.ui.widget.TerminalScrollBarUI;
import com.swiftfaze.veil.ui.widget.WidgetTheme;
import com.swiftfaze.veil.world.Tile;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * In-game reference overlay (X key): a tab switcher across Items/Tiles/Classes,
 * each tab a list+detail split mirroring InventoryPanel's own layout.
 */
public class CodexPanel extends PopupWidget {

    private record FieldRow(String field, String value) {
    }

    private record CodexEntry(String name, List<FieldRow> fields) {
    }

    public enum Category {
        ITEMS("Items"), TILES("Tiles"), CLASSES("Classes");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private enum Focus { ENTRY_LIST, FIELDS }

    private static final int BODY_HEIGHT = 280;
    private static final String NO_ENTRY_TEXT = "(no item selected)";

    private final ListWidget<CodexEntry> entryList;
    private final JPanel detailsPanel;
    private final JScrollPane detailsScrollPane;
    private final TableWidget<FieldRow> fieldsTable;
    private final List<JLabel> tabLabels = new ArrayList<>();

    private List<Item> items = List.of();
    private List<Tile> tiles = List.of();
    private List<PlayerClass> classes = List.of();
    private List<CodexEntry> currentEntries = List.of();
    private Category selectedCategory = Category.ITEMS;
    private Focus focus = Focus.ENTRY_LIST;
    private boolean showingPlaceholder;

    public CodexPanel() {
        Border bottomLine = BorderFactory.createMatteBorder(0, 0, 2, 0, WidgetTheme.BORDER);
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        setBorder(BorderFactory.createCompoundBorder(bottomLine, padding));

        addContent(makeTitleLabel());
        addContent(buildTabRow());

        entryList = new ListWidget<>(CodexEntry::name);
        entryList.setWrapAround(false);
        entryList.setOnSelectionChange(this::updateDetails);

        Border detailsDivider = BorderFactory.createMatteBorder(0, 2, 0, 0, WidgetTheme.BORDER);
        Border detailsPadding = BorderFactory.createEmptyBorder(4, 10, 0, 0);
        detailsPanel = new JPanel();
        detailsPanel.setBackground(WidgetTheme.BACKGROUND);
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(detailsDivider, detailsPadding));

        fieldsTable = new TableWidget<>(List.of("Field", "Value"), List.of(FieldRow::field, FieldRow::value));
        fieldsTable.setWrapAround(false);
        fieldsTable.setSelectable(false);
        fieldsTable.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsTable.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        detailsScrollPane = buildScrollPane(detailsPanel);
        addContent(buildBody(buildScrollPane(entryList), detailsScrollPane));
        bindTabKeys();
        disableFocusTraversalKeys();
    }

    /**
     * Without this, Java's default focus-traversal handling consumes Tab/Shift+Tab on
     * whichever component currently holds keyboard focus (the Close button, in practice)
     * before bindTabKeys()'s own InputMap bindings ever see the key event - silently
     * kicking keyboard focus out of the popup entirely instead of switching tabs.
     */
    private void disableFocusTraversalKeys() {
        setFocusTraversalKeysEnabled(false);
        getCloseButton().setFocusTraversalKeysEnabled(false);
        entryList.setFocusTraversalKeysEnabled(false);
        fieldsTable.setFocusTraversalKeysEnabled(false);
    }

    public void showItems(List<Item> items) {
        this.items = items;
        if (selectedCategory == Category.ITEMS) {
            refreshEntries();
        }
    }

    public void showTiles(List<Tile> tiles) {
        this.tiles = tiles;
        if (selectedCategory == Category.TILES) {
            refreshEntries();
        }
    }

    public void showClasses(List<PlayerClass> classes) {
        this.classes = classes;
        if (selectedCategory == Category.CLASSES) {
            refreshEntries();
        }
    }

    @Override
    public void open() {
        selectedCategory = Category.ITEMS;
        refreshEntries();
        refreshTabHighlight();
        super.open();
    }

    public Category getSelectedCategory() {
        return selectedCategory;
    }

    public List<String> getTabLabels() {
        return List.of(Category.ITEMS.getLabel(), Category.TILES.getLabel(), Category.CLASSES.getLabel());
    }

    public int getEntryCount() {
        return currentEntries.size();
    }

    public String getSelectedEntryName() {
        CodexEntry selected = entryList.getSelectedItem();
        return selected == null ? null : selected.name();
    }

    public boolean isShowingPlaceholder() {
        return showingPlaceholder;
    }

    public String getDetailPlaceholderText() {
        return showingPlaceholder ? NO_ENTRY_TEXT : null;
    }

    public TableWidget<FieldRow> getFieldsTable() {
        return fieldsTable;
    }

    public void nextTab() {
        Category[] all = Category.values();
        selectCategory(all[(selectedCategory.ordinal() + 1) % all.length]);
    }

    public void prevTab() {
        Category[] all = Category.values();
        selectCategory(all[(selectedCategory.ordinal() - 1 + all.length) % all.length]);
    }

    @Override
    protected void onUp() {
        switch (focus) {
            case ENTRY_LIST -> entryList.moveUp();
            case FIELDS -> fieldsTable.moveUp();
        }
    }

    @Override
    protected void onDown() {
        switch (focus) {
            case ENTRY_LIST -> entryList.moveDown();
            case FIELDS -> fieldsTable.moveDown();
        }
    }

    @Override
    protected void onLeft() {
        if (focus != Focus.ENTRY_LIST) {
            focus = Focus.ENTRY_LIST;
            fieldsTable.setSelectable(false);
        }
    }

    @Override
    protected void onRight() {
        if (focus == Focus.ENTRY_LIST) {
            focus = Focus.FIELDS;
            fieldsTable.setSelectable(true);
            fieldsTable.moveToStart();
        }
    }

    private void selectCategory(Category category) {
        selectedCategory = category;
        focus = Focus.ENTRY_LIST;
        fieldsTable.setSelectable(false);
        refreshTabHighlight();
        refreshEntries();
    }

    private void refreshEntries() {
        currentEntries = switch (selectedCategory) {
            case ITEMS -> items.stream().map(this::toEntry).toList();
            case TILES -> tiles.stream().map(this::toEntry).toList();
            case CLASSES -> classes.stream().map(this::toEntry).toList();
        };
        entryList.setItems(currentEntries);
    }

    private CodexEntry toEntry(Item item) {
        return new CodexEntry(item.getName(), List.of(
                new FieldRow("ID", item.getId()),
                new FieldRow("Name", item.getName()),
                new FieldRow("Glyph", String.valueOf(item.getGlyph())),
                new FieldRow("Type", item.getType()),
                new FieldRow("Slot", item.getSlot())
        ));
    }

    private CodexEntry toEntry(Tile tile) {
        Color c = tile.getColor();
        return new CodexEntry(tile.getId(), List.of(
                new FieldRow("ID", tile.getId()),
                new FieldRow("Symbol", String.valueOf(tile.getSymbol())),
                new FieldRow("Color", "rgb(" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ")"),
                new FieldRow("Walkable", String.valueOf(tile.isWalkable()))
        ));
    }

    private CodexEntry toEntry(PlayerClass playerClass) {
        return new CodexEntry(playerClass.getName(), List.of(
                new FieldRow("ID", playerClass.getId()),
                new FieldRow("Name", playerClass.getName())
        ));
    }

    private void updateDetails(CodexEntry entry) {
        detailsPanel.removeAll();
        focus = Focus.ENTRY_LIST;
        if (entry == null) {
            showingPlaceholder = true;
            detailsPanel.add(detailLabel(NO_ENTRY_TEXT));
        } else {
            showingPlaceholder = false;
            fieldsTable.setRows(entry.fields());
            fieldsTable.setSelectable(false);
            detailsPanel.add(fieldsTable);
        }
        detailsPanel.revalidate();
        detailsPanel.repaint();
        detailsScrollPane.getViewport().setViewPosition(new Point(0, 0));
    }

    private void bindTabKeys() {
        InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = getActionMap();
        inputMap.put(Keybindings.NEXT_TAB, Keybindings.ACTION_NEXT_TAB);
        inputMap.put(Keybindings.PREV_TAB, Keybindings.ACTION_PREV_TAB);
        actionMap.put(Keybindings.ACTION_NEXT_TAB, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                nextTab();
            }
        });
        actionMap.put(Keybindings.ACTION_PREV_TAB, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                prevTab();
            }
        });
    }

    private JLabel makeTitleLabel() {
        JLabel titleLabel = new JLabel("Codex");
        titleLabel.setForeground(WidgetTheme.NORMAL_TEXT);
        titleLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return titleLabel;
    }

    private JPanel buildTabRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setBackground(WidgetTheme.BACKGROUND);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        for (Category category : Category.values()) {
            JLabel label = new JLabel(category.getLabel());
            label.setOpaque(true);
            label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
            label.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            tabLabels.add(label);
            row.add(label);
        }
        return row;
    }

    private void refreshTabHighlight() {
        for (Category category : Category.values()) {
            WidgetTheme.applySelection(tabLabels.get(category.ordinal()), category == selectedCategory);
        }
    }

    private JLabel detailLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(WidgetTheme.NORMAL_TEXT);
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
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
        body.setBackground(WidgetTheme.BACKGROUND);
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.setPreferredSize(new Dimension(Integer.MAX_VALUE, BODY_HEIGHT));
        body.setMaximumSize(new Dimension(Integer.MAX_VALUE, BODY_HEIGHT));
        body.add(left);
        body.add(right);
        return body;
    }
}
