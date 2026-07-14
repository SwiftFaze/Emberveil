package com.swiftfaze.emberveil.game;

import com.swiftfaze.emberveil.Camera;
import com.swiftfaze.emberveil.DrawableAsciiEntity;
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

import static com.swiftfaze.emberveil.GameConst.*;

public class GamePanel extends JPanel {


    private final Player player = new Player(GAME_WIDTH / 2, GAME_HEIGHT / 2);
    private final TileTestScene scene = new TileTestScene(GAME_WIDTH * 2, GAME_HEIGHT * 2, GAME_DEPTH);
    private final Camera camera = new Camera(GAME_WIDTH, GAME_HEIGHT);
    private final List<Positionable> entitiesToDraw = new ArrayList<>();
    private List<GameListener> listeners = new ArrayList<>();

    private EastPanel eastPanel;
    private float preciseZLevel = 0;


    public GamePanel() {
        setPreferredSize(new Dimension(GAME_WIDTH * CHAR_WIDTH, GAME_HEIGHT * CHAR_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));

        int startX = GAME_WIDTH / 2;
        int startY = GAME_HEIGHT / 2;
        int startZ = scene.getSurfaceHeight(startX, startY);
        player.setPosition(startX, startY, startZ);
        preciseZLevel = startZ;

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
                    case KeyEvent.VK_PAGE_UP -> player.forceAscend();
                    case KeyEvent.VK_PAGE_DOWN -> player.forceDescend();
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
        roundZLevel();
        camera.centerOn(player.getX(), player.getY());

        float visualZLevel = preciseZLevel;

        int lowestVisibleZ = Math.max(0, (int) Math.floor(visualZLevel) - 3);
        int highestVisibleZ = scene.getDepth() - 1;

        for (int zLevel = lowestVisibleZ; zLevel <= highestVisibleZ; zLevel++) {

            float zDistanceFromPlayer = visualZLevel - zLevel;

            float brightness;

            if (zDistanceFromPlayer > 0) {
                brightness = getBrightnessFromDepth(zDistanceFromPlayer);
            } else {
                brightness = MAX_BRIGHTNESS;
            }

            scene.renderWorld(
                    g2d,
                    CHAR_WIDTH,
                    CHAR_HEIGHT,
                    camera.getX(),
                    camera.getY(),
                    zLevel,
                    brightness
            );

            float fogStartingHeight = zLevel - visualZLevel;
            scene.renderClouds(
                    g2d,
                    CHAR_WIDTH,
                    CHAR_HEIGHT,
                    camera.getX(),
                    camera.getY(),
                    zLevel,
                    fogStartingHeight
            );

        }
        for (Positionable entity : entitiesToDraw) {
            if (entity == scene) continue;

            if (entity instanceof DrawableAsciiEntity ascii) {
                ascii.render(
                        g2d,
                        CHAR_WIDTH,
                        CHAR_HEIGHT,
                        camera.getX(),
                        camera.getY()
                );

            }
        }
    }

    /**
     * Every time player moves, smoothly transition render Z level
     */
    private void roundZLevel() {
        float targetZ = player.getZ();
        float difference = targetZ - preciseZLevel;
        ///  STOP FLOATING POINT DRIFT
        if (Math.abs(difference) < 0.001f) {
            preciseZLevel = targetZ;
            return;
        }
        preciseZLevel += difference * Z_TRANSITION_SPEED;
    }


    private float getBrightnessFromDepth(float zDistanceFromPlayer) {
        if (zDistanceFromPlayer <= SHADOW_START_DEPTH) {
            return MAX_BRIGHTNESS;
        }
        float shadowDepth = zDistanceFromPlayer - SHADOW_START_DEPTH;
        float brightness = (float) Math.pow(
                BRIGHTNESS_LEVEL_DECAY_RATE,
                shadowDepth
        );
        return Math.max(MIN_BRIGHTNESS, brightness);
    }
}
