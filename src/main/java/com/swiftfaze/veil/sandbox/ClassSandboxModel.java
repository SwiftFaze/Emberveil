package com.swiftfaze.veil.sandbox;

import com.swiftfaze.veil.entities.player.PlayerClassLoader;
import com.swiftfaze.veil.entities.player.Stats;
import com.swiftfaze.veil.entities.player.classes.PlayerClass;

import java.util.List;

public class ClassSandboxModel {

    private final List<PlayerClass> classes;

    public ClassSandboxModel() {
        this(PlayerClassLoader.loadAll());
    }

    public ClassSandboxModel(List<PlayerClass> classes) {
        this.classes = classes;
    }

    public List<String> classNames() {
        return classes.stream().map(PlayerClass::getName).toList();
    }

    public Stats computedStats(String className) {
        PlayerClass playerClass = classes.stream()
                .filter(c -> c.getName().equals(className))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown class: " + className));

        Stats stats = new Stats();
        playerClass.applyBaseStats(stats);
        return stats;
    }
}
