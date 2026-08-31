package com.swiftfaze.veil.ui.widget;

import javax.swing.*;
import java.awt.*;

/**
 * A compact, fixed-size, centered popup variant suitable for smaller dialogs
 * (e.g. Yes/No confirmation). Unlike full-screen popups, this widget:
 * - Has a fixed preferred size (centered when laid out by FillLayout)
 * - Displays a title bar, divided from the body by a line
 * - Has no Close button (dismiss is via content-specific means, e.g. a radio choice)
 * - Reuses PopupWidget's core key-binding mechanism (Escape-to-dismiss, onUp/Down/Left/Right)
 */
public class CompactPopupWidget extends PopupWidget {
    private static final int POPUP_WIDTH = 400;
    private static final int POPUP_HEIGHT = 200;

    // Narrower than POPUP_WIDTH to clear the outer frame plus a body label's own padding
    // (see e.g. ResetConfirmationPopup/DropConfirmationPopup's question labels).
    private static final int BODY_TEXT_WIDTH = 340;

    private final JLabel titleLabel;

    public CompactPopupWidget(String title) {
        super();

        // Remove the inherited Close button from the layout
        remove(getCloseButton());

        // Outer frame around the whole dialog, not just the title — without this the body
        // below the title bar has no border at all and reads as black-on-black against the
        // game view behind it.
        setBorder(BorderFactory.createLineBorder(WidgetTheme.TABLE_BORDER, 2));

        // Add a title bar at the top, separated from the body by a divider line rather than
        // its own full box (the outer frame above already closes that box).
        titleLabel = new JLabel(title);
        titleLabel.setForeground(WidgetTheme.NORMAL_TEXT);
        titleLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, WidgetTheme.TABLE_BORDER),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        add(titleLabel, BorderLayout.NORTH);
    }

    /**
     * Lets a subclass update the title after construction (e.g. to name the specific item a
     * confirmation is about) instead of being stuck with whatever title the constructor was
     * first given.
     */
    protected void setTitleText(String title) {
        titleLabel.setText(title);
    }

    /**
     * Wraps plain text for a body JLabel so it word-wraps and centers within the dialog instead
     * of running past the border and getting truncated with "..." — a plain JLabel never wraps
     * on its own, only HTML content does, and only when explicitly given a pixel width to wrap
     * at (an unconstrained div would just keep growing sideways). The label's own font/color set
     * via setFont()/setForeground() still apply as the HTML's default, so callers don't need to
     * repeat that styling in CSS.
     */
    protected static String wrapBodyText(String text) {
        return "<html><div style='width:" + BODY_TEXT_WIDTH + "px;text-align:center;'>" + text + "</div></html>";
    }

    /**
     * Sets a body label's text to the wrapped form above and caps its maximumSize to match —
     * without the cap, the label has no bounded maximumSize of its own (a plain JComponent's
     * defaults to unbounded), so the popup's vertical BoxLayout content pane stretches it to the
     * pane's full width and the label's own centered alignment has no narrower block left to
     * center; capping it lets the parent's alignmentX actually center the (fixed-width) wrapped
     * text instead. Must be called again whenever the text changes, since a different wrap
     * (different line count) changes the label's preferred height.
     */
    protected static void setBodyText(JLabel label, String text) {
        label.setText(wrapBodyText(text));
        label.setMaximumSize(label.getPreferredSize());
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
