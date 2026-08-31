package com.swiftfaze.veil;

import com.swiftfaze.veil.game.GamePanel;
import com.swiftfaze.veil.mods.ModLoader;
import com.swiftfaze.veil.mods.ModRegistry;
import com.swiftfaze.veil.mods.WidgetColorTheme;
import com.swiftfaze.veil.ui.EastPanel;
import com.swiftfaze.veil.ui.GameWindow;
import com.swiftfaze.veil.ui.NorthPanel;
import com.swiftfaze.veil.ui.SettingsKeybindsPanel;
import com.swiftfaze.veil.ui.SettingsScreenPanel;
import com.swiftfaze.veil.ui.SettingsWindow;
import com.swiftfaze.veil.ui.SouthPanel;
import com.swiftfaze.veil.ui.TitleScreenPanel;
import com.swiftfaze.veil.ui.widget.WidgetTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        loadGame();
    }

    private static void loadGame() {
        JFrame frame = new JFrame("Veil");
        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);

        loadAndApplyDefaultTheme();

        // Populated as each screen is built below; every screen's navigation callback captures
        // this same map reference, so forward references between screens built later (settings
        // <-> keybinds is a genuine two-way cycle) resolve fine at runtime, once the map is full
        // and before the frame is ever shown - only the map reference itself needs to exist yet
        // when each lambda is written, not its final contents.
        Map<String, JComponent> cards = new HashMap<>();

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
                case "Settings" -> navigateTo(cardLayout, cardPanel, cards, "settings");
                // Continue, Load, Exit: placeholders, do nothing
            }
        });

        // Build settings card
        SettingsScreenPanel settingsScreen = new SettingsScreenPanel(
            screen -> navigateTo(cardLayout, cardPanel, cards, screen),
            Main::openFolder
        );

        JLayeredPane settingsContentArea = SettingsWindow.buildContentArea(settingsScreen);

        // Build keybinds card
        SettingsKeybindsPanel keybindsScreen = new SettingsKeybindsPanel(
            screen -> navigateTo(cardLayout, cardPanel, cards, screen)
        );

        cards.put("title", titleScreen);
        cards.put("settings", settingsScreen);
        cards.put("keybinds", keybindsScreen);

        cardPanel.add(titleScreen, "title");
        cardPanel.add(gameCard, "game");
        cardPanel.add(settingsContentArea, "settings");
        cardPanel.add(keybindsScreen, "keybinds");

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

    // Must run before any screen/widget is constructed below - they read WidgetTheme's
    // statics at construction time. No settings/config system exists yet to pick a
    // non-default theme, so this always applies whichever mod owns ID "core:default".
    private static void loadAndApplyDefaultTheme() {
        ModRegistry mods = ModLoader.load(Paths.get("mods"));
        WidgetColorTheme defaultTheme = mods.getTheme("core:default");
        if (defaultTheme != null) {
            WidgetTheme.applyTheme(defaultTheme);
        }
    }

    private static void navigateTo(CardLayout cardLayout, JPanel cardPanel,
                                    Map<String, JComponent> cards, String cardName) {
        cardLayout.show(cardPanel, cardName);
        JComponent target = cards.get(cardName);
        if (target != null) {
            target.requestFocusInWindow();
        }
    }

    private static void openFolder(String which) {
        try {
            Path base = Path.of("").toAbsolutePath();
            File target = "mods".equals(which) ? base.resolve("mods").toFile() : base.toFile();
            if ("mods".equals(which) && !target.exists()) {
                Files.createDirectories(target.toPath());
            }
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(target);
            }
        } catch (IOException e) {
            logger.warn("Failed to open folder: {}", which, e);
        }
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
