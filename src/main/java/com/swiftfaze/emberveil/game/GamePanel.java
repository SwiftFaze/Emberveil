package com.swiftfaze.emberveil.game;

import com.swiftfaze.emberveil.Camera;
import com.swiftfaze.emberveil.DrawableAsciiEntity;
import com.swiftfaze.emberveil.DrawableImageEntity;
import com.swiftfaze.emberveil.Positionable;
import com.swiftfaze.emberveil.entities.player.Player;
import com.swiftfaze.emberveil.entities.scenery.BigGrass;
import com.swiftfaze.emberveil.entities.scenery.GrassPatch;
import com.swiftfaze.emberveil.entities.scenery.Lake;
import com.swiftfaze.emberveil.world.Background;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    static final int COLS = 60;
    static final int ROWS = 30;
    static final int CHAR_WIDTH = 20;
    static final int CHAR_HEIGHT = 20;

    private final Player player = new Player(COLS / 2, ROWS / 2);
    private final Background background = new Background(COLS, ROWS);
    private final Camera camera = new Camera(COLS, ROWS);
    private final List<Positionable> entitiesToDraw = new ArrayList<>();

    public GamePanel() {
        setPreferredSize(new Dimension(COLS * CHAR_WIDTH, ROWS * CHAR_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        addEntity(background);
//        addEntity(new Grass(31, 16));
        addEntity(player);

        keyListen();
    }

    private void keyListen() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_Z, KeyEvent.VK_UP -> player.moveUp();
                    case KeyEvent.VK_S, KeyEvent.VK_DOWN -> player.moveDown();
                    case KeyEvent.VK_Q, KeyEvent.VK_LEFT -> player.moveLeft();
                    case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> player.moveRight();
                }
                repaint();
            }
        });
    }

    public void addEntity(Positionable entity) {
        entitiesToDraw.add(entity);
    }

    public void startGameLoop() {
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        camera.centerOn(player.getX(), player.getY());

        for (Positionable entity : entitiesToDraw) {
            if (entity instanceof DrawableAsciiEntity ascii) {
                ascii.render(g2d, CHAR_WIDTH, CHAR_HEIGHT, camera.getX(), camera.getY());
            } else if (entity instanceof DrawableImageEntity image) {
                image.render(g2d, CHAR_WIDTH, CHAR_HEIGHT, camera.getX(), camera.getY());
            }
        }
    }
}
