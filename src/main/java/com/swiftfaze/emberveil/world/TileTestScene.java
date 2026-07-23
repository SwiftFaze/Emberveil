package com.swiftfaze.emberveil.world;

import com.swiftfaze.emberveil.tools.NoiseGenerator;

import java.util.Random;


public class TileTestScene extends WorldScene {


    private Random rand = new Random();

    public TileTestScene(int width, int height, int depth) {
        super(width, height, depth);
        generateHeightmapTerrain(width, height, depth);

    }

    private void generateHeightmapTerrain(int width, int height, int depth) {
        NoiseGenerator noiseGen = new NoiseGenerator();
        noiseGen.setSeed(rand.nextDouble() * 100000);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double noise = noiseGen.noise(x, y); // one 2D value per column

                int elevation = elevationForNoise(noise, depth);

                // Solid rock underneath the surface (not walkable).
                for (int z = 0; z < elevation; z++) {
                    fillRegion(z, x, y, 1, 1, Tile.EMPTY);
                }

                // The actual walkable ground, biome picked from the same noise value.
                fillRegion(elevation, x, y, 1, 1, biomeForNoise(noise));

                // Everything above elevation is left unset -> open air, nothing to draw or walk on.
            }
        }
    }

    private int elevationForNoise(double noise, int depth) {
        double normalized = (noise + 1) / 2.0; // roughly -1..1 -> 0..1
        int elevation = (int) Math.round(normalized * (depth - 1));
        return Math.max(0, Math.min(depth - 1, elevation));
    }

    private Tile biomeForNoise(double noise) {
        if (noise < -0.6) return Tile.WATER;
        if (noise < -0.5) return Tile.SAND;
        if (noise < -0.4) return Tile.MUD;
        if (noise < -0.2) return Tile.DIRT;
        if (noise < 0.2)  return Tile.GRASS;
        if (noise < 0.35) return Tile.DIRT;
        if (noise < 0.4)  return Tile.TREE;
        if (noise < 0.5)  return Tile.ROCK;
        if (noise < 0.8)  return Tile.STONE;
        if (noise < 0.9)  return Tile.SNOW;
        return Tile.ICE;
    }

}
