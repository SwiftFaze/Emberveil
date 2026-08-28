package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.entities.player.Player;
import com.swiftfaze.veil.game.GameListener;

import javax.swing.*;
import java.awt.*;

import static com.swiftfaze.veil.GameConst.TILE_HEIGHT;
import static com.swiftfaze.veil.GameConst.GAME_WINDOW_HEIGHT;


public class EastPanel extends JPanel implements GameListener {

    private final PlayerInfoPanel playerInfoPanel;
    private final InventoryPanel inventoryPanel;
    private final MenuPanel menuPanel;
    private Runnable restoreGameFocus = () -> {
    };

    public EastPanel() {
        setPreferredSize(new Dimension(500, GAME_WINDOW_HEIGHT * TILE_HEIGHT));
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        setFocusable(false);

        playerInfoPanel = new PlayerInfoPanel();
        menuPanel = new MenuPanel();
        inventoryPanel = new InventoryPanel();
        menuPanel.setOnInventoryConfirmed(this::toggleInventory);
        menuPanel.setOnCancel(this::cancelMenu);

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
        boolean opening = !inventoryPanel.isVisible();
        inventoryPanel.setVisible(opening);
        revalidate();
        repaint();
        if (opening) {
            menuPanel.requestFocusInWindow();
        } else {
            restoreGameFocus.run();
        }
    }

    private void cancelMenu() {
        if (inventoryPanel.isVisible()) {
            inventoryPanel.setVisible(false);
            revalidate();
            repaint();
        }
        restoreGameFocus.run();
    }

    public InventoryPanel getInventoryPanel() {
        return inventoryPanel;
    }

    @Override
    public void updatePlayer(Player player) {
        playerInfoPanel.updatePlayer(player);
    }
}
