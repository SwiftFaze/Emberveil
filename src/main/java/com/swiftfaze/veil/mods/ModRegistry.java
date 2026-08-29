package com.swiftfaze.veil.mods;

import com.swiftfaze.veil.entities.buildings.Building;
import com.swiftfaze.veil.world.Tile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModRegistry {
    private final Map<String, Building> buildingsById = new LinkedHashMap<>();
    private final Map<String, Tile> tilesById = new LinkedHashMap<>();
    private final List<String> modLoadOrder;

    ModRegistry(Map<String, Building> buildingsById, Map<String, Tile> tilesById, List<String> modLoadOrder) {
        this.buildingsById.putAll(buildingsById);
        this.tilesById.putAll(tilesById);
        this.modLoadOrder = List.copyOf(modLoadOrder);
    }

    public Building getBuilding(String id) {
        return buildingsById.get(id);
    }

    public Tile getTile(String id) {
        return tilesById.get(id);
    }

    public List<String> getModLoadOrder() {
        return modLoadOrder;
    }
}
