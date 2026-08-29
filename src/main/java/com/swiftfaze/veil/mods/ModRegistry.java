package com.swiftfaze.veil.mods;

import com.swiftfaze.veil.entities.buildings.Building;
import com.swiftfaze.veil.entities.player.classes.PlayerClass;
import com.swiftfaze.veil.world.Tile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModRegistry {
    private final Map<String, Building> buildingsById = new LinkedHashMap<>();
    private final Map<String, Tile> tilesById = new LinkedHashMap<>();
    private final Map<String, PlayerClass> classesById = new LinkedHashMap<>();
    private final List<String> modLoadOrder;

    ModRegistry(Map<String, Building> buildingsById, Map<String, Tile> tilesById,
                Map<String, PlayerClass> classesById, List<String> modLoadOrder) {
        this.buildingsById.putAll(buildingsById);
        this.tilesById.putAll(tilesById);
        this.classesById.putAll(classesById);
        this.modLoadOrder = List.copyOf(modLoadOrder);
    }

    public Building getBuilding(String id) {
        return buildingsById.get(id);
    }

    public Tile getTile(String id) {
        return tilesById.get(id);
    }

    public PlayerClass getPlayerClass(String id) {
        return classesById.get(id);
    }

    public List<PlayerClass> getAllPlayerClasses() {
        return List.copyOf(classesById.values());
    }

    public List<String> getModLoadOrder() {
        return modLoadOrder;
    }
}
