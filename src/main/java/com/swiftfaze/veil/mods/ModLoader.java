package com.swiftfaze.veil.mods;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.swiftfaze.veil.entities.buildings.Building;
import com.swiftfaze.veil.exceptions.ModLoadException;
import com.swiftfaze.veil.world.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModLoader {
    private static final Logger logger = LoggerFactory.getLogger(ModLoader.class);

    private ModLoader() {
    }

    public static ModRegistry load(Path modsRoot) {
        List<ModManifest> manifests = readManifests(modsRoot);
        List<ModManifest> loadOrder = orderByDependencies(manifests);

        Map<String, Building> buildingsById = new LinkedHashMap<>();
        Map<String, String> owningModById = new LinkedHashMap<>();
        List<String> modLoadOrder = new ArrayList<>();

        for (ModManifest manifest : loadOrder) {
            modLoadOrder.add(manifest.id());
            loadBuildings(modsRoot, manifest, buildingsById, owningModById);
        }

        return new ModRegistry(buildingsById, modLoadOrder);
    }

    private static List<ModManifest> readManifests(Path modsRoot) {
        List<ModManifest> manifests = new ArrayList<>();

        if (!Files.isDirectory(modsRoot)) {
            return manifests;
        }

        try (DirectoryStream<Path> modDirs = Files.newDirectoryStream(modsRoot, Files::isDirectory)) {
            for (Path modDir : modDirs) {
                Path manifestFile = modDir.resolve("mod.json");
                if (!Files.exists(manifestFile)) {
                    continue;
                }
                manifests.add(readManifest(manifestFile));
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

    private static void loadBuildings(Path modsRoot, ModManifest manifest,
                                       Map<String, Building> buildingsById,
                                       Map<String, String> owningModById) {
        Path buildingsDir = modsRoot.resolve(manifest.id()).resolve("buildings");
        if (!Files.isDirectory(buildingsDir)) {
            return;
        }

        try (DirectoryStream<Path> files = Files.newDirectoryStream(buildingsDir, "*.json")) {
            for (Path file : files) {
                loadBuilding(file, manifest.id(), buildingsById, owningModById);
            }
        } catch (IOException e) {
            throw new ModLoadException("Failed to scan buildings for mod: " + manifest.id(), e);
        }
    }

    private static void loadBuilding(Path file, String modId,
                                      Map<String, Building> buildingsById,
                                      Map<String, String> owningModById) {
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            String id = json.get("id").getAsString();
            boolean overrides = json.has("overrides");

            if (buildingsById.containsKey(id) && !overrides) {
                throw new ModLoadException("Building ID '" + id + "' from mod '" + modId
                        + "' collides with existing content from mod '" + owningModById.get(id)
                        + "'; add an \"overrides\" field to confirm this is intentional.");
            }

            if (buildingsById.containsKey(id)) {
                logger.info("Mod '{}' overrides building '{}' previously provided by mod '{}'",
                        modId, id, owningModById.get(id));
            }

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

            buildingsById.put(id, new Building(blueprint));
            owningModById.put(id, modId);
            logger.info("Loaded building '{}' from mod '{}'", id, modId);
        } catch (ModLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ModLoadException("Failed to load building from file: " + file, e);
        }
    }

    private record ModManifest(String id, List<String> dependsOn) {
    }
}
