package com.swiftfaze.emberveil.world;

import com.swiftfaze.emberveil.DrawableAsciiEntity;

import java.awt.*;

import static com.swiftfaze.emberveil.GameConst.*;

public abstract class WorldScene implements DrawableAsciiEntity {
    private final Tile[][][] tiles;
    private final int width;
    private final int height;
    private final int depth;
    private static final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 15);

    protected WorldScene(int width, int height) {
        this(width, height, 10);
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

    public void fillRegion(int startZ, int startX, int startY, int width, int height, Tile type) {
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                if (x >= 0 && x < this.width && y >= 0 && y < this.height) {
                    tiles[startZ][x][y] = type;
                }
            }
        }
    }

    public void fillHouseRegion(int startZ, int startX, int startY, int width, int height, Tile wallTile, Tile floorTile) {
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                if (x >= 0 && x < this.width && y >= 0 && y < this.height) {

                    Tile tile = getTile(x, y, startZ);

                    if (tile == Tile.WOOD && wallTile == Tile.STONE) {
                        tiles[startZ][x][y] = floorTile;
                    } else {
                        tiles[startZ][x][y] = wallTile;
                    }
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
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public char getSymbol() {
        return ' ';
    }

    @Override
    public Color getColor() {
        return Color.WHITE;
    }

    @Override
    public void render(Graphics2D g2d, int tileWidth, int tileHeight, int cameraX, int cameraY) {
        renderWorld(g2d, tileWidth, tileHeight, cameraX, cameraY, 0, MAX_BRIGHTNESS);
    }

    public void renderWorld(Graphics2D g2d,
                            int tileWidth,
                            int tileHeight,
                            int cameraX,
                            int cameraY,
                            int z,
                            float brightness) {

        Tile[][] layer = tiles[z];

        g2d.setFont(font);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                Tile type = layer[x][y];

                if (type == null || type == Tile.EMPTY)
                    continue;


                int screenX = (x - cameraX) * tileWidth;
                int screenY = (y - cameraY) * tileHeight + tileHeight;


                g2d.setColor(dim(type.getColor(), brightness));
                g2d.drawString(
                        String.valueOf(type.getSymbol()),
                        screenX,
                        screenY
                );
            }
        }
    }

    public void renderClouds(Graphics2D g2d,
                             int tileWidth,
                             int tileHeight,
                             int cameraX,
                             int cameraY,
                             int z,
                             float heightAbove) {


        float fogAmount = 0f;

        if (heightAbove > LEVEL_ABOVE_FOG_Z_LEVEL_START) {
            fogAmount = Math.min(MAX_BRIGHTNESS, heightAbove - LEVEL_ABOVE_FOG_Z_LEVEL_START);
        }

        if (fogAmount <= 0) return;


        Tile[][] layer = tiles[z];

        g2d.setFont(font);

        g2d.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        FOG_ALPHA_COEFFICIENT * fogAmount
                )
        );

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                Tile tile = layer[x][y];

                if (tile == null)
                    continue;

                int screenX = (x - cameraX) * tileWidth;
                int screenY = (y - cameraY) * tileHeight + tileHeight;

                g2d.setColor(Tile.CLOUD.getColor());

                g2d.drawString(
                        String.valueOf(Tile.CLOUD.getSymbol()),
                        screenX,
                        screenY
                );
            }
        }

        g2d.setComposite(AlphaComposite.SrcOver);
    }

    private static Color dim(Color color, float factor) {
        if (factor >= MAX_BRIGHTNESS) return color;
        int r = (int) (color.getRed() * factor);
        int g = (int) (color.getGreen() * factor);
        int b = (int) (color.getBlue() * factor);
        return new Color(r, g, b);
    }
}
