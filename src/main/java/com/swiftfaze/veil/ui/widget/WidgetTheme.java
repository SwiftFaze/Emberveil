package com.swiftfaze.veil.ui.widget;

import javax.swing.JLabel;
import java.awt.Color;

public final class WidgetTheme {

    public static final Color SELECTED_HIGHLIGHT = Color.decode("#eeb392");
    public static final Color SELECTED_TEXT = Color.BLACK;
    public static final Color NORMAL_TEXT = Color.WHITE;
    public static final Color BACKGROUND = Color.BLACK;
    public static final Color INVALID_HIGHLIGHT = Color.decode("#e05a4e");
    public static final Color TABLE_HEADER_BACKGROUND = Color.decode("#1a1a1a");
    public static final Color TABLE_BORDER = Color.LIGHT_GRAY;
    public static final Color SCROLLBAR_THUMB = Color.GRAY;

    /**
     * The one place every widget's "selected" look is defined: a filled background, not just
     * recolored text — applied identically by ListWidget, TableWidget, and RadioGroupWidget, so
     * the highlight convention can't quietly diverge between widgets. Requires the label be
     * opaque (set once, at label-creation time) for the background fill to actually paint.
     */
    public static void applySelection(JLabel label, boolean selected) {
        label.setForeground(selected ? SELECTED_TEXT : NORMAL_TEXT);
        label.setBackground(selected ? SELECTED_HIGHLIGHT : BACKGROUND);
    }

    private WidgetTheme() {
    }
}
