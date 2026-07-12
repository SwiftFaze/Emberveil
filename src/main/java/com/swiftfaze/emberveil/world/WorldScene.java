package com.swiftfaze.emberveil.world;

import com.swiftfaze.emberveil.DrawableAsciiEntity;

import java.awt.*;

public abstract class WorldScene implements DrawableAsciiEntity {
    private final Tile[][] tiles;
    private final int width;
    private final int height;
    private static final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 15);

    protected WorldScene(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[width][height];

        fillAll(Tile.EMPTY);
    }

    public void fillAll(Tile type) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = type;
            }
        }
    }

    public void fillRegion(int startX, int startY, int width, int height, Tile type) {
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                if (x >= 0 && x < this.width && y >= 0 && y < this.height) {
                    tiles[x][y] = type;
                }
            }
        }
    }

    public void createBorder(int width, int height, Tile type) {
        fillRegion(0,0,width,1, type);
        fillRegion(0,0,1,height, type);
        fillRegion(0,height-1,width,1, type);
        fillRegion(width-1,0,1,height, type);
    }


    public boolean isWalkable(int x, int y) {
        /// IF NOT OUTSIDE SCENE DIMENSIONS
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        /// CAN TRAVERSE TILE
        Tile type = tiles[x][y];
        return type != null && type.isWalkable();
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

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile type = tiles[x][y];
                int screenX = (x - cameraX) * tileWidth;
                int screenY = (y - cameraY) * tileHeight + tileHeight;

                g2d.setColor(type.getColor());
                g2d.drawString(String.valueOf(type.getSymbol()), screenX, screenY);
            }
        }
    }
}
