package com.swiftfaze.veil.ui;

public class SelectableMenu {

    private final int itemCount;
    private int selectedIndex;

    public SelectableMenu(int itemCount) {
        if (itemCount <= 0) {
            throw new IllegalArgumentException("itemCount must be positive");
        }
        this.itemCount = itemCount;
    }

    public void moveUp() {
        selectedIndex = (selectedIndex - 1 + itemCount) % itemCount;
    }

    public void moveDown() {
        selectedIndex = (selectedIndex + 1) % itemCount;
    }

    public int selected() {
        return selectedIndex;
    }
}
