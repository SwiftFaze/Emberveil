package com.swiftfaze.emberveil.game;

public interface GameListener {
    void onPlayerMoved(int x, int y);
    void onHealthChanged(int hp, int maxHp);
}
