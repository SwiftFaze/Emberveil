package com.swiftfaze.emberveil.entities.player;

import com.swiftfaze.emberveil.DrawableAsciiEntity;
import com.swiftfaze.emberveil.world.WorldScene;

import java.awt.*;

public class Player implements DrawableAsciiEntity {
    private int x;
    private int y;
    private static final char symbol = '⏺';
    private static final Color color = Color.decode("#ef481f");
    private static final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 18);

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    private void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public void moveUp(WorldScene worldScene) {
        tryMove(0, -1, worldScene);
    }

    public void moveDown(WorldScene worldScene) {
        tryMove(0, 1, worldScene);
    }

    public void moveLeft(WorldScene worldScene) {
        tryMove(-1, 0, worldScene);
    }

    public void moveRight(WorldScene worldScene) {
        tryMove(1, 0, worldScene);
    }

    private void tryMove(int dx, int dy, WorldScene worldScene) {
        int newX = x + dx;
        int newY = y + dy;

        if (worldScene.isWalkable(newX, newY)) {
            x = newX;
            y = newY;
        }
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
