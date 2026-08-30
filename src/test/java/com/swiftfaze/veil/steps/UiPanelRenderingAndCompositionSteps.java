package com.swiftfaze.veil.steps;

import com.swiftfaze.veil.GameConst;
import com.swiftfaze.veil.entities.player.Player;
import com.swiftfaze.veil.input.Keybindings;
import com.swiftfaze.veil.ui.EastPanel;
import com.swiftfaze.veil.ui.NorthPanel;
import com.swiftfaze.veil.ui.PlayerInfoPanel;
import com.swiftfaze.veil.ui.SouthPanel;
import com.swiftfaze.veil.ui.TerminalPanel;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UiPanelRenderingAndCompositionSteps {

    private TerminalPanel panel;
    private PlayerInfoPanel playerInfoPanel;
    private NorthPanel northPanel;
    private SouthPanel southPanel;
    private EastPanel eastPanel;
    private Player player;
    private JPanel currentPanel;

    @Given("a new instance of a panel extending TerminalPanel")
    public void aNewInstanceOfAPanelExtendingTerminalPanel() {
        panel = new PlayerInfoPanel();
    }

    @Then("the panel's background is black")
    public void thePanelsBackgroundIsBlack() {
        assertEquals(Color.BLACK, panel.getBackground());
    }

    @Then("the panel is not focusable")
    public void thePanelIsNotFocusable() {
        assertFalse(panel.isFocusable());
    }

    @Then("a label made by the panel is white and monospaced")
    public void aLabelMadeByThePanelIsWhiteAndMonospaced() {
        JLabel label = (JLabel) panel.getComponents()[0];
        assertEquals(Color.WHITE, label.getForeground());
        Font font = label.getFont();
        assertEquals(Font.MONOSPACED, font.getName());
    }

    @Then("a label made by the panel without an explicit alignment is left-aligned")
    public void aLabelMadeByThePanelWithoutExplicitAlignmentIsLeftAligned() {
        JLabel label = (JLabel) panel.getComponents()[0];
        assertEquals(Component.LEFT_ALIGNMENT, label.getAlignmentX());
    }

    @Given("a new PlayerInfoPanel")
    public void aNewPlayerInfoPanel() {
        playerInfoPanel = new PlayerInfoPanel();
    }

    @Then("the name label reads {string}")
    public void theNameLabelReads(String expectedText) {
        JLabel nameLabel = (JLabel) playerInfoPanel.getComponents()[0];
        assertEquals(expectedText, nameLabel.getText());
    }

    @Then("the level label reads {string}")
    public void theLevelLabelReads(String expectedText) {
        JLabel levelLabel = (JLabel) playerInfoPanel.getComponents()[1];
        assertEquals(expectedText, levelLabel.getText());
    }

    @Then("the position label reads {string}")
    public void thePositionLabelReads(String expectedText) {
        JLabel positionLabel = (JLabel) playerInfoPanel.getComponents()[2];
        assertEquals(expectedText, positionLabel.getText());
    }

    @Given("a player named {string} at level {int} with {int} XP at position \\({int}, {int}\\)")
    public void aPlayerNamedAtLevelWithXPAtPosition(String fullName, int level, int xp, int x, int y) {
        String[] nameParts = fullName.split(" ");
        String firstName = nameParts[0];
        String lastName = nameParts[1];

        player = new Player(x, y);
        player.getPlayerInfo().setFirstName(firstName);
        player.getPlayerInfo().setLastName(lastName);
        player.getPlayerInfo().getLevel().setCurrentLevel(level);
        player.getPlayerInfo().getLevel().setXp(xp);
    }

    @When("the panel is updated with that player")
    public void thePanelIsUpdatedWithThatPlayer() {
        playerInfoPanel.updatePlayer(player);
    }

    @Given("a new NorthPanel")
    public void aNewNorthPanel() {
        northPanel = new NorthPanel();
        currentPanel = northPanel;
    }

    @Then("the panel's preferred width equals the game window width")
    public void thePanelsPreferredWidthEqualsTheGameWindowWidth() {
        assertEquals(GameConst.GAME_WINDOW_WIDTH, currentPanel.getPreferredSize().width);
    }

    @Then("the panel's preferred height equals 4 times the tile height")
    public void thePanelsPreferredHeightEquals4TimesTheTileHeight() {
        assertEquals(4 * GameConst.TILE_HEIGHT, currentPanel.getPreferredSize().height);
    }

    @Then("the panel has a light-gray border")
    public void thePanelHasALightGrayBorder() {
        Border border = currentPanel.getBorder();
        assertTrue(border instanceof LineBorder);
        LineBorder lineBorder = (LineBorder) border;
        assertEquals(Color.LIGHT_GRAY, lineBorder.getLineColor());
    }

    @Then("the panel displays a centered title reading {string}")
    public void thePanelDisplaysACenteredTitleReading(String expectedTitle) {
        JLabel titleLabel = (JLabel) northPanel.getComponents()[1];
        assertEquals(expectedTitle, titleLabel.getText());
    }

    @Given("a new SouthPanel")
    public void aNewSouthPanel() {
        southPanel = new SouthPanel();
        currentPanel = southPanel;
    }

    @Then("the panel's preferred height equals 4 times the game window height")
    public void thePanelsPreferredHeightEquals4TimesTheGameWindowHeight() {
        assertEquals(GameConst.GAME_WINDOW_HEIGHT * 4, currentPanel.getPreferredSize().height);
    }

    @Given("a new EastPanel")
    public void aNewEastPanel() {
        eastPanel = new EastPanel();
    }

    @Then("its player info panel is in the north of the layout")
    public void itsPlayerInfoPanelIsInTheNorthOfTheLayout() {
        BorderLayout layout = (BorderLayout) eastPanel.getLayout();
        Component north = layout.getLayoutComponent(BorderLayout.NORTH);
        assertTrue(north instanceof PlayerInfoPanel);
    }

    @Then("its inventory panel is in the center of the layout")
    public void itsInventoryPanelIsInTheCenterOfTheLayout() {
        BorderLayout layout = (BorderLayout) eastPanel.getLayout();
        Component center = layout.getLayoutComponent(BorderLayout.CENTER);
        assertEquals(eastPanel.getInventoryPanel(), center);
    }

    @Then("its menu panel is in the south of the layout")
    public void itsMenuPanelIsInTheSouthOfTheLayout() {
        BorderLayout layout = (BorderLayout) eastPanel.getLayout();
        Component south = layout.getLayoutComponent(BorderLayout.SOUTH);
        assertEquals(eastPanel.getMenuPanel(), south);
    }

    @When("EastPanel is updated with that player")
    public void eastPanelIsUpdatedWithThatPlayer() {
        eastPanel.updatePlayer(player);
    }

    @Then("its player info panel's name label reads {string}")
    public void itsPlayerInfoPanelsNameLabelReads(String expectedText) {
        BorderLayout layout = (BorderLayout) eastPanel.getLayout();
        PlayerInfoPanel playerInfoPanel = (PlayerInfoPanel) layout.getLayoutComponent(BorderLayout.NORTH);
        JLabel nameLabel = (JLabel) playerInfoPanel.getComponents()[0];
        assertEquals(expectedText, nameLabel.getText());
    }

    @Then("the player info panel's border has a 2px light-gray bottom line and 10px padding on all sides")
    public void thePlayerInfoPanelsBorderHasBottomLineAndPadding() {
        Border border = playerInfoPanel.getBorder();
        assertTrue(border instanceof CompoundBorder);
        CompoundBorder compound = (CompoundBorder) border;

        Border outside = compound.getOutsideBorder();
        assertTrue(outside instanceof MatteBorder);
        MatteBorder matteBorder = (MatteBorder) outside;
        assertEquals(Color.LIGHT_GRAY, matteBorder.getMatteColor());
        assertEquals(new Insets(0, 0, 2, 0), matteBorder.getBorderInsets());

        Border inside = compound.getInsideBorder();
        assertTrue(inside instanceof EmptyBorder);
        EmptyBorder emptyBorder = (EmptyBorder) inside;
        assertEquals(new Insets(10, 10, 10, 10), emptyBorder.getBorderInsets());
    }

    @Then("the title is colored {string}")
    public void theTitleIsColored(String expectedHex) {
        JLabel titleLabel = (JLabel) northPanel.getComponents()[1];
        assertEquals(Color.decode(expectedHex), titleLabel.getForeground());
    }

    @Then("EastPanel's preferred size is {int} wide and the game window height times the tile height tall")
    public void eastPanelSizeCheck(int expectedWidth) {
        Dimension size = eastPanel.getPreferredSize();
        assertEquals(expectedWidth, size.width);
        assertEquals(GameConst.GAME_WINDOW_HEIGHT * GameConst.TILE_HEIGHT, size.height);
    }

    @Then("EastPanel's background is black")
    public void eastPanelBackgroundIsBlack() {
        assertEquals(Color.BLACK, eastPanel.getBackground());
    }

    @Then("EastPanel is not focusable")
    public void eastPanelIsNotFocusable() {
        assertFalse(eastPanel.isFocusable());
    }

    @Then("EastPanel has a light-gray border")
    public void eastPanelHasLightGrayBorder() {
        Border border = eastPanel.getBorder();
        assertTrue(border instanceof LineBorder);
        LineBorder lineBorder = (LineBorder) border;
        assertEquals(Color.LIGHT_GRAY, lineBorder.getLineColor());
    }

    @Given("its inventory is visible")
    public void itsInventoryIsVisible() {
        eastPanel.getInventoryPanel().setVisible(true);
    }

    @Given("its inventory is hidden")
    public void itsInventoryIsHidden() {
        eastPanel.getInventoryPanel().setVisible(false);
    }

    @Given("a restore-game-focus action is registered")
    public void aRestoreGameFocusActionIsRegistered() {
        // No-op: "the rebuilt in-game menu and inventory screen" step
        // (UiComponentFrameworkSteps) already registers the restore action
        // on the EastPanel that scenario exercises.
    }

}
