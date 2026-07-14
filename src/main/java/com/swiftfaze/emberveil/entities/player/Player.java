package com.swiftfaze.emberveil.entities.player;

import com.swiftfaze.emberveil.DrawableAsciiEntity;
import com.swiftfaze.emberveil.world.WorldScene;

import java.awt.*;
import java.util.logging.Logger;

public class Player implements DrawableAsciiEntity {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private int x;
    private int y;
    private int z = 0;
    private static final char symbol = '◼';
    private static final Color color = Color.decode("#ef481f");
    private static final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 18);

    private final PlayerInfo playerInfo;

    public Player(int startX, int startY) {
        this.playerInfo = new PlayerInfo();
        this.x = startX;
        this.y = startY;
    }

    public void moveUp(WorldScene worldScene) {
        move(0, -1, worldScene);
    }

    public void moveDown(WorldScene worldScene) {
        move(0, 1, worldScene);
    }

    public void moveLeft(WorldScene worldScene) {
        move(-1, 0, worldScene);
    }

    public void moveRight(WorldScene worldScene) {
        move(1, 0, worldScene);
    }
    public void setPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    private void move(int dx, int dy, WorldScene worldScene) {
        int newX = x + dx;
        int newY = y + dy;

        // Same floor.
        if (worldScene.isWalkable(newX, newY, z)) {
            x = newX;
            y = newY;
            return;
        }

        // Natural terrain step-up.
        if (worldScene.isWalkable(newX, newY, z + 1)) {
            x = newX;
            y = newY;
            z++;
            return;
        }

        // Natural terrain step-down.
        if (worldScene.isWalkable(newX, newY, z - 1)) {
            x = newX;
            y = newY;
            z--;
        }
    }

    public void forceAscend() {
            z++;
    }

    public void forceDescend() {
            z--;
    }
    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }
    @Override
    public int getZ() {
        return z;
    }
    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public char getSymbol() {
        return symbol;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void render(Graphics2D g2d, int tileWidth, int tileHeight, int cameraX, int cameraY) {
        g2d.setFont(font);
        g2d.setColor(color);
        int screenX = (x - cameraX) * tileWidth;
        int screenY = (y - cameraY) * tileHeight + tileHeight;
        g2d.drawString(String.valueOf(symbol), screenX, screenY);
    }
}
