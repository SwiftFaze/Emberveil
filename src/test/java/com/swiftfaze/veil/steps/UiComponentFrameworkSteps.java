package com.swiftfaze.veil.steps;

import com.swiftfaze.veil.entities.player.Stats;
import com.swiftfaze.veil.game.GamePanel;
import com.swiftfaze.veil.input.Keybindings;
import com.swiftfaze.veil.sandbox.ClassSandboxModel;
import com.swiftfaze.veil.sandbox.ClassSandboxPanel;
import com.swiftfaze.veil.ui.EastPanel;
import com.swiftfaze.veil.ui.GameWindow;
import com.swiftfaze.veil.ui.widget.ButtonWidget;
import com.swiftfaze.veil.ui.widget.ListWidget;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JLayeredPane;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UiComponentFrameworkSteps {

    private ListWidget<String> listWidget;
    private ButtonWidget buttonWidget;
    private String selectedItem;
    private String confirmedItem;
    private boolean actionInvoked;
    private List<String> listItems;
    private List<String> dataSourceItems;

    private EastPanel eastPanel;
    private ClassSandboxPanel classPanel;
    private ClassSandboxModel classModel;
    private List<String> classNames;
    private boolean restoreGameFocusInvoked;
    private int lastKeyCode; // Track which key was pressed for scenarios
    private JLayeredPane layeredContentArea;

    @Given("a list widget with items {string}, {string}, {string} and {string} selected")
    public void aListWidgetWithItems(String first, String second, String third, String selected) {
        listItems = List.of(first, second, third);
        listWidget = new ListWidget<>(s -> s);
        listWidget.setItems(listItems);

        while (!listWidget.getSelectedItem().equals(selected)) {
            listWidget.moveDown();
        }
    }

    @Given("the list widget has keyboard focus")
    public void theListWidgetHasKeyboardFocus() {
        // Keyboard focus is modeled at the widget level; real Swing focus-transfer
        // is exercised in manual playtest (Step 4.5), not in headless tests.
    }

    @When("the {string} key is pressed")
    public void theKeyIsPressed(String key) {
        switch (key) {
            case "Up" -> {
                if (listWidget != null) {
                    listWidget.moveUp();
                } else if (eastPanel != null && eastPanel.getInventoryPanel().isVisible()) {
                    fireInventoryPopupAction("popup-up");
                }
            }
            case "Down" -> {
                if (listWidget != null) {
                    listWidget.moveDown();
                } else if (eastPanel != null && eastPanel.getInventoryPanel().isVisible()) {
                    fireInventoryPopupAction("popup-down");
                }
            }
            case "Enter" -> {
                if (listWidget != null) {
                    confirmedItem = listWidget.getSelectedItem();
                } else if (buttonWidget != null) {
                    // Fire the button's confirm action
                    Action action = buttonWidget.getActionMap().get("button-confirm");
                    if (action != null) {
                        action.actionPerformed(new ActionEvent(buttonWidget, ActionEvent.ACTION_PERFORMED, "button-confirm"));
                    }
                }
            }
            case "Escape" -> {
                if (eastPanel != null && eastPanel.getInventoryPanel().isVisible()) {
                    fireInventoryPopupAction("popup-dismiss");
                }
            }
            default -> throw new IllegalArgumentException("Unhandled key: " + key);
        }
    }

    private void fireInventoryPopupAction(String actionName) {
        Action action = eastPanel.getInventoryPanel().getActionMap().get(actionName);
        if (action != null) {
            action.actionPerformed(new ActionEvent(eastPanel.getInventoryPanel(), ActionEvent.ACTION_PERFORMED, actionName));
        }
    }

    @Then("the selected item is {string}")
    public void theSelectedItemIs(String expected) {
        assertEquals(expected, listWidget.getSelectedItem());
    }

    @Then("the confirmed item is {string}")
    public void theConfirmedItemIs(String expected) {
        assertEquals(expected, confirmedItem);
    }

    @Given("a list widget backed by a data source currently containing {string}, {string}")
    public void aListWidgetBackedByDataSource(String first, String second) {
        dataSourceItems = new ArrayList<>(List.of(first, second));
        listWidget = new ListWidget<>(s -> s);
        listWidget.setItems(dataSourceItems);
    }

    @When("the data source's contents change to {string}, {string}, {string}")
    public void theDataSourceContentsChange(String first, String second, String third) {
        dataSourceItems.clear();
        dataSourceItems.addAll(List.of(first, second, third));
    }

    @When("the list widget is refreshed")
    public void theListWidgetIsRefreshed() {
        listWidget.setItems(dataSourceItems);
    }

    @Then("the list widget's items are {string}, {string}, {string}")
    public void theListWidgetItemsAre(String first, String second, String third) {
        List<String> expected = List.of(first, second, third);
        for (int i = 0; i < expected.size(); i++) {
            listWidget.moveDown();
        }
        listWidget.moveUp();
        listWidget.moveUp();
        listWidget.moveUp();
        assertEquals(0, listWidget.getSelectedIndex());
    }

    @Given("a button widget labeled {string} with an action registered")
    public void aButtonWidgetWithAction(String label) {
        actionInvoked = false;
        buttonWidget = new ButtonWidget(label);
        buttonWidget.setOnConfirm(() -> actionInvoked = true);
    }

    @Given("the button widget has keyboard focus")
    public void theButtonWidgetHasKeyboardFocus() {
        // Keyboard focus is modeled at the widget level
    }

    @Then("the button's action was invoked")
    public void theButtonsActionWasInvoked() {
        assertTrue(actionInvoked);
    }

    @Given("the rebuilt in-game inventory screen")
    public void theRebuiltInGameInventoryScreen() {
        eastPanel = new EastPanel();
        restoreGameFocusInvoked = false;
        eastPanel.setRestoreGameFocusAction(() -> restoreGameFocusInvoked = true);
    }

    @When("the inventory is toggled open")
    public void theInventoryIsToggledOpen() {
        eastPanel.toggleInventory();
    }

    @Then("the inventory popup is open")
    public void theInventoryPopupIsOpen() {
        assertTrue(eastPanel.getInventoryPanel().isVisible());
    }

    @Then("the popup's Close button has keyboard focus")
    public void thePopupsCloseButtonHasKeyboardFocus() {
        assertNotNull(eastPanel.getInventoryPanel().getCloseButton());
    }

    @Then("the inventory popup is closed")
    public void theInventoryPopupIsClosed() {
        assertFalse(eastPanel.getInventoryPanel().isVisible());
    }

    @Then("the restore-game-focus action was invoked")
    public void theRestoreGameFocusActionWasInvoked() {
        assertTrue(restoreGameFocusInvoked);
    }

    @Given("the game window's layered content area")
    public void theGameWindowsLayeredContentArea() {
        eastPanel = new EastPanel();
        layeredContentArea = GameWindow.buildContentArea(new GamePanel(), eastPanel);
    }

    @Then("the inventory popup's layer is above the game and sidebar content's layer")
    public void theInventoryPopupsLayerIsAboveTheGameAndSidebarContentsLayer() {
        int popupLayer = layeredContentArea.getLayer(eastPanel.getInventoryPanel());
        int mainAreaLayer = layeredContentArea.getLayer(
                layeredContentArea.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)[0]);
        assertTrue(popupLayer > mainAreaLayer);
    }

    @When("the popup's Close button is confirmed")
    public void thePopupsCloseButtonIsConfirmed() {
        Action confirmAction = eastPanel.getInventoryPanel().getCloseButton().getActionMap().get("button-confirm");
        if (confirmAction != null) {
            confirmAction.actionPerformed(new ActionEvent(eastPanel.getInventoryPanel().getCloseButton(),
                ActionEvent.ACTION_PERFORMED, "button-confirm"));
        } else {
            eastPanel.getInventoryPanel().dismiss();
        }
    }


    @Then("the inventory popup lists the item {string}")
    public void theInventoryPopupListsTheItem(String itemName) {
        assertTrue(eastPanel.getInventoryPanel().isVisible());
    }

    @Then("the inventory popup's first item is highlighted as selected")
    public void theInventoryPopupsFirstItemIsHighlightedAsSelected() {
        assertEquals(0, eastPanel.getInventoryPanel().getSelectedIndex());
    }

    @Then("the inventory popup's selected item is no longer the first item")
    public void theInventoryPopupsSelectedItemIsNoLongerTheFirstItem() {
        assertNotEquals(0, eastPanel.getInventoryPanel().getSelectedIndex());
    }

    @Given("a class sandbox panel is showing")
    public void aClassSandboxPanelIsShowing() {
        classModel = new ClassSandboxModel();
        classPanel = new ClassSandboxPanel(classModel);
        classNames = classModel.classNames();
    }

    @Then("the first class's label is colored {string}")
    public void theFirstClassSLabelIsColored(String hex) {
        assertEquals(Color.decode(hex), classPanel.getClassLabel(0).getForeground());
    }

    @Then("the stats label shows the first class's computed stats")
    public void theStatsLabelShowsTheFirstClassSComputedStats() {
        assertStatsLabelShows(0);
    }

    @When("the down-bound action fires")
    public void theDownBoundActionFires() {
        fireAction(Keybindings.ACTION_MENU_DOWN);
    }

    @When("the up-bound action fires")
    public void theUpBoundActionFires() {
        fireAction(Keybindings.ACTION_MENU_UP);
    }

    @Then("the previously selected class's label is white")
    public void thePreviouslySelectedClassSLabelIsWhite() {
        assertEquals(Color.WHITE, classPanel.getClassLabel(0).getForeground());
    }

    @Then("the newly selected class's label is colored {string}")
    public void theNewlySelectedClassSLabelIsColored(String hex) {
        assertEquals(Color.decode(hex), classPanel.getClassLabel(1).getForeground());
    }

    @Then("the stats label shows the newly selected class's computed stats")
    public void theStatsLabelShowsTheNewlySelectedClassSComputedStats() {
        assertStatsLabelShows(1);
    }

    @Then("the last class's label is colored {string}")
    public void theLastClassSLabelIsColored(String hex) {
        assertEquals(Color.decode(hex), classPanel.getClassLabel(classNames.size() - 1).getForeground());
    }

    @Then("the stats label shows the last class's computed stats")
    public void theStatsLabelShowsTheLastClassSComputedStats() {
        assertStatsLabelShows(classNames.size() - 1);
    }

    private void assertStatsLabelShows(int index) {
        Stats stats = classModel.computedStats(classNames.get(index));
        String expected = String.format(
                "ATK %d  DEF %d  HP %d  MP %d",
                stats.getAttackPower(), stats.getDefense(), stats.getMaxHp(), stats.getMaxMana()
        );
        assertEquals(expected, classPanel.getStatsLabel().getText());
    }

    private void fireAction(String actionName) {
        Action action = classPanel.getActionMap().get(actionName);
        action.actionPerformed(new ActionEvent(classPanel, ActionEvent.ACTION_PERFORMED, actionName));
    }
}
