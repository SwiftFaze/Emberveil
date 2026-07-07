package com.swiftfaze.emberveil.world;

import com.swiftfaze.emberveil.DrawableAsciiEntity;

import java.awt.*;

public class Background implements DrawableAsciiEntity {
    private final TileType[][] tiles;
    private final int cols;
    private final int rows;
    private static final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 50);

    public Background(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.tiles = new TileType[cols][rows];

        fillAll(TileType.EMPTY); // default terrain
        fillRegion(26, 17, 12 , 4, TileType.WATER);
        fillRegion(26, 22, 12 , 4, TileType.DIRT);
        fillRegion(26, 27, 12 , 4, TileType.GRASS);
        fillRegion(26, 12, 12 , 4, TileType.STONE);
    }

    public void fillAll(TileType type) {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                tiles[x][y] = type;
            }
        }
    }

    public void fillRegion(int startX, int startY, int width, int height, TileType type) {
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                if (x >= 0 && x < cols && y >= 0 && y < rows) {
                    tiles[x][y] = type;
                }
            }
        }
    }

    public void setTile(int x, int y, TileType type) {
        if (x >= 0 && x < cols && y >= 0 && y < rows) {
            tiles[x][y] = type;
        }
    }

    @Override
    public int getX() { return 0; }

    @Override
    public int getY() { return 0; }

    @Override
    public char getSymbol() { return ' '; }

    @Override
    public Color getColor() { return Color.WHITE; }

    @Override
    public void render(Graphics2D g2d, int tileWidth, int tileHeight, int cameraX, int cameraY) {
        g2d.setFont(font);

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                TileType type = tiles[x][y];
                int screenX = (x - cameraX) * tileWidth;
                int screenY = (y - cameraY) * tileHeight + tileHeight;

                g2d.setColor(type.getColor());
                g2d.drawString(String.valueOf(type.getSymbol()), screenX, screenY);
            }
        }
    }
}
