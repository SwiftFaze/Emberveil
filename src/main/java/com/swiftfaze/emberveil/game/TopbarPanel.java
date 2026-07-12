package com.swiftfaze.emberveil.game;

import javax.swing.*;
import java.awt.*;

import static com.swiftfaze.emberveil.game.GamePanel.*;

public class TopbarPanel extends JPanel {
    private JLabel title;

    public TopbarPanel() {
        setPreferredSize(new Dimension(GAME_WIDTH, 4 * CHAR_HEIGHT));
        setBackground(Color.BLACK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));

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
