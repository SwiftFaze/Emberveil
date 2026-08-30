package com.swiftfaze.veil.ui.widget;

import com.swiftfaze.veil.input.Keybindings;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A keyboard-navigable, full-width table with bordered grid cells (like a
 * terminal-rendered markdown table), an optional header row, and an optional
 * non-selectable mode for purely static/display data.
 */
public class TableWidget<T> extends Widget {
    private final List<String> columnHeaders;
    private final List<Function<T, String>> columnRenderers;
    private final List<T> rows = new ArrayList<>();
    private final List<List<JLabel>> rowCells = new ArrayList<>();
    private JPanel headerPanel;
    private int selectedRowIndex = 0;
    private int selectedColumnIndex = 0;
    private boolean wrapAround = true;
    private boolean selectable = true;
    private Consumer<T> onConfirm = t -> {};
    private Border selectedRowAccentBorder;
    private boolean otherRowsDimmed = false;

    public TableWidget(List<Function<T, String>> columnRenderers) {
        this(List.of(), columnRenderers);
    }

    public TableWidget(List<String> columnHeaders, List<Function<T, String>> columnRenderers) {
        this.columnHeaders = columnHeaders;
        this.columnRenderers = columnRenderers;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(LEFT_ALIGNMENT);
        setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, WidgetTheme.TABLE_BORDER));
        buildHeaderRow();
        bindKeys();
    }

    public void setRows(List<T> rows) {
        this.rows.clear();
        this.rows.addAll(rows);
        this.selectedRowIndex = 0;
        this.selectedColumnIndex = 0;
        refresh();
    }

    public void setWrapAround(boolean wrapAround) {
        this.wrapAround = wrapAround;
    }

    /**
     * A non-selectable table renders every cell in NORMAL_TEXT, with no row-highlight
     * indication — for a table that isn't currently the keyboard-navigation target (either
     * purely static data, or momentarily not the active pane in a multi-table details view).
     */
    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
        refreshHighlight();
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setOnConfirm(Consumer<T> onConfirm) {
        this.onConfirm = onConfirm;
    }

    /**
     * An extra border drawn around the selected row's cells, on top of the normal
     * selected-row highlight - for a consumer that needs to flag the selected row as
     * additionally "armed" for some other in-progress action (e.g. a popup open on its
     * behalf), distinct from just being the current cursor position. Null (the default)
     * draws no extra border, matching every consumer that doesn't need this.
     */
    public void setSelectedRowAccentBorder(Border border) {
        this.selectedRowAccentBorder = border;
        refreshHighlight();
    }

    /**
     * When true, every row except the selected one renders in WidgetTheme.DIMMED_TEXT
     * instead of NORMAL_TEXT - pairs with setSelectedRowAccentBorder() to make the
     * accented row read as the only currently-active thing, like a modal dimming its
     * backdrop. False (the default) leaves every consumer's existing look unchanged.
     */
    public void setOtherRowsDimmed(boolean dimmed) {
        this.otherRowsDimmed = dimmed;
        refreshHighlight();
    }

    /**
     * Replaces one row's data and re-renders just its cells, preserving the current
     * selection - unlike setRows(), which always resets selection to the first row.
     * For a consumer whose row data can change in place (e.g. a live-editable value)
     * without the row set itself being rebuilt.
     */
    public void updateRow(int index, T newRow) {
        if (index < 0 || index >= rows.size()) {
            return;
        }
        rows.set(index, newRow);
        List<JLabel> cells = rowCells.get(index);
        for (int i = 0; i < columnRenderers.size(); i++) {
            cells.get(i).setText(columnRenderers.get(i).apply(newRow));
        }
        revalidate();
        repaint();
    }

    public T getSelectedRow() {
        return rows.isEmpty() ? null : rows.get(selectedRowIndex);
    }

    public int getSelectedRowIndex() {
        return selectedRowIndex;
    }

    public int getSelectedColumnIndex() {
        return selectedColumnIndex;
    }

    public int getRowCount() {
        return rows.size();
    }

    public void moveToStart() {
        if (rows.isEmpty()) return;
        selectedRowIndex = 0;
        refreshHighlight();
    }

    public void moveToEnd() {
        if (rows.isEmpty()) return;
        selectedRowIndex = rows.size() - 1;
        refreshHighlight();
    }

    public boolean isAtFirstRow() {
        return selectedRowIndex == 0;
    }

    public boolean isAtLastRow() {
        return rows.isEmpty() || selectedRowIndex == rows.size() - 1;
    }

    public void moveUp() {
        if (rows.isEmpty()) return;
        selectedRowIndex = wrapAround
            ? (selectedRowIndex - 1 + rows.size()) % rows.size()
            : Math.max(0, selectedRowIndex - 1);
        refreshHighlight();
    }

    public void moveDown() {
        if (rows.isEmpty()) return;
        selectedRowIndex = wrapAround
            ? (selectedRowIndex + 1) % rows.size()
            : Math.min(rows.size() - 1, selectedRowIndex + 1);
        refreshHighlight();
    }

    public void moveLeft() {
        if (rows.isEmpty() || columnRenderers.isEmpty()) return;
        selectedColumnIndex = wrapAround
            ? (selectedColumnIndex - 1 + columnRenderers.size()) % columnRenderers.size()
            : Math.max(0, selectedColumnIndex - 1);
        refreshHighlight();
    }

    public void moveRight() {
        if (rows.isEmpty() || columnRenderers.isEmpty()) return;
        selectedColumnIndex = wrapAround
            ? (selectedColumnIndex + 1) % columnRenderers.size()
            : Math.min(columnRenderers.size() - 1, selectedColumnIndex + 1);
        refreshHighlight();
    }

    private void bindKeys() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();
        inputMap.put(Keybindings.MENU_UP, "table-up");
        inputMap.put(Keybindings.MENU_DOWN, "table-down");
        inputMap.put(Keybindings.MENU_LEFT, "table-left");
        inputMap.put(Keybindings.MENU_RIGHT, "table-right");
        inputMap.put(Keybindings.MENU_CONFIRM, "table-confirm");
        actionMap.put("table-up", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { moveUp(); } });
        actionMap.put("table-down", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { moveDown(); } });
        actionMap.put("table-left", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { moveLeft(); } });
        actionMap.put("table-right", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { moveRight(); } });
        actionMap.put("table-confirm", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                T selected = getSelectedRow();
                if (selected != null) onConfirm.accept(selected);
            }
        });
    }

    private void buildHeaderRow() {
        if (columnHeaders.isEmpty()) {
            return;
        }
        headerPanel = buildRowPanel(columnHeaders, true);
        add(headerPanel);
    }

    private void refresh() {
        for (List<JLabel> cells : rowCells) {
            // Each row's panel is the shared parent of its cells; remove it once per row.
            remove(cells.get(0).getParent());
        }
        rowCells.clear();
        for (T row : rows) {
            List<String> cellText = new ArrayList<>();
            for (Function<T, String> renderer : columnRenderers) {
                cellText.add(renderer.apply(row));
            }
            JPanel rowPanel = buildRowPanel(cellText, false);
            List<JLabel> cells = new ArrayList<>();
            for (var component : rowPanel.getComponents()) {
                cells.add((JLabel) component);
            }
            rowCells.add(cells);
            add(rowPanel);
        }
        refreshHighlight();
        revalidate();
        repaint();
    }

    private JPanel buildRowPanel(List<String> cellText, boolean isHeader) {
        int columnCount = Math.max(1, cellText.size());
        JPanel rowPanel = new JPanel(new GridLayout(1, columnCount));
        rowPanel.setAlignmentX(LEFT_ALIGNMENT);
        rowPanel.setBackground(isHeader ? WidgetTheme.TABLE_HEADER_BACKGROUND : WidgetTheme.BACKGROUND);
        for (String text : cellText) {
            rowPanel.add(buildCellLabel(text, isHeader));
        }
        // Computed after the cells are added — an empty panel's preferred height is ~0, which
        // would otherwise clamp every row (including the header) to zero visible height.
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowPanel.getPreferredSize().height));
        return rowPanel;
    }

    private JLabel buildCellLabel(String text, boolean isHeader) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(isHeader ? WidgetTheme.TABLE_HEADER_BACKGROUND : WidgetTheme.BACKGROUND);
        label.setForeground(WidgetTheme.NORMAL_TEXT);
        label.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, isHeader ? java.awt.Font.BOLD : java.awt.Font.PLAIN, 16));
        label.setBorder(baseCellBorder());
        return label;
    }

    private Border baseCellBorder() {
        Border cellLine = BorderFactory.createMatteBorder(0, 0, 1, 1, WidgetTheme.TABLE_BORDER);
        Border padding = BorderFactory.createEmptyBorder(4, 8, 4, 8);
        return BorderFactory.createCompoundBorder(cellLine, padding);
    }

    private void refreshHighlight() {
        for (int i = 0; i < rowCells.size(); i++) {
            boolean highlighted = selectable && i == selectedRowIndex;
            boolean accented = highlighted && selectedRowAccentBorder != null;
            for (JLabel cell : rowCells.get(i)) {
                WidgetTheme.applySelection(cell, highlighted);
                if (accented) {
                    cell.setBorder(BorderFactory.createCompoundBorder(selectedRowAccentBorder, baseCellBorder()));
                } else {
                    cell.setBorder(baseCellBorder());
                    if (otherRowsDimmed && !highlighted) {
                        cell.setForeground(WidgetTheme.DIMMED_TEXT);
                    }
                }
            }
        }
        if (selectable && selectedRowIndex < rowCells.size() && !rowCells.isEmpty()) {
            // scrollRectToVisible scrolls the *minimum* distance needed to reveal exactly the
            // rectangle it's given. The header is a separate component sitting above row 0 that
            // it knows nothing about, so scrolling to reveal row 0 alone can (and did, in
            // practice) leave the header scrolled just out of view above it. Unioning the
            // header's bounds into the target whenever row 0 is selected forces the header along.
            java.awt.Rectangle target = rowCells.get(selectedRowIndex).get(0).getParent().getBounds();
            if (selectedRowIndex == 0 && headerPanel != null) {
                target = target.union(headerPanel.getBounds());
            }
            scrollRectToVisible(target);
        }
    }
}
