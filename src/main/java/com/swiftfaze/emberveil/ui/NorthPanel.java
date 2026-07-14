package com.swiftfaze.emberveil.ui;

import javax.swing.*;
import java.awt.*;

import static com.swiftfaze.emberveil.GameConst.TILE_HEIGHT;
import static com.swiftfaze.emberveil.GameConst.GAME_WINDOW_WIDTH;

public class NorthPanel extends JPanel {
    private JLabel title;

    public NorthPanel() {
        setPreferredSize(new Dimension(GAME_WINDOW_WIDTH, 4 * TILE_HEIGHT));
        setBackground(Color.BLACK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        setFocusable(false);

        title = makeLabel("Emberveil");
        title.setForeground(Color.decode("#eeb392"));



        add(Box.createVerticalGlue());
        add(title);
        add(Box.createVerticalGlue());
    }


    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

}
