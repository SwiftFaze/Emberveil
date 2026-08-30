package com.swiftfaze.veil.ui.widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Pattern;

public class PatternFieldWidget extends Widget {
    private final Pattern pattern;
    private final StringBuilder input;
    private final JLabel label;

    public PatternFieldWidget(String pattern) {
        this.input = new StringBuilder();
        this.pattern = Pattern.compile(pattern);
        this.label = new JLabel();
        this.label.setForeground(WidgetTheme.NORMAL_TEXT);
        this.label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
        bindKeys();
        addKeyListener(new PatternFieldKeyListener());
        updateLabel();
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
        updateLabel();
    }

    public void deleteLastCharacter() {
        if (input.length() > 0) {
            input.deleteCharAt(input.length() - 1);
        }
        updateLabel();
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

    private void updateLabel() {
        label.setText(input.toString());
        label.setForeground(patternIsValid() ? WidgetTheme.NORMAL_TEXT : WidgetTheme.INVALID_HIGHLIGHT);
    }

    private class PatternFieldKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || isPrintableSpecial(c)) {
                input.append(c);
                updateLabel();
                e.consume();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {}

        @Override
        public void keyReleased(KeyEvent e) {}
    }
}
