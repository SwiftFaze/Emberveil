package com.swiftfaze.emberveil.game;

import com.swiftfaze.emberveil.entities.player.Player;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class PlayerInfoPanel extends JPanel {

    private final JLabel nameLabel;
    private final JLabel levelLabel;
    private final JLabel positionLabel;

    public PlayerInfoPanel() {
        setBackground(Color.BLACK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setFocusable(false);
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

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
