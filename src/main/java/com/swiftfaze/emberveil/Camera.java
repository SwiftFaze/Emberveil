package com.swiftfaze.emberveil;

public class Camera {
    private int x;
    private int y;
    private final int viewportCols;
    private final int viewportRows;

    public Camera(int viewportCols, int viewportRows) {
        this.viewportCols = viewportCols;
        this.viewportRows = viewportRows;
    }

    public void centerOn(int targetX, int targetY) {
        x = targetX - viewportCols / 2;
        y = targetY - viewportRows / 2;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
