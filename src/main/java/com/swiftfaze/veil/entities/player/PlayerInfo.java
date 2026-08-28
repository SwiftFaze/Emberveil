package com.swiftfaze.veil.entities.player;

import com.swiftfaze.veil.entities.player.classes.PlayerClass;

public class PlayerInfo {
    private static final String DEFAULT_CLASS_FILE = "warrior.json";

    private String firstName;
    private String lastName;
    private Level level;
    private Stats stats;
    private PlayerClass playerClass;

    public PlayerInfo() {
        this.firstName = "Branor";
        this.lastName = "Hamerfell";
        this.level = new Level();
        this.stats = new Stats();
        this.playerClass = PlayerClassLoader.load(DEFAULT_CLASS_FILE);
        this.playerClass.applyBaseStats(stats);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public void setPlayerClass(PlayerClass playerClass) {
        this.playerClass = playerClass;
        this.playerClass.applyBaseStats(this.stats);
    }
}
