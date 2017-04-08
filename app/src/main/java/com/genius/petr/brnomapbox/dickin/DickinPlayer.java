package com.genius.petr.brnomapbox.dickin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.genius.petr.brnomapbox.R;

public class DickinPlayer {
    private Bitmap normalBitmap;
    private static DickinGameData gameData = new DickinGameData();
    private Rect boundingBox;

    //coordinates
    private int x;
    private int y;

    private int run = 0;
    private int change = 1;

    private int rotation;

    private int height;
    private int width;

    private boolean jumpin = false;

    private final int GRAVITY = 5;

    //motion speed of the character
    private int speed = 0;
    private final int MAX_SPEED = 15;

    //constructor
    public DickinPlayer(Context context, int screenX, int screenY) {
        //x = left
        x = screenX / 2;
        y = gameData.getRockBottom();

        height = 200;
        width = 67;

        //Getting bitmap from drawable resource
        normalBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.dickin);
        normalBitmap = getResizedBitmap(normalBitmap, width, height);

        boundingBox =  new Rect(x, y, normalBitmap.getWidth(), normalBitmap.getHeight());
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


    //update based on coordinates of finger
    public void update(Point finger){
        Point vector;
        int newDegree = 0;
        if (finger != null) {
            speed += 1;
            if (speed > MAX_SPEED) {
                speed = MAX_SPEED;
            }

            vector = new Point(finger.x - x, finger.y - y);
            double length = Math.sqrt(vector.x * vector.x + vector.y * vector.y);
            if (length > 0) {

                //good enough approx of distance
                if ((Math.abs(finger.x - x) + Math.abs(finger.y - y)) > 20) {
                    Point p = new Point(x - finger.x, y - finger.y);

                    if(p.y < 0) {
                        p.y *= -1;
                    }

                    double l = Math.sqrt(p.x * p.x + p.y * p.y);
                    newDegree = (int) (Math.atan2(p.x / l, p.y / l) * 180 / Math.PI);
                    //Log.i("Game", "vector: [" + Double.toString((p.x) / l) +"] [" + Double.toString((p.y)/l) + "]");
                }
                vector.x = (int)((vector.x / length) * speed);
                vector.y = (int)((vector.y / length) * speed);
                //vector.y = (int)Math.min(GRAVITY-speed, (vector.y / length) * speed);
            }

        } else {
            speed -= 5;
            if (speed < 0) {
                speed = 0;
            }
            vector = new Point(0, GRAVITY - speed);
        }

        x+=vector.x;
        y+=vector.y;

       // Log.i("Game", "Degree: " + Integer.toString(newDegree));

        if (newDegree < rotation) {
            rotation--;
            if (newDegree > rotation) {
                rotation = newDegree;
            }
        } else if (newDegree > rotation){
            rotation++;
            if (newDegree < rotation) {
                rotation = newDegree;
            }
        }

        /*
        y += GRAVITY;
        y-=speed;
        */

        if (y > gameData.getRockBottom()) {
            y = gameData.getRockBottom();
        }

        boundingBox.top = y;
        boundingBox.bottom = y + height;
        boundingBox.left = x;
        boundingBox.right = x + width;
    }


    public Rect getBoundingBox() {
        return boundingBox;
    }

    public int getRotation() {
        return -rotation;
    }

    public Bitmap getBitmap() {
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