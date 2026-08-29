package com.swiftfaze.veil.entities.player.classes;

import com.swiftfaze.veil.entities.player.Stats;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerClassTest {

    private final PlayerClass testClass = new PlayerClass("test:class", "Test",
            Map.of(
                    "strength", new PlayerClass.StatCurve(10, "level*2"),
                    "dexterity", new PlayerClass.StatCurve(8, null),
                    "maxHp", new PlayerClass.StatCurve(100, null),
                    "maxMana", new PlayerClass.StatCurve(50, null)
            ));

    @Test
    void baseOnlyStatsAtLevel0() {
        PlayerClass baseClass = new PlayerClass("test:base", "Base",
                Map.of(
                        "strength", new PlayerClass.StatCurve(15, null),
                        "dexterity", new PlayerClass.StatCurve(10, null),
                        "constitution", new PlayerClass.StatCurve(14, null),
                        "intelligence", new PlayerClass.StatCurve(6, null),
                        "wisdom", new PlayerClass.StatCurve(6, null),
                        "luck", new PlayerClass.StatCurve(8, null),
                        "maxHp", new PlayerClass.StatCurve(120, null),
                        "maxMana", new PlayerClass.StatCurve(20, null)
                ));

        Stats stats = new Stats();
        baseClass.applyStatsAtLevel(stats, 0);

        assertEquals(15, stats.getStrength());
        assertEquals(120, stats.getMaxHp());
        assertEquals(20, stats.getMaxMana());
    }

    @Test
    void additiveGrowthAtLaterLevel() {
        Stats stats = new Stats();
        testClass.applyStatsAtLevel(stats, 5);

        assertEquals(20, stats.getStrength()); // base 10 + level*2 (5*2=10)
    }

    @Test
    void statWithNoGrowthCurveStaysFlat() {
        Stats stats0 = new Stats();
        testClass.applyStatsAtLevel(stats0, 0);

        Stats stats10 = new Stats();
        testClass.applyStatsAtLevel(stats10, 10);

        assertEquals(stats0.getDexterity(), stats10.getDexterity());
        assertEquals(8, stats10.getDexterity());
    }

    @Test
    void missingBaseDefaultsTo0() {
        PlayerClass sparse = new PlayerClass("test:sparse", "Sparse",
                Map.of("strength", new PlayerClass.StatCurve(0, "level")));

        Stats stats = new Stats();
        sparse.applyStatsAtLevel(stats, 5);

        assertEquals(5, stats.getStrength()); // base 0 + level*1 = 5
    }

    @Test
    void maxHpAndMaxManaCascadeToCurrentValues() {
        Stats stats = new Stats();
        testClass.applyStatsAtLevel(stats, 0);

        assertEquals(100, stats.getMaxHp());
        assertEquals(100, stats.getCurrentHp());
        assertEquals(50, stats.getMaxMana());
        assertEquals(50, stats.getCurrentMana());
    }
}
