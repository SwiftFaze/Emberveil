package com.swiftfaze.veil.ui.widget;

import com.swiftfaze.veil.input.Keybindings;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TableWidget<T> extends Widget {
    private final List<String> columnHeaders;
    private final List<Function<T, String>> columnRenderers;
    private final List<T> rows = new ArrayList<>();
    private final List<JLabel> labels = new ArrayList<>();
    private int selectedRowIndex = 0;
    private int selectedColumnIndex = 0;
    private boolean wrapAround = true;
    private boolean selectable = true;
    private Consumer<T> onConfirm = t -> {};

    public TableWidget(List<Function<T, String>> columnRenderers) {
        this(List.of(), columnRenderers);
    }

    public TableWidget(List<String> columnHeaders, List<Function<T, String>> columnRenderers) {
        this.columnHeaders = columnHeaders;
        this.columnRenderers = columnRenderers;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
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
     * A non-selectable table renders every row in NORMAL_TEXT, with no row-highlight
     * indication — for purely static/display data (e.g. a fixed field/value list) that
     * isn't meant to be keyboard-navigated, as opposed to a real navigable table.
     */
    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
        refreshHighlight();
    }

    public void setOnConfirm(Consumer<T> onConfirm) {
        this.onConfirm = onConfirm;
    }

    public T getSelectedRow() {
        return rows.isEmpty() ? null : rows.get(selectedRowIndex);
    }

    public int getSelectedRowIndex() {
        return selectedRowIndex;
    }

    public int getRowCount() {
        return rows.size();
    }

    public boolean isSelectable() {
        return selectable;
    }

    public int getSelectedColumnIndex() {
        return selectedColumnIndex;
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
        JLabel header = new JLabel(String.join("  ", columnHeaders));
        header.setForeground(WidgetTheme.NORMAL_TEXT);
        header.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.BOLD, 16));
        header.setAlignmentX(LEFT_ALIGNMENT);
        Border bottomLine = BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.LIGHT_GRAY);
        header.setBorder(BorderFactory.createCompoundBorder(bottomLine, BorderFactory.createEmptyBorder(0, 0, 2, 0)));
        add(header);
    }

    private void refresh() {
        // Remove only the data-row labels, keep the header row (index 0, if present) intact.
        for (JLabel label : labels) {
            remove(label);
        }
        labels.clear();
        for (T row : rows) {
            String cellText = renderRow(row);
            JLabel label = new JLabel(cellText);
            label.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 16));
            label.setAlignmentX(LEFT_ALIGNMENT);
            labels.add(label);
            add(label);
        }
        refreshHighlight();
        revalidate();
        repaint();
    }

    private String renderRow(T row) {
        List<String> cells = new ArrayList<>();
        for (Function<T, String> renderer : columnRenderers) {
            cells.add(renderer.apply(row));
        }
        return String.join("  ", cells);
    }

    private void refreshHighlight() {
        for (int i = 0; i < labels.size(); i++) {
            labels.get(i).setForeground(
                selectable && i == selectedRowIndex ? WidgetTheme.SELECTED_HIGHLIGHT : WidgetTheme.NORMAL_TEXT);
        }
        if (selectable && selectedRowIndex < labels.size()) {
            scrollRectToVisible(labels.get(selectedRowIndex).getBounds());
        }
    }
}
