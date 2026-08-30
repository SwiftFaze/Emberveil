package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.game.GamePanel;
import com.swiftfaze.veil.ui.widget.FillLayout;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Assembles the game view, sidebar, and any modal popups (e.g. the inventory)
 * into a single layered content area: the game/sidebar sit at
 * {@link JLayeredPane#DEFAULT_LAYER}, popups at {@link JLayeredPane#POPUP_LAYER}
 * above them, so a popup covers the game view instead of living inside the
 * sidebar's own layout.
 */
public class GameWindow {

    public static JLayeredPane buildContentArea(GamePanel gamePanel, EastPanel eastPanel) {
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.add(gamePanel, BorderLayout.CENTER);
        mainArea.add(eastPanel, BorderLayout.EAST);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(mainArea.getPreferredSize());
        layeredPane.setLayout(new FillLayout());

        layeredPane.add(mainArea, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(eastPanel.getInventoryPanel(), JLayeredPane.POPUP_LAYER);

        return layeredPane;
    }
}
