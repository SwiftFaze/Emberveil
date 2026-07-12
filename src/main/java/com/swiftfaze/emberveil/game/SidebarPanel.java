package com.swiftfaze.emberveil.game;

import javax.swing.*;
import java.awt.*;

import static com.swiftfaze.emberveil.game.GamePanel.CHAR_HEIGHT;
import static com.swiftfaze.emberveil.game.GamePanel.GAME_HEIGHT;

public class SidebarPanel extends JPanel implements GameListener {
    private JLabel healthLabel;
    private JLabel positionLabel;
    private JTextArea logArea;

    public SidebarPanel() {
        setPreferredSize(new Dimension(500, GAME_HEIGHT * CHAR_HEIGHT));
        setBackground(Color.BLACK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));

        healthLabel = makeLabel("HP: 100/100");
        positionLabel = makeLabel("Pos: (0, 0)");

        logArea = new JTextArea(10, 18);
        logArea.setEditable(false);
        logArea.setBackground(Color.BLACK);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        add(healthLabel);
        add(positionLabel);
        add(new JScrollPane(logArea));
    }

    @Override
    public void onPlayerMoved(int x, int y) {
        positionLabel.setText("Pos: (" + x + ", " + y + ")");
    }

    @Override
    public void onHealthChanged(int hp, int maxHp) {
        healthLabel.setText("HP: " + hp + "/" + maxHp);
    }


    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

}
