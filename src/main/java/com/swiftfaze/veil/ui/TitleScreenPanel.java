package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.ui.widget.ListWidget;
import com.swiftfaze.veil.ui.widget.WidgetTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TitleScreenPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(TitleScreenPanel.class);
    private static final Font TERMINAL_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 16);

    private final JLabel titleLabel;
    private final ListWidget<String> menuWidget;
    private final Consumer<String> onMenuSelect;

    public TitleScreenPanel(Consumer<String> onMenuSelect) {
        this.onMenuSelect = onMenuSelect;
        setBackground(Color.BLACK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setFocusable(false);

        // Title
        titleLabel = new JLabel("VEIL");
        titleLabel.setFont(loadTitleFont());
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(20));
        add(titleLabel);
        add(Box.createVerticalStrut(40));

        // Menu
        List<String> menuItems = List.of("Continue", "New", "Load", "Settings", "Exit");
        menuWidget = new ListWidget<>(s -> s);
        menuWidget.setItems(menuItems);
        menuWidget.setOnConfirm(this::handleMenuSelect);
        add(menuWidget);
    }

    private Font loadTitleFont() {
        try {
            InputStream fontStream = getClass().getResourceAsStream("/fonts/DeltaCorpsPriest1.ttf");
            if (fontStream != null) {
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                fontStream.close();
                return customFont.deriveFont(48f);
            }
        } catch (FontFormatException | IOException e) {
            logger.warn("Failed to load Delta Corps Priest 1 font, using fallback", e);
        }
        return new Font(Font.MONOSPACED, Font.PLAIN, 48);
    }

    private void handleMenuSelect(String item) {
        onMenuSelect.accept(item);
    }

    public String getHighlightedMenuItem() {
        return menuWidget.getSelectedItem();
    }

    public void moveUp() {
        menuWidget.moveUp();
    }

    public void moveDown() {
        menuWidget.moveDown();
    }

    public void confirm() {
        handleMenuSelect(menuWidget.getSelectedItem());
    }
}
