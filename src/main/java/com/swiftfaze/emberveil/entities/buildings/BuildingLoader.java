package com.swiftfaze.emberveil.entities.buildings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.swiftfaze.emberveil.exceptions.BuildingException;
import com.swiftfaze.emberveil.world.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class BuildingLoader {
    private static final Logger logger = LoggerFactory.getLogger(BuildingLoader.class);

    private BuildingLoader() {
    }

    public static Building load(String fileName) {
        try {
            InputStream stream = BuildingLoader.class.getResourceAsStream("/buildings/" + fileName);
            Reader reader = new InputStreamReader(stream);

            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray layers = json.getAsJsonArray("layers");


            int floors = layers.size();


            JsonArray firstFloor = layers
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonArray("tiles");


            int height = firstFloor.size();
            int width = firstFloor
                    .get(0)
                    .getAsJsonArray()
                    .size();


            Tile[][][] blueprint = new Tile[floors][height][width];


            for (int z = 0; z < floors; z++) {


                JsonArray rows = layers
                        .get(z)
                        .getAsJsonObject()
                        .getAsJsonArray("tiles");


                for (int y = 0; y < height; y++) {


                    JsonArray row = rows
                            .get(y)
                            .getAsJsonArray();


                    for (int x = 0; x < width; x++) {


                        String tileName = row
                                .get(x)
                                .getAsString();


                        blueprint[z][y][x] =
                                Tile.valueOf(tileName);

                    }
                }
            }

            logger.info("Loaded building from file: {}", fileName);
            return new Building(blueprint);
        } catch (Exception e) {
            throw new BuildingException("Failed to load building from resource file: " + fileName, e);
        }
    }
}
