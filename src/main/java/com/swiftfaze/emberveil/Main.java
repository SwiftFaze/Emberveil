package com.swiftfaze.emberveil;

import com.swiftfaze.emberveil.game.GamePanel;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Emberveil");
        GamePanel gamePanel = new GamePanel();

        frame.add(gamePanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null); // center on screen
        frame.setVisible(true);

        gamePanel.startGameLoop();
    }
}
