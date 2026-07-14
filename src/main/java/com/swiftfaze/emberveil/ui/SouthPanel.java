package com.swiftfaze.emberveil.ui;

import javax.swing.*;
import java.awt.*;

import static com.swiftfaze.emberveil.GameConst.GAME_WINDOW_HEIGHT;
import static com.swiftfaze.emberveil.GameConst.GAME_WINDOW_WIDTH;


public class SouthPanel extends JPanel {

    public SouthPanel() {
        setPreferredSize(new Dimension(GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT * 4));
        setBackground(Color.BLACK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        setFocusable(false);

        add(Box.createVerticalGlue());
    }


}
