package com.swiftfaze.emberveil.game;

import com.swiftfaze.emberveil.entities.player.Player;

import javax.swing.*;
import java.awt.*;

import static com.swiftfaze.emberveil.game.GamePanel.CHAR_HEIGHT;
import static com.swiftfaze.emberveil.game.GamePanel.GAME_HEIGHT;

public class EastPanel extends JPanel implements GameListener {

    private final PlayerInfoPanel playerInfoPanel;
    private final InventoryPanel inventoryPanel;
    private final MenuPanel menuPanel;

    public EastPanel() {
        setPreferredSize(new Dimension(500, GAME_HEIGHT * CHAR_HEIGHT));
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        setFocusable(false);

        playerInfoPanel = new PlayerInfoPanel();
        menuPanel = new MenuPanel();
        inventoryPanel = new InventoryPanel();

        add(playerInfoPanel, BorderLayout.NORTH);
        add(inventoryPanel, BorderLayout.CENTER);
        add(menuPanel, BorderLayout.SOUTH);
    }

    public void toggleInventory() {
        inventoryPanel.setVisible(!inventoryPanel.isVisible());
        revalidate();
        repaint();
    }

    public InventoryPanel getInventoryPanel() {
        return inventoryPanel;
    }

    @Override
    public void updatePlayer(Player player) {
        playerInfoPanel.updatePlayer(player);
    }
}
