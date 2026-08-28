package com.swiftfaze.veil.steps;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.swiftfaze.veil.entities.player.PlayerClassLoader;
import com.swiftfaze.veil.entities.player.Stats;
import com.swiftfaze.veil.entities.player.classes.PlayerClass;
import com.swiftfaze.veil.sandbox.ClassSandboxModel;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClassStatsSandboxSteps {

    private static final Gson GSON = new Gson();

    private ClassSandboxModel model;
    private PlayerClass editedClass;
    private Stats selectedStats;

    @Given("the class sandbox is running")
    public void theClassSandboxIsRunning() {
        model = new ClassSandboxModel();
    }

    @When("{string} is selected")
    public void isSelected(String className) {
        selectedStats = model.computedStats(className);
    }

    @Then("the class list includes {string} and {string}")
    public void theClassListIncludesAnd(String first, String second) {
        List<String> names = model.classNames();
        assertTrue(names.contains(first));
        assertTrue(names.contains(second));
    }

    @Then("the displayed attack power is {int}")
    public void theDisplayedAttackPowerIs(int expected) {
        assertEquals(expected, selectedStats.getAttackPower());
    }

    @Then("the displayed defense is {int}")
    public void theDisplayedDefenseIs(int expected) {
        assertEquals(expected, selectedStats.getDefense());
    }

    @Then("the displayed max HP is {int}")
    public void theDisplayedMaxHpIs(int expected) {
        assertEquals(expected, selectedStats.getMaxHp());
    }

    @Then("the displayed max mana is {int}")
    public void theDisplayedMaxManaIs(int expected) {
        assertEquals(expected, selectedStats.getMaxMana());
    }

    /**
     * Simulates an on-disk JSON edit via a Gson round-trip (load the real
     * resource, override one field, re-deserialize) rather than mutating
     * the actual resource file, so the test suite has no filesystem
     * side effects.
     */
    @Given("the {string} class JSON has been edited to set max HP to {int}")
    public void theClassJsonHasBeenEditedToSetMaxHpTo(String classFileBaseName, int newMaxHp) {
        PlayerClass original = PlayerClassLoader.load(classFileBaseName + ".json");
        JsonObject json = GSON.toJsonTree(original).getAsJsonObject();
        json.addProperty("maxHp", newMaxHp);
        editedClass = GSON.fromJson(json, PlayerClass.class);
    }

    @When("the class sandbox is started fresh and {string} is selected")
    public void theClassSandboxIsStartedFreshAndIsSelected(String className) {
        ClassSandboxModel freshModel = new ClassSandboxModel(List.of(editedClass));
        selectedStats = freshModel.computedStats(className);
    }
}
