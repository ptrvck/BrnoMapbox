package com.genius.petr.brnomapbox.horsin;

/**
 * Created by Petr on 13. 3. 2017.
 */

import com.genius.petr.brnomapbox.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

public class HorsinPlayer {
    //Bitmap to get character from image
    private Bitmap jumpinBitmap;
    private Bitmap normalBitmap;

    private Rect detectCollision;

    //coordinates
    private int x;
    private int y;

    private int run = 0;
    private int change = 1;

    private boolean jumpin = false;

    private final int GRAVITY = -2;
    private final int ROCK_BOTTOM = 400;

    //motion speed of the character
    private int speed = 0;

    //constructor
    public HorsinPlayer(Context context) {
        x = 150;
        y = ROCK_BOTTOM;

        //Getting bitmap from drawable resource
        normalBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.horsin);
        normalBitmap = getResizedBitmap(normalBitmap, 120, 80);
        jumpinBitmap =  BitmapFactory.decodeResource(context.getResources(), R.drawable.horsin_jumpin);
        jumpinBitmap = getResizedBitmap(jumpinBitmap, 120, 80);


        detectCollision =  new Rect(x, y, jumpinBitmap.getWidth(), jumpinBitmap.getHeight());
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

    //returns true if it starts jump
    public boolean jump() {
        Log.i("Game", "in jump");
        if (!jumpin) {
            Log.i("Game","Starting jumppp!");
            speed = -35;
            jumpin = true;
            return true;
        }
        return false;
    }


    //Method to update coordinate of character
    public void update(){

        if (jumpin) {
            Log.i("Game","Jumpin!");
            speed = speed - GRAVITY;
            y += speed;

            if (y > ROCK_BOTTOM) {
                y = ROCK_BOTTOM;
                jumpin = false;
                run = 0;
            }
        } else {
            run++;
            if (run % 10 == 0) {
                change *= -1;
            }
            y+=change;
        }

        detectCollision.left = x + 40;
        detectCollision.top = y;
        detectCollision.right = x + jumpinBitmap.getWidth() - 40;
        detectCollision.bottom = y + jumpinBitmap.getHeight();


    }

    public Rect getDetectCollision() {
        return detectCollision;
    }


    public Bitmap getBitmap() {
        if (jumpin) {
            return jumpinBitmap;
        }
        return normalBitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSpeed() {
        return speed;
    }
}