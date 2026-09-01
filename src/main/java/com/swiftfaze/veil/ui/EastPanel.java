package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.entities.player.Player;
import com.swiftfaze.veil.game.GameListener;
import com.swiftfaze.veil.mods.ModLoader;
import com.swiftfaze.veil.mods.ModRegistry;
import com.swiftfaze.veil.ui.widget.FocusManager;
import com.swiftfaze.veil.ui.widget.WidgetTheme;

import javax.swing.*;
import java.awt.*;

import static com.swiftfaze.veil.GameConst.TILE_HEIGHT;
import static com.swiftfaze.veil.GameConst.GAME_WINDOW_HEIGHT;


public class EastPanel extends JPanel implements GameListener {

    private final PlayerInfoPanel playerInfoPanel;
    private final InventoryPanel inventoryPanel;
    private final CodexPanel codexPanel;
    private final FocusManager focusManager;
    private Runnable restoreGameFocus = () -> {
    };

    public EastPanel() {
        setPreferredSize(new Dimension(500, GAME_WINDOW_HEIGHT * TILE_HEIGHT));
        setBackground(WidgetTheme.BACKGROUND);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(WidgetTheme.BORDER, 2));
        setFocusable(false);

        focusManager = new FocusManager();

        playerInfoPanel = new PlayerInfoPanel();
        inventoryPanel = new InventoryPanel();
        codexPanel = new CodexPanel();

        ModRegistry mods = ModLoader.load(java.nio.file.Paths.get("mods"));

        inventoryPanel.setFocusManager(focusManager);
        inventoryPanel.showItems(mods.getAllItems());
        inventoryPanel.setOnDismiss(this::onInventoryDismissed);

        codexPanel.setFocusManager(focusManager);
        codexPanel.showItems(mods.getAllItems());
        codexPanel.showTiles(mods.getAllTiles());
        codexPanel.showClasses(mods.getAllPlayerClasses());
        codexPanel.setOnDismiss(this::onCodexDismissed);

        add(playerInfoPanel, BorderLayout.NORTH);
    }

    public void setRestoreGameFocusAction(Runnable restoreGameFocus) {
        this.restoreGameFocus = restoreGameFocus;
    }

    @Override
    public void toggleInventory() {
        if (inventoryPanel.isVisible()) {
            inventoryPanel.dismiss();
        } else {
            if (codexPanel.isVisible()) {
                codexPanel.dismiss();
            }
            inventoryPanel.open();
        }
    }

    @Override
    public void toggleCodex() {
        if (codexPanel.isVisible()) {
            codexPanel.dismiss();
        } else {
            if (inventoryPanel.isVisible()) {
                inventoryPanel.dismiss();
            }
            codexPanel.open();
        }
    }

    private void onInventoryDismissed() {
        restoreGameFocus.run();
    }

    private void onCodexDismissed() {
        restoreGameFocus.run();
    }

    public InventoryPanel getInventoryPanel() {
        return inventoryPanel;
    }

    public CodexPanel getCodexPanel() {
        return codexPanel;
    }

    @Override
    public void updatePlayer(Player player) {
        playerInfoPanel.updatePlayer(player);
    }
}
