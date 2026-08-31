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

    /**
     * Returns true if this popup should stretch to fill the parent container,
     * false if it should be sized and centered at its preferred size.
     * Subclasses can override to present smaller, compact popups.
     */
    public boolean isFullScreen() {
        return true;
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

    /**
     * Hook for a subclass with scrollable/navigable content (e.g. an item
     * list) to move its own selection. No-op by default, since not every
     * popup has anything to navigate.
     */
    protected void onUp() {
    }

    protected void onDown() {
    }

    protected void onLeft() {
    }

    protected void onRight() {
    }

    private void bindKeys() {
        InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = getActionMap();

        inputMap.put(Keybindings.MENU_CANCEL, "popup-dismiss");
        inputMap.put(Keybindings.MENU_UP, "popup-up");
        inputMap.put(Keybindings.MENU_DOWN, "popup-down");
        inputMap.put(Keybindings.MENU_LEFT, "popup-left");
        inputMap.put(Keybindings.MENU_RIGHT, "popup-right");

        actionMap.put("popup-dismiss", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dismiss();
            }
        });

        actionMap.put("popup-up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onUp();
            }
        });

        actionMap.put("popup-down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDown();
            }
        });

        actionMap.put("popup-left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onLeft();
            }
        });

        actionMap.put("popup-right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRight();
            }
        });
    }
}
