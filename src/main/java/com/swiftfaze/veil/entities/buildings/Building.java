package com.swiftfaze.veil.entities.buildings;

import com.swiftfaze.veil.world.Tile;

public class Building {

    private int worldX;
    private int worldY;

    private final Tile[][] blueprint;

    public Building(Tile[][] blueprint) {
        this.blueprint = blueprint;
    }

    public Tile[][] getBlueprint() {
        return blueprint;
    }

    public int getWorldX() {
        return worldX;
    }

    public void setWorldX(int worldX) {
        this.worldX = worldX;
    }

    public int getWorldY() {
        return worldY;
    }

    public void setWorldY(int worldY) {
        this.worldY = worldY;
    }
}
