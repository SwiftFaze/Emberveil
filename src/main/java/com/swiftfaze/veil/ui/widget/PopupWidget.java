package com.swiftfaze.veil.ui.widget;

import com.swiftfaze.veil.input.Keybindings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PopupWidget extends Widget {

    private final JPanel contentPanel;
    private final ButtonWidget closeButton;
    private Runnable onDismiss = () -> {
    };
    private FocusManager focusManager;

    public PopupWidget() {
        setLayout(new BorderLayout());
        setVisible(false);

        contentPanel = new JPanel();
        contentPanel.setBackground(Color.BLACK);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        add(contentPanel, BorderLayout.CENTER);

        closeButton = new ButtonWidget("Close");
        closeButton.setOnConfirm(() -> dismiss());
        add(closeButton, BorderLayout.SOUTH);

        bindKeys();
    }

    public void setFocusManager(FocusManager focusManager) {
        this.focusManager = focusManager;
    }

    public void setOnDismiss(Runnable onDismiss) {
        this.onDismiss = onDismiss;
    }

    public void addContent(JComponent component) {
        contentPanel.add(component);
    }

    public void removeAllContent() {
        contentPanel.removeAll();
    }

    public ButtonWidget getCloseButton() {
        return closeButton;
    }

    public void open() {
        setVisible(true);
        if (focusManager != null) {
            focusManager.captureModally();
        }
        closeButton.requestFocusInWindow();
    }

    public void dismiss() {
        setVisible(false);
        if (focusManager != null) {
            focusManager.restoreFocus();
        }
        onDismiss.run();
    }

    private void bindKeys() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();

        inputMap.put(Keybindings.MENU_CANCEL, "popup-dismiss");

        actionMap.put("popup-dismiss", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dismiss();
            }
        });
    }
}
