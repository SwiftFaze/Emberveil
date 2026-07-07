package com.swiftfaze.emberveil;

import java.awt.*;

public interface DrawableImageEntity extends Positionable {
    void render(Graphics2D g2d, int tileWidth, int tileHeight, int cameraX, int cameraY);
}
