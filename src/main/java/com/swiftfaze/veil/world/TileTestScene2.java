package com.swiftfaze.veil.world;

import com.swiftfaze.veil.entities.buildings.Building;
import com.swiftfaze.veil.mods.ModLoader;
import com.swiftfaze.veil.mods.ModRegistry;

import java.nio.file.Paths;

public class TileTestScene2 extends WorldScene {

    public TileTestScene2(int width, int height) {
        super(width, height);
        fillAll(Tile.GRASS);

        ModRegistry mods = ModLoader.load(Paths.get("mods"));
        Building house2 = mods.getBuilding("core:small_house_01");

        house2.setWorldX(130);
        house2.setWorldY(130);

        placeBuilding(house2);
    }
}
