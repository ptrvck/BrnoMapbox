package com.genius.petr.brnomapbox.dickin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import com.genius.petr.brnomapbox.R;

import java.util.Random;

/**
 * Created by Petr on 20. 3. 2017.
 */

public class Fuel {
    private static DickinGameData gameData = new DickinGameData();

    //bitmap for the enemy
    //we have already pasted the bitmap in the drawable folder
    private Bitmap bitmap;

    private int id;

    private boolean dead = false;

    //x and y coordinates
    private int x;
    private int y;


    private int maxY;

    public Fuel(Context context, int screenY, int x) {
        //getting bitmap from drawable resource
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.obstacle);
        bitmap = getResizedBitmap(bitmap, 30, 30);

        maxY = screenY + 40;


        //initializing min and max coordinates
        this.x = x;
        y = -40;
    }

    public void reset(int x) {
        dead = false;
        this.x = x;
        y = -40;
    }

    private boolean collision(Rect playerBB, int rotation) {
        //TODO: hardcoded
        int circleX = x+20;
        int circleY = y+20;
        int radius = 20;

        double angle = Math.toRadians(rotation);

        double unrotatedCircleX = Math.cos(angle) * (circleX - playerBB.centerX()) -
                Math.sin(angle) * (circleY - playerBB.centerY()) + playerBB.centerX();
        double unrotatedCircleY  = Math.sin(angle) * (circleX - playerBB.centerX()) +
                Math.cos(angle) * (circleY - playerBB.centerY()) + playerBB.centerY();

        // Closest point in the rectangle to the center of circle rotated backwards(unrotated)
        double closestX, closestY;

        // Find the unrotated closest x point from center of unrotated circle
        if (unrotatedCircleX  < playerBB.left)
            closestX = playerBB.left;
        else if (unrotatedCircleX  > playerBB.right)
            closestX = playerBB.right;
        else
            closestX = unrotatedCircleX ;

// Find the unrotated closest y point from center of unrotated circle
        if (unrotatedCircleY < playerBB.top)
            closestY = playerBB.top;
        else if (unrotatedCircleY > playerBB.bottom)
            closestY = playerBB.bottom;
        else
            closestY = unrotatedCircleY;

// Determine collision

        double distance = findDistance(unrotatedCircleX , unrotatedCircleY, closestX, closestY);
        if (distance < radius)
            return true; // Collision
        else
            return false;
    }

    private static double findDistance(double fromX, double fromY, double toX, double toY){
        double a = Math.abs(fromX - toX);
        double b = Math.abs(fromY - toY);

        return Math.sqrt((a * a) + (b * b));
    }

    private static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    //returns true if collision happens
    public boolean update(Rect playerBB, int rotation) {
        //decreasing x coordinate so that enemy will move right to left
        if (!dead) {
            y += gameData.getSpeed();
            if (y > maxY) {
                dead = true;
            }
            if (collision(playerBB, rotation)){
                dead = true;
                Log.i("Game", "collision");
                return true;

            }
        }
        return false;
    }



    //getters
    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean dead() {
        return dead;
    }

    public int getId() {
        return id;
    }
}
