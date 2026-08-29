package com.swiftfaze.veil.mods;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.swiftfaze.veil.entities.buildings.Building;
import com.swiftfaze.veil.entities.player.classes.PlayerClass;
import com.swiftfaze.veil.exceptions.ModLoadException;
import com.swiftfaze.veil.world.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ModLoader {
    private static final Logger logger = LoggerFactory.getLogger(ModLoader.class);

    private ModLoader() {
    }

    public static ModRegistry load(Path modsRoot) {
        List<ModManifest> loadOrder = orderByDependencies(readManifests(modsRoot));

        Map<String, Tile> tilesById = new LinkedHashMap<>();
        Map<String, String> owningTileModById = new LinkedHashMap<>();
        for (ModManifest manifest : loadOrder) {
            loadTiles(modsRoot, manifest, tilesById, owningTileModById);
        }

        Map<String, Building> buildingsById = new LinkedHashMap<>();
        Map<String, String> owningBuildingModById = new LinkedHashMap<>();
        Set<String> validStatNames = loadStatRegistry(modsRoot);

        Map<String, PlayerClass> classesById = new LinkedHashMap<>();
        Map<String, String> owningClassModById = new LinkedHashMap<>();
        List<String> modLoadOrder = new ArrayList<>();
        for (ModManifest manifest : loadOrder) {
            modLoadOrder.add(manifest.id());
            loadBuildings(modsRoot, manifest, tilesById, buildingsById, owningBuildingModById);
            loadClasses(modsRoot, manifest, validStatNames, classesById, owningClassModById);
        }

        return new ModRegistry(buildingsById, tilesById, classesById, modLoadOrder);
    }

    private static List<ModManifest> readManifests(Path modsRoot) {
        List<ModManifest> manifests = new ArrayList<>();

        if (!Files.isDirectory(modsRoot)) {
            return manifests;
        }

        try (DirectoryStream<Path> modDirs = Files.newDirectoryStream(modsRoot, Files::isDirectory)) {
            for (Path modDir : modDirs) {
                Path manifestFile = modDir.resolve("mod.json");
                if (Files.exists(manifestFile)) {
                    manifests.add(readManifest(manifestFile));
                }
            }
        } catch (IOException e) {
            throw new ModLoadException("Failed to scan mods directory: " + modsRoot, e);
        }

        return manifests;
    }

    private static ModManifest readManifest(Path manifestFile) {
        try (Reader reader = Files.newBufferedReader(manifestFile)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            String id = json.get("id").getAsString();

            List<String> dependsOn = new ArrayList<>();
            if (json.has("dependsOn")) {
                for (var element : json.getAsJsonArray("dependsOn")) {
                    dependsOn.add(element.getAsString());
                }
            }

            return new ModManifest(id, dependsOn);
        } catch (Exception e) {
            throw new ModLoadException("Failed to load mod manifest: " + manifestFile, e);
        }
    }

    private static List<ModManifest> orderByDependencies(List<ModManifest> manifests) {
        List<ModManifest> ordered = new ArrayList<>();
        List<ModManifest> remaining = new ArrayList<>(manifests);

        remaining.stream()
                .filter(m -> m.id().equals("core"))
                .findFirst()
                .ifPresent(core -> {
                    ordered.add(core);
                    remaining.remove(core);
                });

        while (!remaining.isEmpty()) {
            List<String> orderedIds = ordered.stream().map(ModManifest::id).toList();

            ModManifest next = remaining.stream()
                    .filter(m -> orderedIds.containsAll(m.dependsOn()))
                    .findFirst()
                    .orElseThrow(() -> new ModLoadException(
                            "Unresolved or cyclic mod dependency among: "
                                    + remaining.stream().map(ModManifest::id).toList()));

            ordered.add(next);
            remaining.remove(next);
        }

        return ordered;
    }

    private static void loadTiles(Path modsRoot, ModManifest manifest,
                                   Map<String, Tile> tilesById,
                                   Map<String, String> owningModById) {
        Path tilesDir = modsRoot.resolve(manifest.id()).resolve("tiles");
        if (!Files.isDirectory(tilesDir)) {
            return;
        }

        try (DirectoryStream<Path> files = Files.newDirectoryStream(tilesDir, "*.json")) {
            for (Path file : files) {
                loadTile(file, manifest.id(), tilesById, owningModById);
            }
        } catch (IOException e) {
            throw new ModLoadException("Failed to scan tiles for mod: " + manifest.id(), e);
        }
    }

    private static void loadTile(Path file, String modId,
                                  Map<String, Tile> tilesById,
                                  Map<String, String> owningModById) {
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            String id = json.get("id").getAsString();
            char symbol = json.get("symbol").getAsString().charAt(0);
            Color color = readColor(json.getAsJsonObject("color"));
            boolean walkable = json.get("walkable").getAsBoolean();

            registerWithCollisionCheck(id, new Tile(id, symbol, color, walkable), modId,
                    tilesById, owningModById, json.has("overrides"), "Tile");
        } catch (ModLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ModLoadException("Failed to load tile from file: " + file, e);
        }
    }

    private static Color readColor(JsonObject color) {
        return new Color(color.get("r").getAsInt(), color.get("g").getAsInt(), color.get("b").getAsInt());
    }

    private static void loadBuildings(Path modsRoot, ModManifest manifest,
                                       Map<String, Tile> tilesById,
                                       Map<String, Building> buildingsById,
                                       Map<String, String> owningModById) {
        Path buildingsDir = modsRoot.resolve(manifest.id()).resolve("buildings");
        if (!Files.isDirectory(buildingsDir)) {
            return;
        }

        try (DirectoryStream<Path> files = Files.newDirectoryStream(buildingsDir, "*.json")) {
            for (Path file : files) {
                loadBuilding(file, manifest.id(), tilesById, buildingsById, owningModById);
            }
        } catch (IOException e) {
            throw new ModLoadException("Failed to scan buildings for mod: " + manifest.id(), e);
        }
    }

    private static void loadBuilding(Path file, String modId,
                                      Map<String, Tile> tilesById,
                                      Map<String, Building> buildingsById,
                                      Map<String, String> owningModById) {
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            String id = json.get("id").getAsString();
            Tile[][] blueprint = readBlueprint(json.getAsJsonArray("tiles"), tilesById, id);

            registerWithCollisionCheck(id, new Building(blueprint), modId,
                    buildingsById, owningModById, json.has("overrides"), "Building");
        } catch (ModLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ModLoadException("Failed to load building from file: " + file, e);
        }
    }

    private static Tile[][] readBlueprint(JsonArray rows, Map<String, Tile> tilesById, String buildingId) {
        int height = rows.size();
        int width = rows.get(0).getAsJsonArray().size();
        Tile[][] blueprint = new Tile[height][width];

        for (int y = 0; y < height; y++) {
            JsonArray row = rows.get(y).getAsJsonArray();
            for (int x = 0; x < width; x++) {
                String tileId = row.get(x).getAsString();
                Tile tile = tilesById.get(tileId);
                if (tile == null) {
                    throw new ModLoadException("Building '" + buildingId
                            + "' references unknown tile ID: " + tileId);
                }
                blueprint[y][x] = tile;
            }
        }

        return blueprint;
    }

    private static <T> void registerWithCollisionCheck(String id, T value, String modId,
                                                         Map<String, T> registry,
                                                         Map<String, String> owningModById,
                                                         boolean overrides, String contentType) {
        if (registry.containsKey(id) && !overrides) {
            throw new ModLoadException(contentType + " ID '" + id + "' from mod '" + modId
                    + "' collides with existing content from mod '" + owningModById.get(id)
                    + "'; add an \"overrides\" field to confirm this is intentional.");
        }

        if (registry.containsKey(id)) {
            logger.info("Mod '{}' overrides {} '{}' previously provided by mod '{}'",
                    modId, contentType.toLowerCase(), id, owningModById.get(id));
        }

        registry.put(id, value);
        owningModById.put(id, modId);
        logger.info("Loaded {} '{}' from mod '{}'", contentType.toLowerCase(), id, modId);
    }

    private static Set<String> loadStatRegistry(Path modsRoot) {
        Path statsFile = modsRoot.resolve("core").resolve("stats.json");
        if (!Files.exists(statsFile)) {
            return Set.of();
        }

        try (Reader reader = Files.newBufferedReader(statsFile)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("stats")) {
                Set<String> result = new HashSet<>();
                for (var element : json.getAsJsonArray("stats")) {
                    result.add(element.getAsString());
                }
                return Set.copyOf(result);
            }
            return Set.of();
        } catch (IOException e) {
            throw new ModLoadException("Failed to load stat registry: " + statsFile, e);
        }
    }

    private static void loadClasses(Path modsRoot, ModManifest manifest,
                                     Set<String> validStatNames,
                                     Map<String, PlayerClass> classesById,
                                     Map<String, String> owningModById) {
        Path classesDir = modsRoot.resolve(manifest.id()).resolve("classes");
        if (!Files.isDirectory(classesDir)) {
            return;
        }

        try (DirectoryStream<Path> files = Files.newDirectoryStream(classesDir, "*.json")) {
            for (Path file : files) {
                loadClass(file, manifest.id(), validStatNames, classesById, owningModById);
            }
        } catch (IOException e) {
            throw new ModLoadException("Failed to scan classes for mod: " + manifest.id(), e);
        }
    }

    private static void loadClass(Path file, String modId,
                                   Set<String> validStatNames,
                                   Map<String, PlayerClass> classesById,
                                   Map<String, String> owningModById) {
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            String id = json.get("id").getAsString();
            String name = json.get("name").getAsString();

            Map<String, PlayerClass.StatCurve> statsByName = new LinkedHashMap<>();
            if (json.has("stats")) {
                JsonObject statsObj = json.getAsJsonObject("stats");
                for (String statName : statsObj.keySet()) {
                    if (!validStatNames.contains(statName)) {
                        throw new ModLoadException("Class '" + id + "' references unregistered stat '"
                                + statName + "' in file: " + file);
                    }
                    JsonObject statObj = statsObj.getAsJsonObject(statName);
                    int base = statObj.has("base") ? statObj.get("base").getAsInt() : 0;
                    String growthCalc = statObj.has("growth") ? statObj.get("growth").getAsString() : null;
                    statsByName.put(statName, new PlayerClass.StatCurve(base, growthCalc));
                }
            }

            registerWithCollisionCheck(id, new PlayerClass(id, name, statsByName), modId,
                    classesById, owningModById, json.has("overrides"), "PlayerClass");
        } catch (ModLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ModLoadException("Failed to load class from file: " + file, e);
        }
    }

    private record ModManifest(String id, List<String> dependsOn) {
    }
}
