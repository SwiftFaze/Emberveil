package com.swiftfaze.veil.steps;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.swiftfaze.veil.exceptions.ModLoadException;
import com.swiftfaze.veil.mods.ModLoader;
import com.swiftfaze.veil.mods.ModRegistry;
import com.swiftfaze.veil.world.Tile;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.awt.Color;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModLoaderSteps {

    private record BuildingFixture(String id, String overrides, String explicitTileId) {
    }

    private record TileFixture(String id, char symbol, int r, int g, int b, boolean walkable, String overrides) {
    }

    private Path modsRoot;
    private final Map<String, List<String>> dependsOnByMod = new LinkedHashMap<>();
    private final Map<String, List<BuildingFixture>> buildingsByMod = new LinkedHashMap<>();
    private final Map<String, List<TileFixture>> tilesByMod = new LinkedHashMap<>();
    private String overriddenBuildingId;
    private String lastCheckedTileId;
    private boolean needsMarkerTiles;

    private ModRegistry registry;
    private ModLoadException thrown;

    @Before
    public void createModsRoot() throws IOException {
        modsRoot = Files.createTempDirectory("veil-mods-test");
    }

    @After
    public void deleteModsRoot() throws IOException {
        try (Stream<Path> paths = Files.walk(modsRoot)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    @Given("a mods directory containing the {string} mod with a building declaring id {string}")
    public void aModsDirectoryContainingTheModWithABuildingDeclaringId(String modId, String buildingId) {
        addBuilding(modId, buildingId, null, null);
    }

    @Given("the mods directory also contains mod {string} with a building declaring id {string}")
    public void theModsDirectoryAlsoContainsModWithABuildingDeclaringId(String modId, String buildingId) {
        addBuilding(modId, buildingId, null, null);
    }

    @Given("the mods directory also contains mod {string} with a building declaring id {string} and no {string} field")
    public void theModsDirectoryAlsoContainsModWithABuildingDeclaringIdAndNoField(String modId, String buildingId, String fieldName) {
        addBuilding(modId, buildingId, null, null);
    }

    @Given("the mods directory also contains mod {string} with a building declaring id {string} and an {string} field of {string}")
    public void theModsDirectoryAlsoContainsModWithABuildingDeclaringIdAndAnFieldOf(String modId, String buildingId, String fieldName, String overriddenId) {
        addBuilding(modId, buildingId, overriddenId, null);
        overriddenBuildingId = overriddenId;
    }

    @Given("mod {string} declares a {string} of {string}")
    public void modDeclaresADependsOnOf(String modId, String fieldName, String dependsOnId) {
        dependsOnByMod.computeIfAbsent(modId, k -> new ArrayList<>()).add(dependsOnId);
    }

    @Given("a mods directory containing mod {string} with a malformed building file")
    public void aModsDirectoryContainingModWithAMalformedBuildingFile(String modId) throws IOException {
        Path modDir = modsRoot.resolve(modId);
        Files.createDirectories(modDir.resolve("buildings"));
        Files.writeString(modDir.resolve("mod.json"), "{\"id\":\"" + modId + "\",\"dependsOn\":[]}");
        Files.writeString(modDir.resolve("buildings").resolve("broken.json"), "{ not valid json");
    }

    @Given("a mods directory containing mod {string} with a malformed mod.json file")
    public void aModsDirectoryContainingModWithAMalformedModJsonFile(String modId) throws IOException {
        Path modDir = modsRoot.resolve(modId);
        Files.createDirectories(modDir);
        Files.writeString(modDir.resolve("mod.json"), "{ not valid json");
    }

    @Given("a mods directory containing the {string} mod with a tile declaring id {string}, symbol {string}, color \\({int}, {int}, {int}), and walkable {word}")
    public void aModsDirectoryContainingTheModWithATileDeclaringId(String modId, String tileId, String symbol,
                                                                    int r, int g, int b, String walkable) {
        addTile(modId, tileId, symbol.charAt(0), r, g, b, Boolean.parseBoolean(walkable), null);
    }

    @Given("the mods directory also contains mod {string} with a tile declaring id {string} and no {string} field")
    public void theModsDirectoryAlsoContainsModWithATileDeclaringIdAndNoField(String modId, String tileId, String fieldName) {
        addTile(modId, tileId, '?', 0, 0, 0, true, null);
    }

    @Given("the mods directory also contains mod {string} with a tile declaring id {string}, symbol {string}, color \\({int}, {int}, {int}), and walkable {word}, whose {string} field names {string}")
    public void theModsDirectoryAlsoContainsModWithATileDeclaringIdOverriding(String modId, String tileId, String symbol,
                                                                               int r, int g, int b, String walkable,
                                                                               String fieldName, String overriddenId) {
        addTile(modId, tileId, symbol.charAt(0), r, g, b, Boolean.parseBoolean(walkable), overriddenId);
    }

    @Given("the mods directory also contains a building declaring id {string} whose blueprint is a single tile {string}")
    public void theModsDirectoryAlsoContainsABuildingDeclaringIdWhoseBlueprintIsASingleTile(String buildingId, String tileId) {
        String modId = buildingId.split(":")[0];
        addBuilding(modId, buildingId, null, tileId);
    }

    @Given("a mods directory containing mod {string} with a malformed tile file")
    public void aModsDirectoryContainingModWithAMalformedTileFile(String modId) throws IOException {
        Path modDir = modsRoot.resolve(modId);
        Files.createDirectories(modDir.resolve("tiles"));
        Files.writeString(modDir.resolve("mod.json"), "{\"id\":\"" + modId + "\",\"dependsOn\":[]}");
        Files.writeString(modDir.resolve("tiles").resolve("broken.json"), "{ not valid json");
    }

    @When("the mods directory is loaded")
    public void theModsDirectoryIsLoaded() throws IOException {
        writeFixtures();
        try {
            registry = ModLoader.load(modsRoot);
        } catch (ModLoadException e) {
            thrown = e;
        }
    }

    @Then("a building with ID {string} is available")
    public void aBuildingWithIDIsAvailable(String id) {
        assertNotNull(registry, "loading did not complete: " + (thrown == null ? "unknown" : thrown.getMessage()));
        assertNotNull(registry.getBuilding(id), "expected building '" + id + "' to be loaded");
    }

    @Then("a tile with ID {string} is available")
    public void aTileWithIDIsAvailable(String id) {
        assertNotNull(registry, "loading did not complete: " + (thrown == null ? "unknown" : thrown.getMessage()));
        assertNotNull(registry.getTile(id), "expected tile '" + id + "' to be loaded");
        lastCheckedTileId = id;
    }

    @Then("its symbol is {string}")
    public void itsSymbolIs(String symbol) {
        assertEquals(symbol.charAt(0), registry.getTile(lastCheckedTileId).getSymbol());
    }

    @Then("its color is \\({int}, {int}, {int})")
    public void itsColorIs(int r, int g, int b) {
        assertEquals(new Color(r, g, b), registry.getTile(lastCheckedTileId).getColor());
    }

    @Then("it is walkable")
    public void itIsWalkable() {
        assertTrue(registry.getTile(lastCheckedTileId).isWalkable());
    }

    @Then("it is not walkable")
    public void itIsNotWalkable() {
        assertFalse(registry.getTile(lastCheckedTileId).isWalkable());
    }

    @Then("the building {string}'s blueprint at \\({int}, {int}) references tile {string}")
    public void theBuildingsBlueprintAtReferencesTile(String buildingId, int x, int y, String expectedTileId) {
        Tile[][] blueprint = registry.getBuilding(buildingId).getBlueprint();
        assertEquals(registry.getTile(expectedTileId), blueprint[y][x]);
    }

    @Then("loading fails with a ModLoadException naming the colliding ID {string} and both mods {string} and {string}")
    public void loadingFailsWithAModLoadExceptionNamingTheCollidingIDAndBothMods(String id, String modA, String modB) {
        assertNotNull(thrown, "expected a ModLoadException to be thrown");
        assertTrue(thrown.getMessage().contains(id), "expected message to name id: " + thrown.getMessage());
        assertTrue(thrown.getMessage().contains(modA), "expected message to name mod: " + modA);
        assertTrue(thrown.getMessage().contains(modB), "expected message to name mod: " + modB);
    }

    @Then("its blueprint matches the one from mod {string}, not {string}")
    public void itsBlueprintMatchesTheOneFromModNot(String overridingMod, String originalMod) {
        Tile[][] blueprint = registry.getBuilding(overriddenBuildingId).getBlueprint();
        assertEquals(registry.getTile("test:stone"), blueprint[0][0]);
    }

    @Then("mod {string} finishes loading before mod {string} starts loading")
    public void modFinishesLoadingBeforeModStartsLoading(String earlierMod, String laterMod) {
        List<String> order = registry.getModLoadOrder();
        assertTrue(order.indexOf(earlierMod) < order.indexOf(laterMod));
    }

    @Then("a ModLoadException is thrown wrapping the underlying cause")
    public void aModLoadExceptionIsThrownWrappingTheUnderlyingCause() {
        assertNotNull(thrown, "expected a ModLoadException to be thrown");
        assertNotNull(thrown.getCause(), "expected the ModLoadException to wrap an underlying cause");
    }

    private void addBuilding(String modId, String buildingId, String overriddenId, String explicitTileId) {
        dependsOnByMod.computeIfAbsent(modId, k -> new ArrayList<>());
        buildingsByMod.computeIfAbsent(modId, k -> new ArrayList<>())
                .add(new BuildingFixture(buildingId, overriddenId, explicitTileId));
        if (explicitTileId == null) {
            needsMarkerTiles = true;
        }
    }

    private void addTile(String modId, String tileId, char symbol, int r, int g, int b, boolean walkable, String overrides) {
        dependsOnByMod.computeIfAbsent(modId, k -> new ArrayList<>());
        tilesByMod.computeIfAbsent(modId, k -> new ArrayList<>())
                .add(new TileFixture(tileId, symbol, r, g, b, walkable, overrides));
    }

    private void writeFixtures() throws IOException {
        Set<String> allMods = new LinkedHashSet<>();
        allMods.addAll(buildingsByMod.keySet());
        allMods.addAll(tilesByMod.keySet());
        allMods.addAll(dependsOnByMod.keySet());

        if (needsMarkerTiles) {
            writeMarkerTiles();
        }

        for (String modId : allMods) {
            Path modDir = modsRoot.resolve(modId);
            Files.createDirectories(modDir);
            writeManifest(modDir, modId);
            writeTiles(modDir, tilesByMod.getOrDefault(modId, List.of()));
            writeBuildings(modDir, buildingsByMod.getOrDefault(modId, List.of()));
        }
    }

    private void writeMarkerTiles() throws IOException {
        Path markerDir = modsRoot.resolve("_markers");
        Files.createDirectories(markerDir);
        Files.writeString(markerDir.resolve("mod.json"), "{\"id\":\"_markers\",\"dependsOn\":[]}");

        Path tilesDir = markerDir.resolve("tiles");
        Files.createDirectories(tilesDir);
        Files.writeString(tilesDir.resolve("test_grass.json"), tileJson("test:grass", ',', 0, 200, 0, true, null));
        Files.writeString(tilesDir.resolve("test_stone.json"), tileJson("test:stone", '#', 100, 100, 100, false, null));
    }

    private void writeManifest(Path modDir, String modId) throws IOException {
        JsonObject manifest = new JsonObject();
        manifest.addProperty("id", modId);
        JsonArray dependsOn = new JsonArray();
        dependsOnByMod.getOrDefault(modId, List.of()).forEach(dependsOn::add);
        manifest.add("dependsOn", dependsOn);
        Files.writeString(modDir.resolve("mod.json"), manifest.toString());
    }

    private void writeTiles(Path modDir, List<TileFixture> fixtures) throws IOException {
        if (fixtures.isEmpty()) {
            return;
        }
        Path tilesDir = modDir.resolve("tiles");
        Files.createDirectories(tilesDir);

        int i = 0;
        for (TileFixture fixture : fixtures) {
            String json = tileJson(fixture.id(), fixture.symbol(), fixture.r(), fixture.g(), fixture.b(),
                    fixture.walkable(), fixture.overrides());
            Files.writeString(tilesDir.resolve("tile_" + (i++) + ".json"), json);
        }
    }

    private String tileJson(String id, char symbol, int r, int g, int b, boolean walkable, String overrides) {
        JsonObject tile = new JsonObject();
        tile.addProperty("id", id);
        tile.addProperty("symbol", String.valueOf(symbol));
        JsonObject color = new JsonObject();
        color.addProperty("r", r);
        color.addProperty("g", g);
        color.addProperty("b", b);
        tile.add("color", color);
        tile.addProperty("walkable", walkable);
        if (overrides != null) {
            tile.addProperty("overrides", overrides);
        }
        return tile.toString();
    }

    private void writeBuildings(Path modDir, List<BuildingFixture> fixtures) throws IOException {
        if (fixtures.isEmpty()) {
            return;
        }
        Path buildingsDir = modDir.resolve("buildings");
        Files.createDirectories(buildingsDir);

        int i = 0;
        for (BuildingFixture fixture : fixtures) {
            String tileId = fixture.explicitTileId() != null
                    ? fixture.explicitTileId()
                    : (fixture.overrides() != null ? "test:stone" : "test:grass");

            JsonObject building = new JsonObject();
            building.addProperty("id", fixture.id());
            building.addProperty("name", fixture.id());
            building.addProperty("width", 1);
            building.addProperty("height", 1);

            JsonArray row = new JsonArray();
            row.add(tileId);
            JsonArray tiles = new JsonArray();
            tiles.add(row);
            building.add("tiles", tiles);

            if (fixture.overrides() != null) {
                building.addProperty("overrides", fixture.overrides());
            }

            Files.writeString(buildingsDir.resolve("building_" + (i++) + ".json"), building.toString());
        }
    }
}
