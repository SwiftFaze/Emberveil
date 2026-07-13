package com.swiftfaze.emberveil.entities.player.classes;

import com.swiftfaze.emberveil.entities.player.Stats;

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
