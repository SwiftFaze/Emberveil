package com.swiftfaze.veil.world;

import com.swiftfaze.veil.entities.buildings.Building;
import com.swiftfaze.veil.entities.buildings.BuildingLoader;

public class TileTestScene2 extends WorldScene {

    public TileTestScene2(int width, int height) {
        super(width, height);
        fillAll(Tile.GRASS);

        Building house2 = BuildingLoader.load("small_house_01.json");

        house2.setWorldX(130);
        house2.setWorldY(130);

        placeBuilding(house2);
    }
}
