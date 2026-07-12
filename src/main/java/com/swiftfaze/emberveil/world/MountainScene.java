package com.swiftfaze.emberveil.world;

import java.util.logging.Logger;

public class MountainScene extends WorldScene {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public MountainScene(int width, int height) {
        super(width, height);


        fillAll(Tile.EMPTY);
        fillRegion(26, 17, 12, 4, Tile.WATER);
        fillRegion(26, 22, 12, 4, Tile.DIRT);
        fillRegion(26, 27, 12, 4, Tile.GRASS);
        fillRegion(26, 12, 12, 4, Tile.STONE);

        createBorder(width, height, Tile.GRASS);
    }

}
