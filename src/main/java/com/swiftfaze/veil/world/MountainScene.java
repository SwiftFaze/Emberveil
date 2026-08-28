package com.swiftfaze.veil.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MountainScene extends WorldScene {
    private static final Logger logger = LoggerFactory.getLogger(MountainScene.class);

    public MountainScene(int width, int height) {
        super(width, height, 0);


        fillAll(Tile.EMPTY);
        fillRegion(26, 17, 12, 4, Tile.WATER);
        fillRegion(26, 22, 12, 4, Tile.DIRT);
        fillRegion(26, 27, 12, 4, Tile.GRASS);
        fillRegion(26, 12, 12, 4, Tile.STONE);

        createBorder(width, height, Tile.GRASS);
    }

}
