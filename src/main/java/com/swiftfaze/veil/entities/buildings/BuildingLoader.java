package com.swiftfaze.veil.entities.buildings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.swiftfaze.veil.exceptions.BuildingException;
import com.swiftfaze.veil.world.Tile;
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
            JsonArray rows = json.getAsJsonArray("tiles");

            int height = rows.size();
            int width = rows.get(0).getAsJsonArray().size();

            Tile[][] blueprint = new Tile[height][width];

            for (int y = 0; y < height; y++) {
                JsonArray row = rows.get(y).getAsJsonArray();

                for (int x = 0; x < width; x++) {
                    blueprint[y][x] = Tile.valueOf(row.get(x).getAsString());
                }
            }

            logger.info("Loaded building from file: {}", fileName);
            return new Building(blueprint);
        } catch (Exception e) {
            throw new BuildingException("Failed to load building from resource file: " + fileName, e);
        }
    }
}
