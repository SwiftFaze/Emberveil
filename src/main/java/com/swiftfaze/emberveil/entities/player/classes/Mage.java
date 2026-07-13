package com.swiftfaze.emberveil.entities.player.classes;

import com.swiftfaze.emberveil.entities.player.Stats;

public class Mage extends PlayerClass {

    public Mage() {
        super("Mage");
    }

    @Override
    public void applyBaseStats(Stats stats) {
        stats.setStrength(6);
        stats.setDexterity(9);
        stats.setConstitution(8);
        stats.setIntelligence(16);
        stats.setWisdom(14);
        stats.setLuck(7);

        stats.setMaxHp(70);
        stats.setCurrentHp(70);

        stats.setMaxMana(100);
        stats.setCurrentMana(100);
    }


}
