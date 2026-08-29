package com.swiftfaze.veil.entities.player;

import com.swiftfaze.veil.entities.player.classes.PlayerClass;
import com.swiftfaze.veil.exceptions.PlayerClassException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerClassLoaderTest {

    @Test
    void loadsWarriorWithExpectedStats() {
        PlayerClass warrior = PlayerClassLoader.load("warrior.json");
        Stats stats = new Stats();
        warrior.applyBaseStats(stats);

        assertEquals("Warrior", warrior.getName());
        assertEquals(15, stats.getStrength());
        assertEquals(10, stats.getDexterity());
        assertEquals(14, stats.getConstitution());
        assertEquals(6, stats.getIntelligence());
        assertEquals(6, stats.getWisdom());
        assertEquals(8, stats.getLuck());
        assertEquals(120, stats.getMaxHp());
        assertEquals(120, stats.getCurrentHp());
        assertEquals(20, stats.getMaxMana());
        assertEquals(20, stats.getCurrentMana());
    }

    @Test
    void loadsMageWithExpectedStats() {
        PlayerClass mage = PlayerClassLoader.load("mage.json");
        Stats stats = new Stats();
        mage.applyBaseStats(stats);

        assertEquals("Mage", mage.getName());
        assertEquals(6, stats.getStrength());
        assertEquals(9, stats.getDexterity());
        assertEquals(8, stats.getConstitution());
        assertEquals(16, stats.getIntelligence());
        assertEquals(14, stats.getWisdom());
        assertEquals(7, stats.getLuck());
        assertEquals(70, stats.getMaxHp());
        assertEquals(100, stats.getMaxMana());
    }

    @Test
    void loadAllReturnsEveryKnownClass() {
        List<PlayerClass> classes = PlayerClassLoader.loadAll();

        List<String> names = classes.stream().map(PlayerClass::getName).toList();
        assertEquals(2, classes.size());
        assertEquals(List.of("Warrior", "Mage"), names);
    }

    @Test
    void loadingMissingFileThrowsPlayerClassException() {
        assertThrows(PlayerClassException.class, () -> PlayerClassLoader.load("not_a_real_class.json"));
    }
}
