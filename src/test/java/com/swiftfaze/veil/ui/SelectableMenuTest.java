package com.swiftfaze.veil.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SelectableMenuTest {

    @Test
    void startsAtTheFirstItem() {
        SelectableMenu menu = new SelectableMenu(3);
        assertEquals(0, menu.selected());
    }

    @Test
    void moveDownAdvancesToTheNextItem() {
        SelectableMenu menu = new SelectableMenu(3);
        menu.moveDown();
        assertEquals(1, menu.selected());
    }

    @Test
    void moveUpFromTheFirstItemWrapsToTheLastItem() {
        SelectableMenu menu = new SelectableMenu(3);
        menu.moveUp();
        assertEquals(2, menu.selected());
    }

    @Test
    void moveDownFromTheLastItemWrapsToTheFirstItem() {
        SelectableMenu menu = new SelectableMenu(3);
        menu.moveDown();
        menu.moveDown();
        menu.moveDown();
        assertEquals(0, menu.selected());
    }
}
