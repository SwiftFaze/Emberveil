package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.ui.widget.WidgetTheme;

import javax.swing.*;
import java.awt.*;

import static com.swiftfaze.veil.GameConst.GAME_WINDOW_HEIGHT;
import static com.swiftfaze.veil.GameConst.GAME_WINDOW_WIDTH;

public class SouthPanel extends TerminalPanel {

    public SouthPanel() {
        setPreferredSize(new Dimension(GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT * 4));
        setBorder(BorderFactory.createLineBorder(WidgetTheme.BORDER, 2));

        add(Box.createVerticalGlue());
    }
}
