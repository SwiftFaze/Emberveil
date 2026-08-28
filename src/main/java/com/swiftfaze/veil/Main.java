package com.swiftfaze.veil;

import com.swiftfaze.veil.game.GamePanel;
import com.swiftfaze.veil.ui.EastPanel;
import com.swiftfaze.veil.ui.NorthPanel;
import com.swiftfaze.veil.ui.SouthPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        loadGame();
    }

    private static void loadGame() {
        JFrame frame = new JFrame("Veil");

        NorthPanel northPanel = new NorthPanel();
        SouthPanel southPanel = new SouthPanel();
        EastPanel eastPanel = new EastPanel();
        GamePanel gamePanel = new GamePanel();

        gamePanel.addGameListener(eastPanel);
        gamePanel.setEastPanel(eastPanel); // wire "I" key to EastPanel's toggle

        frame.setLayout(new BorderLayout());
        frame.add(northPanel, BorderLayout.NORTH);
        frame.add(southPanel, BorderLayout.SOUTH);
        frame.add(gamePanel, BorderLayout.CENTER);
        frame.add(eastPanel, BorderLayout.EAST);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);


        gamePanel.requestFocusInWindow();
        gamePanel.startGameLoop();
        keyListen(frame);
    }

    private static void keyListen(JFrame frame) {
        JRootPane rootPane = frame.getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "resetGame");
        rootPane.getActionMap().put("resetGame", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                resetGame(frame);
            }
        });
    }

    private static void resetGame(JFrame oldFrame) {
        try {
            oldFrame.dispose(); // closes old window, releases its listeners
            loadGame();
            logger.info("Scene Reset");
        } catch (Exception e) {
            logger.error("Reset failed", e); // pass the Throwable, not e.getMessage()
        }
    }


}
