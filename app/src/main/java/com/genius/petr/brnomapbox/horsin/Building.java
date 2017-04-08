package com.genius.petr.brnomapbox.horsin;

import android.graphics.Rect;
import android.util.Log;

import java.util.Random;

/**
 * Created by Petr on 15. 3. 2017.
 */

public class Building {
    private static HorsinGameData gameData = new HorsinGameData();

    public Rect getRectangle() {
        return rectangle;
    }

    private Rect rectangle;

    private int maxX;
    private int width;
    private int height;
    private Random generator;


    public Building(int screenX, int screenY) {
        this(screenX, screenY, screenX, gameData.getRockBottom());
    }

    public Building(int screenX, int screenY, int startingX, int startingY) {
        maxX = screenX;
        generator = new Random();
        //generating a random coordinate
        //but keeping the coordinate inside the screen size

        width = generator.nextInt(50) +50;
        height = generator.nextInt(100) + 80;

        rectangle = new Rect();
        rectangle.bottom = startingY;
        rectangle.top = rectangle.bottom - height;
        rectangle.left = startingX + generator.nextInt(15) + 5;
        rectangle.right = rectangle.left + width;
    }

    public void reset() {
        rectangle.left = maxX;
        rectangle.right = maxX + width;
    }

    public void reset(int y) {
        rectangle.left = maxX;
        rectangle.right = maxX + width;
        rectangle.bottom = y;
        rectangle.top = y - height;
    }

    public void update(int diff) {
        rectangle.left -= diff;
        rectangle.right -= diff;
    }

    public boolean out() {
        return (rectangle.right < 0);
    }

    boolean clear() {
        Log.i("Game", "right " + Integer.toString(rectangle.right));
        Log.i("Game", "maxX " + maxX);
        //this is bullshit but its for keeping distance between buildings
        int threshold = rectangle.right + generator.nextInt(15) + 5;
        return (threshold < maxX);
    }
}
