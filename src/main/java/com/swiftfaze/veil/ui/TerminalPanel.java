package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.ui.widget.WidgetTheme;

import javax.swing.*;
import java.awt.*;

public abstract class TerminalPanel extends JPanel {

    private static final Font TERMINAL_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 16);

    protected TerminalPanel() {
        setBackground(WidgetTheme.BACKGROUND);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setFocusable(false);
    }

    protected JLabel makeLabel(String text) {
        return makeLabel(text, Component.LEFT_ALIGNMENT);
    }

    protected JLabel makeLabel(String text, float horizontalAlignment) {
        JLabel label = new JLabel(text);
        label.setForeground(WidgetTheme.NORMAL_TEXT);
        label.setFont(TERMINAL_FONT);
        label.setAlignmentX(horizontalAlignment);
        return label;
    }
}
