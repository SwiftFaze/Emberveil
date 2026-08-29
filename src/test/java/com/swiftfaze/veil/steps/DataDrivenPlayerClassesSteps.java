package com.swiftfaze.veil.steps;

import com.swiftfaze.veil.entities.player.PlayerClassLoader;
import com.swiftfaze.veil.entities.player.classes.PlayerClass;
import com.swiftfaze.veil.entities.player.Stats;
import com.swiftfaze.veil.exceptions.PlayerClassException;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataDrivenPlayerClassesSteps {

    private PlayerClass loadedClass;
    private List<PlayerClass> loadedClasses;
    private Exception thrown;

    @When("the {string} class is loaded")
    public void theClassIsLoaded(String classFileBaseName) {
        loadedClass = PlayerClassLoader.load(classFileBaseName + ".json");
    }

    @When("all player classes are loaded")
    public void allPlayerClassesAreLoaded() {
        loadedClasses = PlayerClassLoader.loadAll();
    }

    @When("class file {string} is loaded")
    public void classFileIsLoaded(String fileName) {
        thrown = assertThrows(PlayerClassException.class, () -> PlayerClassLoader.load(fileName));
    }

    @Then("the class name is {string}")
    public void theClassNameIs(String expectedName) {
        assertEquals(expectedName, loadedClass.getName());
    }

    @Then("the base max HP is {int}")
    public void theBaseMaxHpIs(int expectedMaxHp) {
        Stats stats = new Stats();
        loadedClass.applyBaseStats(stats);
        assertEquals(expectedMaxHp, stats.getMaxHp());
    }

    @Then("the base max mana is {int}")
    public void theBaseMaxManaIs(int expectedMaxMana) {
        Stats stats = new Stats();
        loadedClass.applyBaseStats(stats);
        assertEquals(expectedMaxMana, stats.getMaxMana());
    }

    @Then("the result includes classes named {string} and {string}")
    public void theResultIncludesClassesNamedAnd(String first, String second) {
        List<String> names = loadedClasses.stream().map(PlayerClass::getName).toList();
        assertTrue(names.contains(first));
        assertTrue(names.contains(second));
    }

    @Then("loading fails with a PlayerClassException")
    public void loadingFailsWithAPlayerClassException() {
        assertTrue(thrown instanceof PlayerClassException);
    }
}
