package com.swiftfaze.emberveil.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class InventoryPanel extends JPanel {


    public InventoryPanel() {
        setBackground(Color.BLACK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setFocusable(false);

        Border bottomLine = BorderFactory.createMatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY);
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);

        setBorder(BorderFactory.createCompoundBorder(bottomLine, padding));

        add(makeLabel("Inventory"));
        add(makeLabel("1. Item"));
        add(makeLabel("2. Item"));
        add(makeLabel("3. Item"));
        add(makeLabel("4. Item"));
        add(makeLabel("5. Item"));
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

}
