package com.swiftfaze.veil.input;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

public final class Keybindings {

    public static final KeyStroke MOVE_UP_Z = KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0);
    public static final KeyStroke MOVE_UP_ARROW = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
    public static final KeyStroke MOVE_DOWN_S = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0);
    public static final KeyStroke MOVE_DOWN_ARROW = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
    public static final KeyStroke MOVE_LEFT_Q = KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0);
    public static final KeyStroke MOVE_LEFT_ARROW = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
    public static final KeyStroke MOVE_RIGHT_D = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0);
    public static final KeyStroke MOVE_RIGHT_ARROW = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
    public static final KeyStroke TOGGLE_INVENTORY = KeyStroke.getKeyStroke(KeyEvent.VK_I, 0);

    public static final KeyStroke MENU_UP = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
    public static final KeyStroke MENU_DOWN = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
    public static final KeyStroke MENU_CONFIRM = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    public static final KeyStroke MENU_CANCEL = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

    public static final String ACTION_MOVE_UP = "move-up";
    public static final String ACTION_MOVE_DOWN = "move-down";
    public static final String ACTION_MOVE_LEFT = "move-left";
    public static final String ACTION_MOVE_RIGHT = "move-right";
    public static final String ACTION_TOGGLE_INVENTORY = "toggle-inventory";

    public static final String ACTION_MENU_UP = "menu-up";
    public static final String ACTION_MENU_DOWN = "menu-down";
    public static final String ACTION_MENU_CONFIRM = "menu-confirm";
    public static final String ACTION_MENU_CANCEL = "menu-cancel";

    private Keybindings() {
    }
}
