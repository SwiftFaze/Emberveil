package com.swiftfaze.veil.ui.widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Pattern;

public class PatternFieldWidget extends Widget {
    private static final int UNFOCUSED_BORDER_WIDTH = 1;
    private static final int FOCUSED_BORDER_WIDTH = 2;

    private final Pattern pattern;
    private final StringBuilder input;
    private final JLabel label;
    private boolean hasFocus = false;

    public PatternFieldWidget(String pattern) {
        this.input = new StringBuilder();
        this.pattern = Pattern.compile(pattern);
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
        // Fixed height, stretches to fill whatever width its container offers — matches every
        // other widget's "full width" treatment (ListWidget's rows, TableWidget's row panels).
        int height = label.getPreferredSize().height + 12;
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
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || isPrintableSpecial(c)) {
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

    private void bindKeys() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "delete-char");
        actionMap.put("delete-char", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { deleteLastCharacter(); }
        });
    }

    private void updateAppearance() {
        label.setText(input.toString());
        boolean valid = patternIsValid();
        label.setForeground(valid ? WidgetTheme.NORMAL_TEXT : WidgetTheme.INVALID_HIGHLIGHT);

        Color borderColor = valid ? WidgetTheme.VALID_HIGHLIGHT : WidgetTheme.INVALID_HIGHLIGHT;
        int borderWidth = hasFocus ? FOCUSED_BORDER_WIDTH : UNFOCUSED_BORDER_WIDTH;
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, borderWidth),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
    }

    private class PatternFieldKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || isPrintableSpecial(c)) {
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
