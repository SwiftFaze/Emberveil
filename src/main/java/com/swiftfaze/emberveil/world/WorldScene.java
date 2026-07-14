package com.swiftfaze.emberveil.world;

import com.swiftfaze.emberveil.DrawableAsciiEntity;

import java.awt.*;

public abstract class WorldScene implements DrawableAsciiEntity {
    private final Tile[][][] tiles; // [z][x][y]
    private final int width;
    private final int height;
    private final int depth;
    private static final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 15);

    protected WorldScene(int width, int height) {
        this(width, height, 1); // default: single floor, backward compatible
    }

    protected WorldScene(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.tiles = new Tile[depth][width][height];

        fillAll(Tile.EMPTY);
    }

    public void fillAll(Tile type) {
        fillAll(0, type);
    }

    public void fillAll(int z, Tile type) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[z][x][y] = type;
            }
        }
    }

    public void fillRegion(int startX, int startY, int width, int height, Tile type) {
        fillRegion(0, startX, startY, width, height, type);
    }

    public void fillRegion(int z, int startX, int startY, int width, int height, Tile type) {
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                if (x >= 0 && x < this.width && y >= 0 && y < this.height) {
                    tiles[z][x][y] = type;
                }
            }
        }
    }

    public void createBorder(int width, int height, Tile type) {
        createBorder(0, width, height, type);
    }

    public void createBorder(int z, int width, int height, Tile type) {
        fillRegion(z, 0, 0, width, 1, type);
        fillRegion(z, 0, 0, 1, height, type);
        fillRegion(z, 0, height - 1, width, 1, type);
        fillRegion(z, width - 1, 0, 1, height, type);
    }

    public boolean isWalkable(int x, int y) {
        return isWalkable(x, y, 0);
    }

    public boolean isWalkable(int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
            return false;
        }
        Tile type = tiles[z][x][y];
        return type != null && type.isWalkable();
    }

    public Tile getTile(int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
            return null;
        }
        return tiles[z][x][y];
    }

    public int getDepth() {
        return depth;
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
        renderLayer(g2d, tileWidth, tileHeight, cameraX, cameraY, 0, 1.0f);
    }
    public int getSurfaceHeight(int x, int y) {
        for (int z = getDepth() - 1; z >= 0; z--) {
            if (isWalkable(x, y, z)) return z;
        }
        return -1; // no ground found in this column
    }
    public void renderLayer(Graphics2D g2d, int tileWidth, int tileHeight, int cameraX, int cameraY, int z, float brightness) {
        Tile[][] layer = tiles[z];
        g2d.setFont(font);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile type = layer[x][y];
                if (type == null) continue;

                int screenX = (x - cameraX) * tileWidth;
                int screenY = (y - cameraY) * tileHeight + tileHeight;

                g2d.setColor(dim(type.getColor(), brightness));
                g2d.drawString(String.valueOf(type.getSymbol()), screenX, screenY);
            }
        }
    }

    private static Color dim(Color color, float factor) {
        if (factor >= 1.0f) return color;
        int r = (int) (color.getRed() * factor);
        int g = (int) (color.getGreen() * factor);
        int b = (int) (color.getBlue() * factor);
        return new Color(r, g, b);
    }
}
