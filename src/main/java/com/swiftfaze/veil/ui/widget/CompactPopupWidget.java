package com.swiftfaze.veil.ui.widget;

import javax.swing.*;
import java.awt.*;

/**
 * A compact, fixed-size, centered popup variant suitable for smaller dialogs
 * (e.g. Yes/No confirmation). Unlike full-screen popups, this widget:
 * - Has a fixed preferred size (centered when laid out by FillLayout)
 * - Displays a decorative title bar with arrow accents
 * - Has no Close button (dismiss is via content-specific means, e.g. a radio choice)
 * - Reuses PopupWidget's core key-binding mechanism (Escape-to-dismiss, onUp/Down/Left/Right)
 */
public class CompactPopupWidget extends PopupWidget {
    private static final int POPUP_WIDTH = 400;
    private static final int POPUP_HEIGHT = 200;

    public CompactPopupWidget(String title) {
        super();

        // Remove the inherited Close button from the layout
        remove(getCloseButton());

        // Add a decorative title bar at the top
        JLabel titleLabel = new JLabel(">> " + title + " <<");
        titleLabel.setForeground(WidgetTheme.NORMAL_TEXT);
        titleLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WidgetTheme.TABLE_BORDER, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        add(titleLabel, BorderLayout.NORTH);
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(POPUP_WIDTH, POPUP_HEIGHT);
    }

    @Override
    public void open() {
        super.open();
        // Subclasses should override to focus their own content (e.g. a radio choice)
        // instead of the Close button (which is no longer visible)
    }
}
