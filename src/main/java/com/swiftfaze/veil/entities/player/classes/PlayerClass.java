package com.swiftfaze.veil.entities.player.classes;

import com.google.gson.annotations.SerializedName;
import com.swiftfaze.veil.entities.player.Stats;

public class PlayerClass {
    private String name;
    private int str;
    private int dex;
    private int con;
    @SerializedName("int")
    private int intelligence;
    private int wis;
    private int luck;
    private int maxHp;
    private int maxMana;

    public String getName() {
        return name;
    }

    public void applyBaseStats(Stats stats) {
        stats.setStrength(str);
        stats.setDexterity(dex);
        stats.setConstitution(con);
        stats.setIntelligence(intelligence);
        stats.setWisdom(wis);
        stats.setLuck(luck);

        stats.setMaxHp(maxHp);
        stats.setCurrentHp(maxHp);

        stats.setMaxMana(maxMana);
        stats.setCurrentMana(maxMana);
    }
}
