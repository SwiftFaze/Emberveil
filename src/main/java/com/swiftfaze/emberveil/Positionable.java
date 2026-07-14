package com.swiftfaze.emberveil;

public interface Positionable {
    int getX();
    int getY();

    default int getZ() {
        return 0;
    }

}
