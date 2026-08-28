package com.swiftfaze.veil.game;

import com.swiftfaze.veil.Camera;
import com.swiftfaze.veil.DrawableAsciiEntity;
import com.swiftfaze.veil.Positionable;
import com.swiftfaze.veil.entities.buildings.BuildingLoader;
import com.swiftfaze.veil.entities.player.Player;
import com.swiftfaze.veil.ui.EastPanel;
import com.swiftfaze.veil.world.TileTestScene;
import com.swiftfaze.veil.world.TileTestScene2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static com.swiftfaze.veil.GameConst.*;

public class GamePanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(GamePanel.class);

    private final Player player = new Player(DEFAULT_PLAYER_START_X, DEFAULT_PLAYER_START_Y);
//    private final TileTestScene scene = new TileTestScene(DEFAULT_MAP_WIDTH, DEFAULT_MAP_HEIGHT, DEFAULT_MAP_DEPTH);
    private TileTestScene2 scene = new TileTestScene2(DEFAULT_MAP_WIDTH, DEFAULT_MAP_HEIGHT, DEFAULT_MAP_DEPTH);
    private final Camera camera = new Camera(GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT);
    private final List<Positionable> entitiesToDraw = new ArrayList<>();
    private List<GameListener> listeners = new ArrayList<>();

    private EastPanel eastPanel;
    private float preciseZLevel = 0;


    public GamePanel() {
        setPreferredSize(new Dimension(GAME_WINDOW_WIDTH * TILE_WIDTH, GAME_WINDOW_HEIGHT * TILE_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));

        player.setPosition(DEFAULT_PLAYER_START_X, DEFAULT_PLAYER_START_Y, DEFAULT_PLAYER_START_Z);
        preciseZLevel = DEFAULT_PLAYER_START_Z;

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
                    TILE_WIDTH,
                    TILE_HEIGHT,
                    camera.getX(),
                    camera.getY(),
                    zLevel,
                    brightness
            );

            float fogStartingHeight = zLevel - visualZLevel;
            scene.renderClouds(
                    g2d,
                    TILE_WIDTH,
                    TILE_HEIGHT,
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
                        TILE_WIDTH,
                        TILE_HEIGHT,
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
