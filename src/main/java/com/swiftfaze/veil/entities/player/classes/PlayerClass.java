package com.swiftfaze.veil.entities.player.classes;

import com.swiftfaze.veil.entities.player.Stats;

public abstract class PlayerClass {
    private String name;

    public PlayerClass(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public abstract void applyBaseStats(Stats stats);

}
