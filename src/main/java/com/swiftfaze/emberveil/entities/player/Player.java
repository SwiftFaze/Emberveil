package com.swiftfaze.emberveil.entities.player;

import com.swiftfaze.emberveil.DrawableAsciiEntity;

import java.awt.*;

public class Player implements DrawableAsciiEntity {
    private int x;
    private int y;
    private static final char symbol = 'H';
    private static final Color color = Color.PINK;
    private static final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 18);

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    private void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public void moveUp() {
        move(0, -1);
    }

    public void moveDown() {
        move(0, 1);
    }

    public void moveLeft() {
        move(-1, 0);
    }

    public void moveRight() {
        move(1, 0);
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
