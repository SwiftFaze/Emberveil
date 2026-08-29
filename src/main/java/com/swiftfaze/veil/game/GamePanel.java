package com.swiftfaze.veil.game;

import com.swiftfaze.veil.Camera;
import com.swiftfaze.veil.DrawableAsciiEntity;
import com.swiftfaze.veil.Positionable;
import com.swiftfaze.veil.entities.player.Player;
import com.swiftfaze.veil.input.Keybindings;
import com.swiftfaze.veil.world.TileTestScene2;
import com.swiftfaze.veil.world.WorldScene;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.swiftfaze.veil.GameConst.*;

public class GamePanel extends JPanel {

    private final Player player = new Player(DEFAULT_PLAYER_START_X, DEFAULT_PLAYER_START_Y);
    private TileTestScene2 scene = new TileTestScene2(DEFAULT_MAP_WIDTH, DEFAULT_MAP_HEIGHT);
    private final Camera camera = new Camera(GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT);
    private final List<Positionable> entitiesToDraw = new ArrayList<>();
    private final List<GameListener> listeners = new ArrayList<>();

    public GamePanel() {
        setPreferredSize(new Dimension(GAME_WINDOW_WIDTH * TILE_WIDTH, GAME_WINDOW_HEIGHT * TILE_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));

        player.setPosition(DEFAULT_PLAYER_START_X, DEFAULT_PLAYER_START_Y);

        addEntity(scene);
        addEntity(player);

        bindKeys();
    }

    private void bindKeys() {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(Keybindings.MOVE_UP_Z, Keybindings.ACTION_MOVE_UP);
        inputMap.put(Keybindings.MOVE_UP_ARROW, Keybindings.ACTION_MOVE_UP);
        inputMap.put(Keybindings.MOVE_DOWN_S, Keybindings.ACTION_MOVE_DOWN);
        inputMap.put(Keybindings.MOVE_DOWN_ARROW, Keybindings.ACTION_MOVE_DOWN);
        inputMap.put(Keybindings.MOVE_LEFT_Q, Keybindings.ACTION_MOVE_LEFT);
        inputMap.put(Keybindings.MOVE_LEFT_ARROW, Keybindings.ACTION_MOVE_LEFT);
        inputMap.put(Keybindings.MOVE_RIGHT_D, Keybindings.ACTION_MOVE_RIGHT);
        inputMap.put(Keybindings.MOVE_RIGHT_ARROW, Keybindings.ACTION_MOVE_RIGHT);
        inputMap.put(Keybindings.TOGGLE_INVENTORY, Keybindings.ACTION_TOGGLE_INVENTORY);

        actionMap.put(Keybindings.ACTION_MOVE_UP, new MoveAction(player::moveUp));
        actionMap.put(Keybindings.ACTION_MOVE_DOWN, new MoveAction(player::moveDown));
        actionMap.put(Keybindings.ACTION_MOVE_LEFT, new MoveAction(player::moveLeft));
        actionMap.put(Keybindings.ACTION_MOVE_RIGHT, new MoveAction(player::moveRight));
        actionMap.put(Keybindings.ACTION_TOGGLE_INVENTORY, new ToggleInventoryAction());
    }

    private void notifyPlayerUpdated() {
        for (GameListener l : listeners) {
            l.updatePlayer(player);
        }
        repaint();
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

        scene.renderWorld(
                g2d,
                TILE_WIDTH,
                TILE_HEIGHT,
                camera.getX(),
                camera.getY()
        );

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

    private class MoveAction extends AbstractAction {
        private final Consumer<WorldScene> move;

        MoveAction(Consumer<WorldScene> move) {
            this.move = move;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            move.accept(scene);
            notifyPlayerUpdated();
        }
    }

    private class ToggleInventoryAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (GameListener l : listeners) {
                l.toggleInventory();
            }
        }
    }
}
