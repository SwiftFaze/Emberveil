package com.swiftfaze.emberveil;

public class Camera {
    private int x;
    private int y;
    private final int viewportWidth;
    private final int viewportHeight;

    public Camera(int viewportWidth, int viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    public void centerOn(int targetX, int targetY) {
        x = targetX - viewportWidth / 2;
        y = targetY - viewportHeight / 2;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
