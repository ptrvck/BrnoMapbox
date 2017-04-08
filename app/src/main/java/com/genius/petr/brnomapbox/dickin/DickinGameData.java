package com.genius.petr.brnomapbox.dickin;

/**
 * Created by Petr on 15. 3. 2017.
 */

public class DickinGameData {
    private static int speed = 7;
    private static int rockBottom = 900;


    public static int getSpeed() {
        return speed;
    }

    public static void setSpeed(int speed) {
        DickinGameData.speed = speed;
    }

    public static int getRockBottom() {
        return rockBottom;
    }

    public static void setRockBottom(int rockBottom) {
        DickinGameData.rockBottom = rockBottom;
    }
}
