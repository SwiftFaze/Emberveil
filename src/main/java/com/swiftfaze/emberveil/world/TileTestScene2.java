package com.swiftfaze.emberveil.world;

public class TileTestScene2 extends WorldScene {

    public TileTestScene2(int width, int height, int depth) {
        super(width, height, depth);

        createTestTerrain(width, height, depth);
    }


    private void createTestTerrain(int width, int height, int depth) {

        fillAll(Tile.EMPTY);


        fillRegion(
                5,
                0,
                0,
                width,
                height,
                Tile.GRASS
        );


        fillRegion(
                5,
                width /2,
                height /2,
                9,
                20,
                Tile.STONE
        );
        fillRegion(
                6,
                width /2,
                height /2,
                3,
                20,
                Tile.DIRT
        );
        fillRegion(
                6,
                (width /2)+3,
                height /2,
                3,
                20,
                Tile.STONE
        );
        fillRegion(
                7,
                (width /2)+3,
                height /2,
                3,
                20,
                Tile.SAND
        );


    }


}
