package com.genius.petr.brnomapbox.horsin;

/**
 * Created by Petr on 13. 3. 2017.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;

import com.genius.petr.brnomapbox.R;

import java.util.Random;

public class Obstacle {

    //bitmap for the enemy
    //we have already pasted the bitmap in the drawable folder
    private Bitmap bitmap;

    private int id;

    private boolean dead = false;

    //x and y coordinates
    private int x;
    private int y;

    private static HorsinGameData gameData = new HorsinGameData();

    //min and max coordinates to keep the enemy inside the screen
    private int maxX;
    private int minX;

    private int maxY;
    private int minY;

    private int reviveIn = 0;

    private Rect detectCollision;

    public Obstacle(Context context, int screenX) {
        //getting bitmap from drawable resource
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.obstacle);
        bitmap = getResizedBitmap(bitmap, 30, 30);

        //topleft corner
        detectCollision = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());

        //initializing min and max coordinates
        maxX = screenX;
        minX = -40;

        x = screenX;
        y = 450;
        id = 0;
    }

    private void afterlife() {
        reviveIn--;
        if (reviveIn <=0) {
            revive();
        }
    }

    private void die() {
        dead = true;
        Random generator = new Random();
        reviveIn = generator.nextInt(60) + 100;
    }

    public void revive() {
        dead = false;
        id++;
        x = maxX;
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

    public void update() {
        //decreasing x coordinate so that enemy will move right to left
        if (!dead) {
            x -= gameData.getSpeed();
            if (x < minX) {
                die();
            }
        } else {
            afterlife();
        }

        //image is not really as big as bitmap - TODO
        detectCollision.left = x+5;
        detectCollision.top = y+5;
        detectCollision.right = x + bitmap.getWidth()-5;
        detectCollision.bottom = y + bitmap.getHeight()-5;
    }



    //getters
    public Bitmap getBitmap() {
        return bitmap;
    }

    public Rect getDetectCollision() {
        return detectCollision;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getId() {
        return id;
    }
}
