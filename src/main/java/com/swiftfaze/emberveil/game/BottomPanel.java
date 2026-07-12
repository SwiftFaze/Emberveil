package com.swiftfaze.emberveil.game;

import javax.swing.*;
import java.awt.*;

import static com.swiftfaze.emberveil.game.GamePanel.GAME_HEIGHT;
import static com.swiftfaze.emberveil.game.GamePanel.GAME_WIDTH;

public class BottomPanel extends JPanel {

    public BottomPanel() {
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT * 4));
        setBackground(Color.BLACK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));

        add(Box.createVerticalGlue());
    }


}
