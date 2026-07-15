package com.swiftfaze.emberveil.world;


import static com.swiftfaze.emberveil.GameConst.DEFAULT_PLAYER_START_Z;

public class TileTestScene2 extends WorldScene {


    public TileTestScene2(int width, int height, int depth) {
        super(width, height, depth);

        createTestTerrain(width, height, depth);
    }


    private void createTestTerrain(int width, int height, int depth) {

        fillAll(DEFAULT_PLAYER_START_Z, Tile.GRASS);


        int startX = (width / 2) + 3;
        int startY = (height / 2) + 3;

        createHouse(DEFAULT_PLAYER_START_Z, startX + 1, startY, 8, 6, Tile.STONE, Tile.WOOD, null);
        createHouse(DEFAULT_PLAYER_START_Z, startX + 5, startY + 2, 8, 15, Tile.STONE, Tile.WOOD, null);
        createHouse(DEFAULT_PLAYER_START_Z, startX + 2, startY + 6, 12, 5, Tile.STONE, Tile.WOOD, Tile.DOOR);


    }

    private void createHouse(int z, int x, int y, int width, int height, Tile wall, Tile floor, Tile door) {
        if (width > 2 && height > 2) {
            fillRegion(z, x + 1, y + 1, width - 2, height - 2, floor);
        }

        fillHouseRegion(z, x, y, width, 1, wall, floor);               // top
        fillHouseRegion(z, x, y + height - 1, width, 1, wall, floor);  // bottom
        fillHouseRegion(z, x, y, 1, height, wall, floor);              // left
        fillHouseRegion(z, x + width - 1, y, 1, height, wall, floor);  // right


        if (door != null) {
            int doorX = x + width / 2;
            fillHouseRegion(z, doorX, y + height - 1, 1, 1, door, floor);
        }


    }


}
