package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.game.GamePanel;
import org.junit.jupiter.api.Test;

import javax.swing.JLayeredPane;
import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GameWindowTest {

    @Test
    void inventoryPopupIsLayeredAboveTheGameAndSidebarContent() {
        EastPanel eastPanel = new EastPanel();
        GamePanel gamePanel = new GamePanel();

        JLayeredPane layeredPane = GameWindow.buildContentArea(gamePanel, eastPanel);

        int popupLayer = layeredPane.getLayer(eastPanel.getInventoryPanel());
        int mainAreaLayer = layeredPane.getLayer(layeredPane.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)[0]);
        assertTrue(popupLayer > mainAreaLayer);
    }

    @Test
    void thePopupFillsTheSameBoundsAsTheGameAndSidebarContent() {
        EastPanel eastPanel = new EastPanel();
        GamePanel gamePanel = new GamePanel();

        JLayeredPane layeredPane = GameWindow.buildContentArea(gamePanel, eastPanel);
        layeredPane.setBounds(0, 0, 1250, 750);
        layeredPane.doLayout();

        Rectangle popupBounds = eastPanel.getInventoryPanel().getBounds();
        assertTrue(new Rectangle(0, 0, 1250, 750).equals(popupBounds));
    }
}
