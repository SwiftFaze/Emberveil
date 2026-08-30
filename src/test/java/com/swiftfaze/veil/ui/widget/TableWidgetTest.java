package com.swiftfaze.veil.ui.widget;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TableWidgetTest {
    private TableWidget<String> tableWidget;
    private List<String> confirmedRows;

    @BeforeEach
    public void setUp() {
        confirmedRows = new ArrayList<>();
        tableWidget = new TableWidget<>(List.of(
            s -> s.substring(0, 3),
            s -> s.substring(3)
        ));
        tableWidget.setOnConfirm(confirmedRows::add);
    }

    @Test
    public void navigatingDownMovesSelectedRowToNextRow() {
        tableWidget.setRows(List.of("Sword", "Shield", "Potion"));
        assertEquals(0, tableWidget.getSelectedRowIndex());
        tableWidget.moveDown();
        assertEquals(1, tableWidget.getSelectedRowIndex());
    }

    @Test
    public void navigatingRightMovesSelectedColumnToNextColumn() {
        tableWidget.setRows(List.of("Sword", "Shield"));
        assertEquals(0, tableWidget.getSelectedColumnIndex());
        tableWidget.moveRight();
        assertEquals(1, tableWidget.getSelectedColumnIndex());
    }

    @Test
    public void movingUpFromFirstRowWrapsToLastRow() {
        tableWidget.setRows(List.of("Sword", "Shield", "Potion"));
        assertEquals(0, tableWidget.getSelectedRowIndex());
        tableWidget.moveUp();
        assertEquals(2, tableWidget.getSelectedRowIndex());
    }

    @Test
    public void movingRightFromLastColumnWrapsToFirstColumn() {
        tableWidget.setRows(List.of("Sword", "Shield"));
        tableWidget.moveRight();
        assertEquals(1, tableWidget.getSelectedColumnIndex());
        tableWidget.moveRight();
        assertEquals(0, tableWidget.getSelectedColumnIndex());
    }

    @Test
    public void wrapAroundCanBeDisabled() {
        tableWidget.setRows(List.of("Sword", "Shield", "Potion"));
        tableWidget.setWrapAround(false);
        assertEquals(0, tableWidget.getSelectedRowIndex());
        tableWidget.moveUp();
        assertEquals(0, tableWidget.getSelectedRowIndex());
    }

    @Test
    public void confirmingTableWidgetConfirmsWholeRow() {
        tableWidget.setRows(List.of("Sword", "Shield", "Potion"));
        tableWidget.moveDown();
        tableWidget.moveDown();
        assertEquals(2, tableWidget.getSelectedRowIndex());
        tableWidget.getActionMap().get("table-confirm").actionPerformed(null);
        assertEquals(1, confirmedRows.size());
        assertEquals("Potion", confirmedRows.get(0));
    }

    @Test
    public void emptyTableNavigationIsNoOp() {
        tableWidget.setRows(List.of());
        tableWidget.moveUp();
        tableWidget.moveDown();
        tableWidget.moveLeft();
        tableWidget.moveRight();
        assertNull(tableWidget.getSelectedRow());
    }

    @Test
    public void emptyTableConfirmIsNoOp() {
        tableWidget.setRows(List.of());
        tableWidget.getActionMap().get("table-confirm").actionPerformed(null);
        assertTrue(confirmedRows.isEmpty());
    }

    @Test
    public void updateRowReplacesRowDataWithoutResettingSelection() {
        tableWidget.setRows(List.of("Sword", "Shield", "Potion"));
        tableWidget.moveDown();
        tableWidget.moveDown();
        assertEquals(2, tableWidget.getSelectedRowIndex());
        tableWidget.updateRow(2, "PotionX");
        assertEquals(2, tableWidget.getSelectedRowIndex());
        assertEquals("PotionX", tableWidget.getSelectedRow());
    }

    @Test
    public void updateRowOutOfBoundsIsNoOp() {
        tableWidget.setRows(List.of("Sword", "Shield"));
        tableWidget.updateRow(5, "Nothing");
        assertEquals(List.of("Sword", "Shield").get(0), tableWidget.getSelectedRow());
    }

    @Test
    public void selectedRowAccentBorderAndDimmingCanBeToggledWithoutError() {
        tableWidget.setRows(List.of("Sword", "Shield"));
        tableWidget.setSelectedRowAccentBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.GREEN));
        tableWidget.setOtherRowsDimmed(true);
        tableWidget.setSelectedRowAccentBorder(null);
        tableWidget.setOtherRowsDimmed(false);
        assertEquals("Sword", tableWidget.getSelectedRow());
    }
}
