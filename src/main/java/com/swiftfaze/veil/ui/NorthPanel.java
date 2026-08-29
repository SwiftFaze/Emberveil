package com.swiftfaze.veil.ui;

import javax.swing.*;
import java.awt.*;

import static com.swiftfaze.veil.GameConst.TILE_HEIGHT;
import static com.swiftfaze.veil.GameConst.GAME_WINDOW_WIDTH;

public class NorthPanel extends TerminalPanel {
    private final JLabel title;

    public NorthPanel() {
        setPreferredSize(new Dimension(GAME_WINDOW_WIDTH, 4 * TILE_HEIGHT));
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));

        title = makeLabel("Veil", Component.CENTER_ALIGNMENT);
        title.setForeground(Color.decode("#eeb392"));

        add(Box.createVerticalGlue());
        add(title);
        add(Box.createVerticalGlue());
    }
}
