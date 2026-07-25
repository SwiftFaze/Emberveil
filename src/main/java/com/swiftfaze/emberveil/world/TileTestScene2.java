package com.swiftfaze.emberveil.world;


import com.swiftfaze.emberveil.entities.buildings.Building;
import com.swiftfaze.emberveil.entities.buildings.BuildingLoader;

import static com.swiftfaze.emberveil.GameConst.DEFAULT_PLAYER_START_Z;






public class TileTestScene2 extends WorldScene {


    public TileTestScene2(int width, int height, int depth) {
        super(width, height, depth);
        fillAll(DEFAULT_PLAYER_START_Z, Tile.GRASS);

        Building house2 = BuildingLoader.load("small_house_01.json");


        house2.setWorldX(130);
        house2.setWorldY(130);
        house2.setWorldZ(DEFAULT_PLAYER_START_Z);

        placeBuilding(house2);

    }




}
