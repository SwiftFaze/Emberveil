package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.entities.player.Player;
import com.swiftfaze.veil.game.GameListener;
import com.swiftfaze.veil.mods.ModLoader;
import com.swiftfaze.veil.ui.widget.FocusManager;

import javax.swing.*;
import java.awt.*;

import static com.swiftfaze.veil.GameConst.TILE_HEIGHT;
import static com.swiftfaze.veil.GameConst.GAME_WINDOW_HEIGHT;


public class EastPanel extends JPanel implements GameListener {

    private final PlayerInfoPanel playerInfoPanel;
    private final InventoryPanel inventoryPanel;
    private final MenuPanel menuPanel;
    private final FocusManager focusManager;
    private Runnable restoreGameFocus = () -> {
    };

    public EastPanel() {
        setPreferredSize(new Dimension(500, GAME_WINDOW_HEIGHT * TILE_HEIGHT));
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        setFocusable(false);

        focusManager = new FocusManager();

        playerInfoPanel = new PlayerInfoPanel();
        menuPanel = new MenuPanel();
        menuPanel.setFocusManager(focusManager);
        inventoryPanel = new InventoryPanel();
        inventoryPanel.setFocusManager(focusManager);
        inventoryPanel.showItems(ModLoader.load(java.nio.file.Paths.get("mods")).getAllItems());

        menuPanel.setOnInventoryConfirmed(this::openInventory);
        menuPanel.setOnCancel(this::cancelMenu);
        inventoryPanel.setOnDismiss(this::onInventoryDismissed);

        add(playerInfoPanel, BorderLayout.NORTH);
        add(inventoryPanel, BorderLayout.CENTER);
        add(menuPanel, BorderLayout.SOUTH);
    }

    /**
     * Lets GamePanel reclaim keyboard focus once the menu is dismissed —
     * MenuPanel only knows how to give focus up via a callback, never a
     * direct GamePanel reference.
     */
    public void setRestoreGameFocusAction(Runnable restoreGameFocus) {
        this.restoreGameFocus = restoreGameFocus;
    }

    @Override
    public void toggleInventory() {
        if (inventoryPanel.isVisible()) {
            inventoryPanel.dismiss();
            restoreGameFocus.run();
        } else {
            openInventory();
        }
    }

    private void openInventory() {
        inventoryPanel.open();
    }

    private void cancelMenu() {
        if (inventoryPanel.isVisible()) {
            inventoryPanel.dismiss();
        } else {
            restoreGameFocus.run();
        }
    }

    private void onInventoryDismissed() {
        menuPanel.requestFocusInWindow();
    }

    public InventoryPanel getInventoryPanel() {
        return inventoryPanel;
    }

    public MenuPanel getMenuPanel() {
        return menuPanel;
    }

    @Override
    public void updatePlayer(Player player) {
        playerInfoPanel.updatePlayer(player);
    }
}
