package com.swiftfaze.emberveil.world;

import com.swiftfaze.emberveil.tools.NoiseGenerator;

import java.util.Random;
import java.util.logging.Logger;

public class TileTestScene extends WorldScene {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private Random rand = new Random();

    public TileTestScene(int width, int height) {
        super(width, height);

        fillAll(Tile.EMPTY);

        NoiseGenerator noiseGen = new NoiseGenerator();
        noiseGen.setSeed(rand.nextDouble()*100000);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double noise = noiseGen.noise(x, y);


                if (noise > -1 && noise < -0.6) {
                    fillRegion(x, y, 1, 1, Tile.WATER);
                    continue;
                }
                if (noise > -0.6 && noise < -0.55) {
                    fillRegion(x, y, 1, 1, Tile.SAND);
                    continue;
                }
                if (noise > -0.55 && noise < -0.5) {
                    fillRegion(x, y, 1, 1, Tile.MUD);
                    continue;
                }
                if (noise > -0.5 && noise < -0.4) {
                    fillRegion(x, y, 1, 1, Tile.DIRT);
                    continue;
                }

                if (noise > -0.2 && noise < 0.2) {
                    fillRegion(x, y, 1, 1, Tile.GRASS);
                    continue;
                }

                if (noise > 0.35 && noise < 0.4) {
                    fillRegion(x, y, 1, 1, Tile.TREE);
                    continue;
                }
                if (noise > 0.4 && noise < 0.5) {
                    fillRegion(x, y, 1, 1, Tile.ROCK);
                    continue;
                }

                if (noise > 0.5 && noise < 0.8) {
                    fillRegion(x, y, 1, 1, Tile.STONE);
                    continue;
                }
                if (noise > 0.8 && noise < 0.9) {
                    fillRegion(x, y, 1, 1, Tile.SNOW);
                    continue;
                }
                if (noise > 0.9 && noise < 1) {
                    fillRegion(x, y, 1, 1, Tile.ICE);
                }

            }
        }


    }
}
