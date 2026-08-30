package com.swiftfaze.veil;

import com.swiftfaze.veil.game.GamePanel;
import com.swiftfaze.veil.ui.EastPanel;
import com.swiftfaze.veil.ui.GameWindow;
import com.swiftfaze.veil.ui.NorthPanel;
import com.swiftfaze.veil.ui.SettingsScreenPanel;
import com.swiftfaze.veil.ui.SouthPanel;
import com.swiftfaze.veil.ui.TitleScreenPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        loadGame();
    }

    private static void loadGame() {
        JFrame frame = new JFrame("Veil");
        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);

        // Build game view components (for the game card)
        NorthPanel northPanel = new NorthPanel();
        SouthPanel southPanel = new SouthPanel();
        EastPanel eastPanel = new EastPanel();
        GamePanel gamePanel = new GamePanel();

        gamePanel.addGameListener(eastPanel);
        eastPanel.setRestoreGameFocusAction(gamePanel::requestFocusInWindow);

        JLayeredPane gameContentArea = GameWindow.buildContentArea(gamePanel, eastPanel);

        // Build game card: North + South + Center layout
        JPanel gameCard = new JPanel(new BorderLayout());
        gameCard.add(northPanel, BorderLayout.NORTH);
        gameCard.add(southPanel, BorderLayout.SOUTH);
        gameCard.add(gameContentArea, BorderLayout.CENTER);

        // Build title screen card
        TitleScreenPanel titleScreen = new TitleScreenPanel(menuItem -> {
            switch (menuItem) {
                case "New" -> {
                    cardLayout.show(cardPanel, "game");
                    gamePanel.requestFocusInWindow();
                    gamePanel.startGameLoop();
                }
                case "Settings" -> cardLayout.show(cardPanel, "settings");
                case "Keybinds" -> cardLayout.show(cardPanel, "keybinds");
                // Continue, Load, Exit: placeholders, do nothing
            }
        });

        // Build settings card
        SettingsScreenPanel settingsScreen = new SettingsScreenPanel(
            screen -> {
                if ("title".equals(screen)) {
                    cardLayout.show(cardPanel, "title");
                    titleScreen.requestFocusInWindow();
                } else if ("keybinds".equals(screen)) {
                    cardLayout.show(cardPanel, "keybinds");
                }
            },
            folder -> {
                // Open folder actions are mocked in tests
            }
        );

        // Placeholder keybinds card (will be replaced in phase 4)
        JPanel keybindsCard = new JPanel();
        keybindsCard.setBackground(Color.BLACK);

        cardPanel.add(titleScreen, "title");
        cardPanel.add(gameCard, "game");
        cardPanel.add(settingsScreen, "settings");
        cardPanel.add(keybindsCard, "keybinds");

        frame.setLayout(new BorderLayout());
        frame.add(cardPanel, BorderLayout.CENTER);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        cardLayout.show(cardPanel, "title");
        titleScreen.requestFocusInWindow();
        keyListen(frame);
    }

    private static void keyListen(JFrame frame) {
        JRootPane rootPane = frame.getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "resetGame");
        rootPane.getActionMap().put("resetGame", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                resetGame(frame);
            }
        });
    }

    private static void resetGame(JFrame oldFrame) {
        try {
            oldFrame.dispose(); // closes old window, releases its listeners
            loadGame();
            logger.info("Scene Reset");
        } catch (Exception e) {
            logger.error("Reset failed", e); // pass the Throwable, not e.getMessage()
        }
    }


}
