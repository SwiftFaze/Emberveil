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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModLoaderSteps {

    private record BuildingFixture(String id, String overrides) {
    }

    private Path modsRoot;
    private final Map<String, List<String>> dependsOnByMod = new LinkedHashMap<>();
    private final Map<String, List<BuildingFixture>> buildingsByMod = new LinkedHashMap<>();
    private String overriddenBuildingId;

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
        addBuilding(modId, buildingId, null);
    }

    @Given("the mods directory also contains mod {string} with a building declaring id {string}")
    public void theModsDirectoryAlsoContainsModWithABuildingDeclaringId(String modId, String buildingId) {
        addBuilding(modId, buildingId, null);
    }

    @Given("the mods directory also contains mod {string} with a building declaring id {string} and no {string} field")
    public void theModsDirectoryAlsoContainsModWithABuildingDeclaringIdAndNoField(String modId, String buildingId, String fieldName) {
        addBuilding(modId, buildingId, null);
    }

    @Given("the mods directory also contains mod {string} with a building declaring id {string} and an {string} field of {string}")
    public void theModsDirectoryAlsoContainsModWithABuildingDeclaringIdAndAnFieldOf(String modId, String buildingId, String fieldName, String overriddenId) {
        addBuilding(modId, buildingId, overriddenId);
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
        assertEquals(Tile.STONE, blueprint[0][0]);
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

    private void addBuilding(String modId, String buildingId, String overriddenId) {
        dependsOnByMod.computeIfAbsent(modId, k -> new ArrayList<>());
        buildingsByMod.computeIfAbsent(modId, k -> new ArrayList<>())
                .add(new BuildingFixture(buildingId, overriddenId));
    }

    private void writeFixtures() throws IOException {
        for (String modId : buildingsByMod.keySet()) {
            Path modDir = modsRoot.resolve(modId);
            Files.createDirectories(modDir);

            JsonObject manifest = new JsonObject();
            manifest.addProperty("id", modId);
            JsonArray dependsOn = new JsonArray();
            dependsOnByMod.getOrDefault(modId, List.of()).forEach(dependsOn::add);
            manifest.add("dependsOn", dependsOn);
            Files.writeString(modDir.resolve("mod.json"), manifest.toString());

            Path buildingsDir = modDir.resolve("buildings");
            Files.createDirectories(buildingsDir);

            int i = 0;
            for (BuildingFixture fixture : buildingsByMod.get(modId)) {
                JsonObject building = new JsonObject();
                building.addProperty("id", fixture.id());
                building.addProperty("name", fixture.id());
                building.addProperty("width", 1);
                building.addProperty("height", 1);

                JsonArray row = new JsonArray();
                row.add(fixture.overrides() != null ? "STONE" : "GRASS");
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
}
