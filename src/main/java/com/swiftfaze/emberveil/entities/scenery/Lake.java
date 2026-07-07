package com.swiftfaze.emberveil.entities.scenery;

import com.swiftfaze.emberveil.DrawableAsciiEntity;

import java.awt.*;

public class Lake implements DrawableAsciiEntity {

    private final int x;
    private final int y;
    private final int widthInTiles;
    private final int heightInTiles;
    private static final char symbol = '~';
    private static final Color color = Color.BLUE;
    private static final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 18);

    public Lake(int x, int y, int widthInTiles, int heightInTiles) {
        this.x = x;
        this.y = y;
        this.widthInTiles = widthInTiles;
        this.heightInTiles = heightInTiles;
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
        for (int row = 0; row < heightInTiles; row++) {
            for (int col = 0; col < widthInTiles; col++) {
                int tileX = x + col;
                int tileY = y + row;

                int screenX = (tileX - cameraX) * tileWidth;
                int screenY = (tileY - cameraY) * tileHeight;

                g2d.drawString(String.valueOf(symbol), screenX, screenY);

            }
        }
    }
}
