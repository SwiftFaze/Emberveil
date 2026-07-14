package com.swiftfaze.emberveil.world;

import java.awt.*;

public enum Tile {
//    GRASS('〴', new Color(179, 224, 160), true),
    GRASS('⡐', new Color(179, 224, 160), true),

    DIRT('⠍', new Color(216, 178, 140), true),
//    WATER('☵', new Color(174, 191, 232), false),
    WATER('⠭', new Color(174, 191, 232), false),
//    STONE('㔣', new Color(88, 88, 92), false),
    STONE('⠿', new Color(88, 88, 92), false),
    SAND('⠋', new Color(197, 175, 110), true),
    MUD('⠊', new Color(103, 76, 41), true),
    SNOW('⠞', Color.WHITE, true),
    ICE('⠨', new Color(210, 240, 255), false),
    PATH('·', new Color(224, 209, 180), true),
    STAIR_UP('▓', new Color(58, 87, 193), true),
    STAIR_DOWN('▓', new Color(179, 71, 71), true),
    EMPTY(' ', new Color(211, 211, 211), false),

    WALL('▓', new Color(190, 190, 190), false),
    BRIDGE('=', new Color(210, 178, 140), true),

    TREE('㣺', new Color(79, 106, 79), false),
//    TREE('⡊', new Color(79, 106, 79), false),
//    ROCK('҈', new Color(197, 197, 197), false),
    ROCK('⠶', new Color(197, 197, 197), false),

    BUSH('b', new Color(180, 224, 180), false),
    DOOR('D', new Color(224, 196, 160), true),
    CHEST('C', new Color(240, 219, 160), false),
    PORTAL('O', new Color(216, 180, 232), false),
    LAVA('^', new Color(240, 160, 160), false);

    private final char symbol;
    private final Color color;
    private final boolean walkable;

    Tile(char symbol, Color color, boolean walkable) {
        this.symbol = symbol;
        this.color = color;
        this.walkable = walkable;
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
