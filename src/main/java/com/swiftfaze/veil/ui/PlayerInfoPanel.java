package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.entities.player.Player;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class PlayerInfoPanel extends TerminalPanel {

    private final JLabel nameLabel;
    private final JLabel levelLabel;
    private final JLabel positionLabel;

    public PlayerInfoPanel() {
        Border bottomLine = BorderFactory.createMatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY);
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);

        setBorder(BorderFactory.createCompoundBorder(bottomLine, padding));

        nameLabel = makeLabel("Name:");
        levelLabel = makeLabel("LV:");
        positionLabel = makeLabel("Pos: ");

        add(nameLabel);
        add(levelLabel);
        add(positionLabel);
    }

    public void updatePlayer(Player player) {
        nameLabel.setText(player.getPlayerInfo().getFirstName() + " " + player.getPlayerInfo().getLastName()
                + " | " + player.getPlayerInfo().getPlayerClass().getName());
        levelLabel.setText("LV " + player.getPlayerInfo().getLevel().getCurrentLevel()
                + " | " + player.getPlayerInfo().getLevel().getXp() + "/" + player.getPlayerInfo().getLevel().getMaxXp() + " XP");
        positionLabel.setText("Pos: (" + player.getX() + ", " + player.getY() + ")");
    }
}
