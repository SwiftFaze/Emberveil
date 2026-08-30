package com.swiftfaze.veil.ui.widget;

import com.swiftfaze.veil.input.Keybindings;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class RadioGroupWidget<T> extends Widget {
    // Same insets as CONFIRMED_BORDER's line width, drawn in the background color instead of
    // green — keeps every label's size identical whether or not it's currently the confirmed
    // option, so confirming doesn't shift the layout of the options around it.
    private static final Border CONFIRMED_BORDER = BorderFactory.createLineBorder(WidgetTheme.VALID_HIGHLIGHT, 2);
    private static final Border UNCONFIRMED_BORDER = BorderFactory.createEmptyBorder(2, 2, 2, 2);

    private final Function<T, String> optionRenderer;
    private final boolean horizontal;
    private final List<T> options = new ArrayList<>();
    private final List<JLabel> labels = new ArrayList<>();
    private int highlightedIndex = 0;
    private int selectedIndex = -1;
    private boolean wrapAround = true;
    private Consumer<T> onConfirm = t -> {};

    public RadioGroupWidget(Function<T, String> optionRenderer, boolean horizontal) {
        this.optionRenderer = optionRenderer;
        this.horizontal = horizontal;
        setLayout(new BoxLayout(this, horizontal ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));
        bindKeys();
    }

    public void setOptions(List<T> options) {
        this.options.clear();
        this.options.addAll(options);
        this.highlightedIndex = 0;
        this.selectedIndex = -1;
        refresh();
    }

    public void setWrapAround(boolean wrapAround) {
        this.wrapAround = wrapAround;
    }

    public void setOnConfirm(Consumer<T> onConfirm) {
        this.onConfirm = onConfirm;
    }

    public T getHighlightedOption() {
        return options.isEmpty() ? null : options.get(highlightedIndex);
    }

    public T getSelectedOption() {
        return selectedIndex < 0 || selectedIndex >= options.size() ? null : options.get(selectedIndex);
    }

    public void selectOption(int index) {
        if (index >= 0 && index < options.size()) {
            selectedIndex = index;
            refreshHighlight();
        }
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public void moveUp() {
        moveVertical(false);
    }

    public void moveDown() {
        moveVertical(true);
    }

    public void moveLeft() {
        moveHorizontal(false);
    }

    public void moveRight() {
        moveHorizontal(true);
    }

    public void moveVertical(boolean down) {
        if (options.isEmpty()) return;
        highlightedIndex = down
            ? (wrapAround
                ? (highlightedIndex + 1) % options.size()
                : Math.min(options.size() - 1, highlightedIndex + 1))
            : (wrapAround
                ? (highlightedIndex - 1 + options.size()) % options.size()
                : Math.max(0, highlightedIndex - 1));
        refreshHighlight();
    }

    public void moveHorizontal(boolean right) {
        if (options.isEmpty()) return;
        highlightedIndex = right
            ? (wrapAround
                ? (highlightedIndex + 1) % options.size()
                : Math.min(options.size() - 1, highlightedIndex + 1))
            : (wrapAround
                ? (highlightedIndex - 1 + options.size()) % options.size()
                : Math.max(0, highlightedIndex - 1));
        refreshHighlight();
    }

    private void bindKeys() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();

        if (horizontal) {
            inputMap.put(Keybindings.MENU_LEFT, "radio-left");
            inputMap.put(Keybindings.MENU_RIGHT, "radio-right");
            actionMap.put("radio-left", new AbstractAction() {
                public void actionPerformed(ActionEvent e) { moveHorizontal(false); } });
            actionMap.put("radio-right", new AbstractAction() {
                public void actionPerformed(ActionEvent e) { moveHorizontal(true); } });
        } else {
            inputMap.put(Keybindings.MENU_UP, "radio-up");
            inputMap.put(Keybindings.MENU_DOWN, "radio-down");
            actionMap.put("radio-up", new AbstractAction() {
                public void actionPerformed(ActionEvent e) { moveVertical(false); } });
            actionMap.put("radio-down", new AbstractAction() {
                public void actionPerformed(ActionEvent e) { moveVertical(true); } });
        }

        inputMap.put(Keybindings.MENU_CONFIRM, "radio-confirm");
        actionMap.put("radio-confirm", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectedIndex = highlightedIndex;
                T option = getSelectedOption();
                if (option != null) onConfirm.accept(option);
            }
        });
    }

    private void refresh() {
        removeAll();
        labels.clear();
        for (T option : options) {
            JLabel label = new JLabel(optionRenderer.apply(option));
            label.setOpaque(true);
            label.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 16));
            label.setAlignmentX(LEFT_ALIGNMENT);
            labels.add(label);
            add(label);
        }
        refreshHighlight();
        revalidate();
        repaint();
    }

    private void refreshHighlight() {
        for (int i = 0; i < labels.size(); i++) {
            JLabel label = labels.get(i);
            WidgetTheme.applySelection(label, i == highlightedIndex);
            // The confirmed option (Enter pressed) gets a green border distinct from the
            // highlighted/cursor background above — they can be different options at once (you've
            // confirmed one, then arrowed elsewhere without confirming again).
            Border outline = i == selectedIndex ? CONFIRMED_BORDER : UNCONFIRMED_BORDER;
            label.setBorder(BorderFactory.createCompoundBorder(outline, BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        }
        if (highlightedIndex < labels.size()) {
            scrollRectToVisible(labels.get(highlightedIndex).getBounds());
        }
    }
}
