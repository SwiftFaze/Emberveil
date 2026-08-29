package com.swiftfaze.veil.entities.items;

import java.util.List;

public class Item {

    public record BaseDamage(int min, int max) {
    }

    public record Effect(String type, String stat, String calc) {
    }

    private final String id;
    private final String name;
    private final char glyph;
    private final String type;
    private final String slot;
    private final BaseDamage baseDamage;
    private final List<Effect> effects;

    public Item(String id, String name, char glyph, String type, String slot,
                BaseDamage baseDamage, List<Effect> effects) {
        this.id = id;
        this.name = name;
        this.glyph = glyph;
        this.type = type;
        this.slot = slot;
        this.baseDamage = baseDamage;
        this.effects = effects;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public char getGlyph() {
        return glyph;
    }

    public String getType() {
        return type;
    }

    public String getSlot() {
        return slot;
    }

    public BaseDamage getBaseDamage() {
        return baseDamage;
    }

    public List<Effect> getEffects() {
        return effects;
    }
}
