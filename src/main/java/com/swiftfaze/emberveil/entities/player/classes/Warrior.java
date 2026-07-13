package com.swiftfaze.emberveil.entities.player.classes;

import com.swiftfaze.emberveil.entities.player.Stats;

public class Warrior extends PlayerClass {

    public Warrior() {
        super("Warrior");
    }

    @Override
    public void applyBaseStats(Stats stats) {
        stats.setStrength(15);
        stats.setDexterity(10);
        stats.setConstitution(14);
        stats.setIntelligence(6);
        stats.setWisdom(6);
        stats.setLuck(8);

        stats.setMaxHp(120);
        stats.setCurrentHp(120);

        stats.setMaxMana(20);
        stats.setCurrentMana(20);
    }

}
