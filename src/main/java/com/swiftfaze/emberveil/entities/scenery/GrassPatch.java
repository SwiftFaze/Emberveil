package com.swiftfaze.emberveil.entities.scenery;

import com.swiftfaze.emberveil.DrawableImageEntity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class GrassPatch implements DrawableImageEntity {
    private final int x;
    private final int y;
    private final int widthInTiles;
    private final int heightInTiles;
    private BufferedImage sprite;

    public GrassPatch(int x, int y, int widthInTiles, int heightInTiles) {
        this.x = x;
        this.y = y;
        this.widthInTiles = widthInTiles;
        this.heightInTiles = heightInTiles;
        loadSprite();
    }

    private void loadSprite() {
        try (InputStream is = getClass().getResourceAsStream("/grass.png")) {
            sprite = ImageIO.read(is);
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
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
    public void render(Graphics2D g2d, int tileWidth, int tileHeight, int cameraX, int cameraY) {
        if (sprite == null) return;

        for (int row = 0; row < heightInTiles; row++) {
            for (int col = 0; col < widthInTiles; col++) {
                int tileX = x + col;
                int tileY = y + row;

                int screenX = (tileX - cameraX) * tileWidth;
                int screenY = (tileY - cameraY) * tileHeight;

                g2d.drawImage(sprite, screenX, screenY, tileWidth, tileHeight, null);
            }
        }
    }
}
