package com.swiftfaze.veil.ui.widget;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Pattern;

/**
 * Material "outlined text field"-style widget: unfocused shows just a bottom
 * border with its label floating above; focused shows a full outline with
 * the label breaking the top edge (via {@link TitledBorder}, which already
 * implements exactly this "label interrupts the border line" look). Border
 * (and label) color tracks state: white while empty, red/green once there's
 * input, matching whether it fails or matches the pattern.
 */
public class PatternFieldWidget extends Widget {
    private static final int UNFOCUSED_BORDER_WIDTH = 1;
    // Swing line/matte borders only take integer pixel widths, so 1px is as thin as either state
    // can get — focus is conveyed by the outline shape (full box vs. bottom-only) and color, not
    // by extra thickness.
    private static final int FOCUSED_BORDER_WIDTH = 1;
    private static final Font LABEL_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    // Fixed and generous, not derived from any child's own metrics — border width and whether a
    // TitledBorder's reserved label space is present both vary with state, and a tightly-computed
    // height left no slack for that, squeezing the content label's area toward zero.
    private static final int FIELD_HEIGHT = 40;
    private static final int LABELED_FIELD_HEIGHT = 56;

    private final Pattern pattern;
    private final StringBuilder input;
    private final JLabel label;
    private final String fieldLabel;
    private boolean hasFocus = false;

    public PatternFieldWidget(String pattern) {
        this(pattern, null);
    }

    public PatternFieldWidget(String pattern, String fieldLabel) {
        this.input = new StringBuilder();
        this.pattern = Pattern.compile(pattern);
        this.fieldLabel = fieldLabel;
        this.label = new JLabel();
        this.label.setForeground(WidgetTheme.NORMAL_TEXT);
        this.label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        setLayout(new BorderLayout());
        setAlignmentX(LEFT_ALIGNMENT);
        add(label, BorderLayout.CENTER);
        bindKeys();
        addKeyListener(new PatternFieldKeyListener());
        addFocusListener(new PatternFieldFocusListener());
        updateAppearance();
        // Stretches to fill whatever width its container offers — matches every other widget's
        // "full width" treatment (ListWidget's rows, TableWidget's row panels).
        int height = fieldLabel != null ? LABELED_FIELD_HEIGHT : FIELD_HEIGHT;
        setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        setPreferredSize(new Dimension(200, height));
    }

    public String getInput() {
        return input.toString();
    }

    public boolean patternIsValid() {
        return pattern.matcher(input.toString()).matches();
    }

    public void typeCharacters(String chars) {
        for (char c : chars.toCharArray()) {
            if (isAppendable(c)) {
                input.append(c);
            }
        }
        updateAppearance();
    }

    public void deleteLastCharacter() {
        if (input.length() > 0) {
            input.deleteCharAt(input.length() - 1);
        }
        updateAppearance();
    }

    private boolean isPrintableSpecial(char c) {
        return c != '\n' && c != '\t' && c >= 32 && c <= 126;
    }

    // Enter (\n, \r) satisfies Character.isWhitespace() just like a space does, so without this
    // exclusion it got silently appended as a literal newline — a character no single-line
    // pattern ever matches, turning the field red the instant Enter was pressed.
    private boolean isAppendable(char c) {
        return c != '\n' && c != '\r'
                && (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || isPrintableSpecial(c));
    }

    private void bindKeys() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "delete-char");
        actionMap.put("delete-char", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { deleteLastCharacter(); }
        });
        // Enter moves to the next field, like Tab, rather than typing a character - standard
        // single-line-field behavior.
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "next-field");
        actionMap.put("next-field", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { transferFocus(); }
        });
    }

    private void updateAppearance() {
        label.setText(input.toString());
        Color stateColor = stateColor();
        label.setForeground(input.length() == 0 ? WidgetTheme.NORMAL_TEXT : stateColor);
        setBorder(buildBorder(stateColor));
    }

    private Color stateColor() {
        if (input.length() == 0) {
            return WidgetTheme.NORMAL_TEXT;
        }
        return patternIsValid() ? WidgetTheme.VALID_HIGHLIGHT : WidgetTheme.INVALID_HIGHLIGHT;
    }

    private Border buildBorder(Color color) {
        // Unfocused: bottom line only. Focused: full outline. TitledBorder reserves the same
        // label space either way, but only visibly "breaks" a line that's actually drawn there —
        // so the label just floats above the field when unfocused (no top line to interrupt),
        // and sits on the top edge, breaking it, once focused. Exactly the Material "outlined
        // field" look, with no custom border-painting/gap-cutting logic needed.
        Border outline = hasFocus
                ? BorderFactory.createLineBorder(color, FOCUSED_BORDER_WIDTH)
                : BorderFactory.createMatteBorder(0, 0, UNFOCUSED_BORDER_WIDTH, 0, color);
        Border padded = BorderFactory.createCompoundBorder(outline, BorderFactory.createEmptyBorder(4, 6, 4, 6));
        if (fieldLabel == null) {
            return padded;
        }
        return BorderFactory.createTitledBorder(
                padded, fieldLabel, TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, LABEL_FONT, color);
    }

    private class PatternFieldKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if (isAppendable(c)) {
                input.append(c);
                updateAppearance();
                e.consume();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {}

        @Override
        public void keyReleased(KeyEvent e) {}
    }

    private class PatternFieldFocusListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            hasFocus = true;
            updateAppearance();
        }

        @Override
        public void focusLost(FocusEvent e) {
            hasFocus = false;
            updateAppearance();
        }
    }
}
