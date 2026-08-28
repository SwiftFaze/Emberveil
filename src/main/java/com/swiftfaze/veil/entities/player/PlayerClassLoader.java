package com.swiftfaze.veil.entities.player;

import com.google.gson.Gson;
import com.swiftfaze.veil.entities.player.classes.PlayerClass;
import com.swiftfaze.veil.exceptions.PlayerClassException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

public class PlayerClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(PlayerClassLoader.class);
    private static final Gson GSON = new Gson();

    // Classpath resource directories can't be listed portably across the IDE,
    // `mvn test`, and the shaded jar without an extra scanning dependency, so
    // known class files are named explicitly. Add new entries here when a new
    // class JSON is added under resources/classes/.
    private static final List<String> KNOWN_CLASS_FILES = List.of("warrior.json", "mage.json");

    private PlayerClassLoader() {
    }

    public static PlayerClass load(String fileName) {
        try (InputStream stream = PlayerClassLoader.class.getResourceAsStream("/classes/" + fileName)) {
            Reader reader = new InputStreamReader(stream);
            PlayerClass playerClass = GSON.fromJson(reader, PlayerClass.class);

            logger.info("Loaded player class from file: {}", fileName);
            return playerClass;
        } catch (Exception e) {
            throw new PlayerClassException("Failed to load player class from resource file: " + fileName, e);
        }
    }

    public static List<PlayerClass> loadAll() {
        return KNOWN_CLASS_FILES.stream()
                .map(PlayerClassLoader::load)
                .toList();
    }
}
