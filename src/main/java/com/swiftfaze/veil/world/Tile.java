package com.swiftfaze.veil.world;

import java.awt.Color;

public final class Tile {
    private final String id;
    private final char symbol;
    private final Color color;
    private final boolean walkable;

    public Tile(String id, char symbol, Color color, boolean walkable) {
        this.id = id;
        this.symbol = symbol;
        this.color = color;
        this.walkable = walkable;
    }

    public String getId() {
        return id;
    }

    public char getSymbol() {
        return symbol;
    }

    public Color getColor() {
        return color;
    }

    public boolean isWalkable() {
        return walkable;
    }
}
