package com.genius.petr.brnomapbox.horsin;

/**
 * Created by Petr on 15. 3. 2017.
 */

public class HorsinGameData {
    private static int speed = 7;
    private static int rockBottom = 400;


    public static int getSpeed() {
        return speed;
    }

    public static void setSpeed(int speed) {
        HorsinGameData.speed = speed;
    }

    public static int getRockBottom() {
        return rockBottom;
    }

    public static void setRockBottom(int rockBottom) {
        HorsinGameData.rockBottom = rockBottom;
    }
}
