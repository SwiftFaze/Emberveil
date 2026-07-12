package com.swiftfaze.emberveil;

import com.swiftfaze.emberveil.game.BottomPanel;
import com.swiftfaze.emberveil.game.GamePanel;
import com.swiftfaze.emberveil.game.SidebarPanel;
import com.swiftfaze.emberveil.game.TopbarPanel;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Emberveil");

        TopbarPanel topbarPanel = new TopbarPanel();
        BottomPanel bottomPanel = new BottomPanel();
        SidebarPanel sidebar = new SidebarPanel();
        GamePanel gamePanel = new GamePanel();
        gamePanel.addGameListener(sidebar);

        frame.setLayout(new BorderLayout());
        frame.add(topbarPanel, BorderLayout.NORTH);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.add(gamePanel, BorderLayout.CENTER);
        frame.add(sidebar, BorderLayout.EAST);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        gamePanel.startGameLoop();
    }
}
