package com.swiftfaze.emberveil.game;

import com.swiftfaze.emberveil.Camera;
import com.swiftfaze.emberveil.DrawableAsciiEntity;
import com.swiftfaze.emberveil.DrawableImageEntity;
import com.swiftfaze.emberveil.Positionable;
import com.swiftfaze.emberveil.entities.player.Player;
import com.swiftfaze.emberveil.ui.EastPanel;
import com.swiftfaze.emberveil.world.TileTestScene;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {

    public static final int GAME_WIDTH = 50;
    public static final int GAME_HEIGHT = 50;
    public static final int CHAR_WIDTH = 15;
    public static final int CHAR_HEIGHT = 15;

    private final Player player = new Player(GAME_WIDTH / 2, GAME_HEIGHT / 2);
    private final TileTestScene scene = new TileTestScene(100, 100, 10);
    private final Camera camera = new Camera(GAME_WIDTH, GAME_HEIGHT);
    private final List<Positionable> entitiesToDraw = new ArrayList<>();
    private List<GameListener> listeners = new ArrayList<>();

    private EastPanel eastPanel;

    public GamePanel() {
        setPreferredSize(new Dimension(GAME_WIDTH * CHAR_WIDTH, GAME_HEIGHT * CHAR_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));

        int startX = GAME_WIDTH / 2;
        int startY = GAME_HEIGHT / 2;
        int startZ = scene.getSurfaceHeight(startX, startY);
        player.setPosition(startX, startY, startZ);

        addEntity(scene);
        addEntity(player);

        keyListen();
    }

    public void setEastPanel(EastPanel eastPanel) {
        this.eastPanel = eastPanel;
    }

    private void keyListen() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_Z, KeyEvent.VK_UP -> player.moveUp(scene);
                    case KeyEvent.VK_S, KeyEvent.VK_DOWN -> player.moveDown(scene);
                    case KeyEvent.VK_Q, KeyEvent.VK_LEFT -> player.moveLeft(scene);
                    case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> player.moveRight(scene);
                    case KeyEvent.VK_PAGE_UP -> player.ascend(scene);
                    case KeyEvent.VK_PAGE_DOWN -> player.descend(scene);
                    case KeyEvent.VK_I -> {
                        if (eastPanel != null) {
                            eastPanel.toggleInventory();
                        }
                    }
                }
                for (GameListener l : listeners) {
                    l.updatePlayer(player);
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

    public void addGameListener(GameListener listener) {
        listeners.add(listener);
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        camera.centerOn(player.getX(), player.getY());

        int playerZ = player.getZ();

        for (int z = 0; z <= playerZ; z++) {
            int depthBelow = playerZ - z;
            float brightness = brightnessForDepth(depthBelow);
            scene.renderLayer(g2d, CHAR_WIDTH, CHAR_HEIGHT, camera.getX(), camera.getY(), z, brightness);
        }

        for (Positionable entity : entitiesToDraw) {
            if (entity == scene) continue;
            if (entity instanceof DrawableAsciiEntity ascii) {
                ascii.render(g2d, CHAR_WIDTH, CHAR_HEIGHT, camera.getX(), camera.getY());
            } else if (entity instanceof DrawableImageEntity image) {
                image.render(g2d, CHAR_WIDTH, CHAR_HEIGHT, camera.getX(), camera.getY());
            }
        }
    }

    private static final float DECAY_RATE = 0.55f; // lower = darker per floor, higher = subtler falloff
    private static final float MIN_BRIGHTNESS = 0.12f;

    private float brightnessForDepth(int depthBelow) {
        if (depthBelow <= 0) return 1.0f;
        float brightness = (float) Math.pow(DECAY_RATE, depthBelow);
        return Math.max(MIN_BRIGHTNESS, brightness);
    }
}
