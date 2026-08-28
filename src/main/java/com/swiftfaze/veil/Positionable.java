package com.swiftfaze.veil;

public interface Positionable {
    int getX();
    int getY();

    default int getZ() {
        return 0;
    }

}
