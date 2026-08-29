package com.swiftfaze.veil.mods;

import com.swiftfaze.veil.entities.buildings.Building;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModRegistry {
    private final Map<String, Building> buildingsById = new LinkedHashMap<>();
    private final List<String> modLoadOrder;

    ModRegistry(Map<String, Building> buildingsById, List<String> modLoadOrder) {
        this.buildingsById.putAll(buildingsById);
        this.modLoadOrder = List.copyOf(modLoadOrder);
    }

    public Building getBuilding(String id) {
        return buildingsById.get(id);
    }

    public List<String> getModLoadOrder() {
        return modLoadOrder;
    }
}
