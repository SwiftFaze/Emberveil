package com.swiftfaze.veil.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class InventoryPanel extends TerminalPanel {

    public InventoryPanel() {
        Border bottomLine = BorderFactory.createMatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY);
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);

        setBorder(BorderFactory.createCompoundBorder(bottomLine, padding));

        add(makeLabel("Inventory"));
        add(makeLabel("1. Item"));
        add(makeLabel("2. Item"));
        add(makeLabel("3. Item"));
        add(makeLabel("4. Item"));
        add(makeLabel("5. Item"));
    }
}
