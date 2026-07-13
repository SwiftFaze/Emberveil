package com.swiftfaze.emberveil.game;

import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {


    public MenuPanel() {
        setBackground(Color.BLACK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setFocusable(false);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        add(makeLabel("I - Inventory"));
        add(makeLabel("H - Help"));
        add(makeLabel("J - Journal"));
        add(makeLabel("M - Map"));
        add(makeLabel("P - Character"));
        add(makeLabel("O - Stats"));
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
