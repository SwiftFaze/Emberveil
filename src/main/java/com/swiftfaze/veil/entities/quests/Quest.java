package com.swiftfaze.veil.entities.quests;

import java.util.List;

public class Quest {

    public record Objective(String type, String target, int count) {
    }

    public record Reward(String type, String id, Integer count, String calc) {
    }

    private final String id;
    private final String name;
    private final Objective objective;
    private final List<Reward> rewards;

    public Quest(String id, String name, Objective objective, List<Reward> rewards) {
        this.id = id;
        this.name = name;
        this.objective = objective;
        this.rewards = rewards;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Objective getObjective() {
        return objective;
    }

    public List<Reward> getRewards() {
        return rewards;
    }
}
