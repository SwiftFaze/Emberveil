package com.swiftfaze.veil;

import com.swiftfaze.veil.game.GamePanel;
import com.swiftfaze.veil.mods.ModLoader;
import com.swiftfaze.veil.mods.ModRegistry;
import com.swiftfaze.veil.mods.WidgetColorTheme;
import com.swiftfaze.veil.ui.CodexPanel;
import com.swiftfaze.veil.ui.GameWindow;
import com.swiftfaze.veil.ui.InventoryPanel;
import com.swiftfaze.veil.ui.PopupToggleListener;
import com.swiftfaze.veil.ui.SettingsKeybindsPanel;
import com.swiftfaze.veil.ui.SettingsScreenPanel;
import com.swiftfaze.veil.ui.SettingsWindow;
import com.swiftfaze.veil.ui.TitleScreenPanel;
import com.swiftfaze.veil.ui.widget.FocusManager;
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
        Map<String, JComponent> cards = new HashMap<>();

        GamePanel gamePanel = buildGameCard(cardPanel, cards);
        buildUIScreens(cardLayout, cardPanel, cards, gamePanel);
        configureAndShowFrame(frame, cardPanel, cardLayout);
    }

    private static GamePanel buildGameCard(JPanel cardPanel, Map<String, JComponent> cards) {
        GamePanel gamePanel = new GamePanel();
        InventoryPanel inventoryPanel = new InventoryPanel();
        CodexPanel codexPanel = new CodexPanel();
        wirePopups(gamePanel, inventoryPanel, codexPanel);

        JLayeredPane gameContentArea = GameWindow.buildContentArea(gamePanel);
        gameContentArea.add(inventoryPanel, JLayeredPane.POPUP_LAYER);
        gameContentArea.add(codexPanel, JLayeredPane.POPUP_LAYER);
        gameContentArea.add(inventoryPanel.getDropConfirmationPopup(), JLayeredPane.DRAG_LAYER);

        JPanel gameCard = new JPanel(new BorderLayout());
        gameCard.add(gameContentArea, BorderLayout.CENTER);
        cardPanel.add(gameCard, "game");
        return gamePanel;
    }

    // Minimal stopgap wiring so the I/X toggles keep working with EastPanel gone (see
    // specs/intent/shared-list-detail-ui-contract.md's Clarifications) — no sidebar, no
    // player-info display, just enough plumbing for the two popups to open/close/exclude
    // each other and hand focus back to the game on dismiss, same as EastPanel used to.
    private static void wirePopups(GamePanel gamePanel, InventoryPanel inventoryPanel, CodexPanel codexPanel) {
        ModRegistry mods = ModLoader.load(Paths.get("mods"));
        FocusManager focusManager = new FocusManager();

        inventoryPanel.setFocusManager(focusManager);
        inventoryPanel.showItems(mods.getAllItems());
        inventoryPanel.setOnDismiss(gamePanel::requestFocusInWindow);

        codexPanel.setFocusManager(focusManager);
        codexPanel.showItems(mods.getAllItems());
        codexPanel.showTiles(mods.getAllTiles());
        codexPanel.showClasses(mods.getAllPlayerClasses());
        codexPanel.setOnDismiss(gamePanel::requestFocusInWindow);

        gamePanel.addGameListener(new PopupToggleListener(inventoryPanel, codexPanel));
    }

    private static void buildUIScreens(CardLayout cardLayout, JPanel cardPanel,
                                       Map<String, JComponent> cards, GamePanel gamePanel) {
        TitleScreenPanel titleScreen = new TitleScreenPanel(menuItem -> handleMenuSelection(menuItem, cardLayout, cardPanel, cards, gamePanel));
        SettingsScreenPanel settingsScreen = new SettingsScreenPanel(screen -> navigateTo(cardLayout, cardPanel, cards, screen), Main::openFolder);
        SettingsKeybindsPanel keybindsScreen = new SettingsKeybindsPanel(screen -> navigateTo(cardLayout, cardPanel, cards, screen));
        cards.put("title", titleScreen);
        cards.put("settings", settingsScreen);
        cards.put("keybinds", keybindsScreen);
        cardPanel.add(titleScreen, "title");
        cardPanel.add(SettingsWindow.buildContentArea(settingsScreen), "settings");
        cardPanel.add(keybindsScreen, "keybinds");
    }

    private static void handleMenuSelection(String menuItem, CardLayout cardLayout, JPanel cardPanel,
                                           Map<String, JComponent> cards, GamePanel gamePanel) {
        if ("New".equals(menuItem)) {
            cardLayout.show(cardPanel, "game");
            gamePanel.requestFocusInWindow();
            gamePanel.startGameLoop();
        } else if ("Settings".equals(menuItem)) {
            navigateTo(cardLayout, cardPanel, cards, "settings");
        }
    }

    private static void configureAndShowFrame(JFrame frame, JPanel cardPanel, CardLayout cardLayout) {
        frame.setLayout(new BorderLayout());
        frame.add(cardPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        cardLayout.show(cardPanel, "title");
        ((JComponent) cardPanel.getComponent(0)).requestFocusInWindow();
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
