package com.swiftfaze.emberveil;

public class GameConst {
    public GameConst() {
    }

    /// GAME
    public static final int GAME_WIDTH = 50;
    public static final int GAME_HEIGHT = 50;
    public static final int GAME_DEPTH = 20;
    public static final int CHAR_WIDTH = 15;
    public static final int CHAR_HEIGHT = 15;
    /// DELAYS Z TRANSITION, CLOUDS AND SHADOWS
    public static final float Z_TRANSITION_SPEED = 0.12f;
    /// LOWER = DARKER PER LEVEL
    public static final float BRIGHTNESS_LEVEL_DECAY_RATE = 0.50f;
    public static final int SHADOW_START_DEPTH = 3;

    public static final float MAX_BRIGHTNESS = 1.0f;
    public static final float MIN_BRIGHTNESS = 0.12f;

    /// FOG
    public static final float FOG_ALPHA_COEFFICIENT = 0.5f;
    public static final int LEVEL_ABOVE_FOG_Z_LEVEL_START = 3;





}
