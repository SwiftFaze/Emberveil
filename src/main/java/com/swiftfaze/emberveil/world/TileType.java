package com.swiftfaze.emberveil.world;

import java.awt.*;

public enum TileType {
    GRASS(',', Color.GREEN.darker()),
    DIRT('.', new Color(139, 69, 19)),
    WATER('~', Color.BLUE),
    STONE('#', Color.GRAY),
    EMPTY(' ', Color.DARK_GRAY);

    private final char symbol;
    private final Color color;

    TileType(char symbol, Color color) {
        this.symbol = symbol;
        this.color = color;
    }

    public char getSymbol() { return symbol; }
    public Color getColor() { return color; }
}
