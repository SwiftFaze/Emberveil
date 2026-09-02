package com.swiftfaze.veil.entities.items;

import com.swiftfaze.veil.component.DetailTable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ItemTest {

    @Test
    void getDetailTablesWithoutEffects() {
        Item item = new Item(
                "test:sword",
                "Iron Sword",
                new Item.ItemAttributes(
                        '/',
                        "weapon",
                        "hand",
                        new Item.BaseDamage(5, 10),
                        List.of()
                )
        );

        List<DetailTable> tables = item.getDetailTables();

        assertEquals(1, tables.size());
        assertEquals("", tables.get(0).label());
        assertEquals(List.of("Field", "Value"), tables.get(0).columnHeaders());
        assertEquals(7, tables.get(0).rows().size());
        assertRowContains(tables.get(0).rows(), "ID", "test:sword");
        assertRowContains(tables.get(0).rows(), "Name", "Iron Sword");
        assertRowContains(tables.get(0).rows(), "Glyph", "/");
        assertRowContains(tables.get(0).rows(), "Type", "weapon");
        assertRowContains(tables.get(0).rows(), "Slot", "hand");
        assertRowContains(tables.get(0).rows(), "Base Damage (Min)", "5");
        assertRowContains(tables.get(0).rows(), "Base Damage (Max)", "10");
    }

    @Test
    void getDetailTablesWithEffects() {
        List<Item.Effect> effects = List.of(
                new Item.Effect("bonus", "strength", "+2"),
                new Item.Effect("penalty", "speed", "-1")
        );
        Item item = new Item(
                "test:staff",
                "Arcane Staff",
                new Item.ItemAttributes(
                        '|',
                        "weapon",
                        "hand",
                        new Item.BaseDamage(3, 8),
                        effects
                )
        );

        List<DetailTable> tables = item.getDetailTables();

        assertEquals(2, tables.size());
        assertEquals("", tables.get(0).label());
        assertEquals(7, tables.get(0).rows().size());
        assertEquals("Effects:", tables.get(1).label());
        assertEquals(List.of("Type", "Stat", "Calc"), tables.get(1).columnHeaders());
        assertEquals(2, tables.get(1).rows().size());
        assertEquals(List.of("bonus", "strength", "+2"), tables.get(1).rows().get(0));
        assertEquals(List.of("penalty", "speed", "-1"), tables.get(1).rows().get(1));
    }

    @Test
    void noDamageFieldsWhenBaseDamageIsZero() {
        Item item = new Item(
                "test:armour",
                "Chain Mail",
                new Item.ItemAttributes(
                        '[',
                        "armour",
                        "body",
                        new Item.BaseDamage(0, 0),
                        List.of()
                )
        );

        List<DetailTable> tables = item.getDetailTables();

        assertEquals(1, tables.size());
        assertEquals(5, tables.get(0).rows().size());
        assertRowNotPresent(tables.get(0).rows(), "Base Damage (Min)");
        assertRowNotPresent(tables.get(0).rows(), "Base Damage (Max)");
    }

    private void assertRowContains(List<List<String>> rows, String field, String value) {
        boolean found = rows.stream()
                .anyMatch(row -> row.size() >= 2 && row.get(0).equals(field) && row.get(1).equals(value));
        assertEquals(true, found, "Row with field '" + field + "' and value '" + value + "' not found");
    }

    private void assertRowNotPresent(List<List<String>> rows, String field) {
        boolean found = rows.stream()
                .anyMatch(row -> row.size() >= 1 && row.get(0).equals(field));
        assertFalse(found, "Row with field '" + field + "' should not be present");
    }
}
