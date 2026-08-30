package com.swiftfaze.veil.ui.widget;

import javax.swing.JLabel;
import java.awt.BorderLayout;

public class SliderWidget extends Widget {
    private final int min;
    private final int max;
    private final int step;
    private int value;
    private final JLabel display;

    public SliderWidget(int min, int max, int step, int initialValue) {
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = Math.max(min, Math.min(max, initialValue));

        setLayout(new BorderLayout());
        display = new JLabel(String.valueOf(value));
        display.setForeground(WidgetTheme.NORMAL_TEXT);
        add(display, BorderLayout.CENTER);
    }

    public int getValue() {
        return value;
    }

    public void moveRight() {
        int newValue = value + step;
        if (newValue <= max) {
            value = newValue;
            updateDisplay();
        }
    }

    public void moveLeft() {
        int newValue = value - step;
        if (newValue >= min) {
            value = newValue;
            updateDisplay();
        }
    }

    private void updateDisplay() {
        display.setText(String.valueOf(value));
    }
}
