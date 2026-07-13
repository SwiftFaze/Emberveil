package com.swiftfaze.emberveil;

import com.swiftfaze.emberveil.ui.EastPanel;
import com.swiftfaze.emberveil.game.GamePanel;
import com.swiftfaze.emberveil.ui.NorthPanel;
import com.swiftfaze.emberveil.ui.SouthPanel;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Emberveil");

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
    }
}
