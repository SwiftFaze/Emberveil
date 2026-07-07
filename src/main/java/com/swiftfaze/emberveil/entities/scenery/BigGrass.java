package com.swiftfaze.emberveil.entities.scenery;

import com.swiftfaze.emberveil.DrawableImageEntity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class BigGrass implements DrawableImageEntity {
    private final int x;
    private final int y;
    private BufferedImage sprite;

    public BigGrass(int x, int y) {
        this.x = x;
        this.y = y;
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
        int screenX = (x - cameraX) * tileWidth;
        int screenY = (y - cameraY) * tileHeight;

        g2d.drawImage(sprite, screenX, screenY, tileWidth*3, tileHeight*3, null);
    }
}
