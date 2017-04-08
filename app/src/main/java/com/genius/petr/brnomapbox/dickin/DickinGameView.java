package com.genius.petr.brnomapbox.dickin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.genius.petr.brnomapbox.R;

import java.util.Random;

/**
 * Created by Petr on 13. 3. 2017.
 */

public class DickinGameView extends SurfaceView implements Runnable {

    //boolean variable to track if the game is playing or not
    volatile boolean playing;

    private static String[] badTexts = {"au!", "nech mě!", "neee!", "bolí!", "kšá!", "huš!"};
    private static String[] goodTexts = {"jooo!", "jupííí!", "hupky dupky!", "hele!", "íhááá!"};
    private String text;

    private FuelManager fuelManager;

    //the game thread
    private Thread gameThread = null;

    private DickinPlayer player;

    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;


    private Rect screen = new Rect();
    private Rect textBounds = new Rect();

    private int badTextColor;
    private int goodTextColor;

    private Point finger = null;

    int cryFor;
    int cheerFor;

    private DickinGameData gameData = new DickinGameData();

    //Class constructor
    public DickinGameView(Context context, int screenX, int screenY) {
        super(context);

        player = new DickinPlayer(context, screenX, screenY);
        fuelManager = new FuelManager(context, screenX, screenY);

        gameData.setRockBottom(screenY - 300);

        //initializing drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();

        this.screen.left = 0;
        this.screen. right = screenX;
        this.screen.top = 0;
        this.screen.bottom = screenY;

        badTextColor = ContextCompat.getColor(context, R.color.colorRestaurantAccent);
        goodTextColor = ContextCompat.getColor(context, R.color.colorBarAccent);

        final Handler h = new Handler();
        final int delay = 5000; //milliseconds

        h.postDelayed(new Runnable(){
            public void run(){
                gameData.setSpeed(gameData.getSpeed()+1);
                h.postDelayed(this, delay);
            }
        }, delay);

    }

    @Override
    public void run() {
        while (playing) {
            //to update the frame
            update();

            //to draw the frame
            draw();

            //to control
            control();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                finger = new Point((int)motionEvent.getX(), (int)motionEvent.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                finger = new Point((int)motionEvent.getX(), (int)motionEvent.getY());
                break;
            case MotionEvent.ACTION_UP:
                finger = null;
        }
        return true;
    }

    private void jump() {
        cheerFor = 45;
        cryFor = 0;
        Random generator = new Random();
        int pos = generator.nextInt(goodTexts.length);
        text = goodTexts[pos];
    }

    /*
    private void collision(Obstacle obstacle) {
        if(lastCollisionId == obstacle.getId()) {
            return;
        }

        lastCollisionId = obstacle.getId();
        cheerFor = 0;
        cryFor = 45;
        Random generator = new Random();
        int pos = generator.nextInt(badTexts.length);
        text = badTexts[pos];
    }
*/
    private void update() {
        player.update(finger);
        boolean hit = fuelManager.update(player.getBoundingBox(), player.getRotation());
        if (hit) {
            Log.i("Game", "hit");
            //TODO: add fuel
        }
    }

    private void drawText(Canvas canvas, boolean good) {
        if(good) {
            paint.setColor(goodTextColor);
        } else {
            paint.setColor(badTextColor);
        }
        paint.setTextSize(60);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        float x = screen.width() / 2f - textBounds.width() / 2f - textBounds.left;
        float y = screen.height() / 3f;
        canvas.drawText(text, x, y, paint);
    }
/*
    private void drawBuildings(Canvas canvas) {
        paint.setColor(Color.argb(255,10,10,10));
        for (Building b : buildings.getFarBuildings()) {
            canvas.drawRect(b.getRectangle(), paint);
        }

        paint.setColor(Color.argb(255,20,20,20));
        for (Building b : buildings.getCloseBuildings()) {
            canvas.drawRect(b.getRectangle(), paint);
        }
    }
*/
    private void draw() {
        //checking if surface is valid
        if (surfaceHolder.getSurface().isValid()) {
            //locking the canvas
            canvas = surfaceHolder.lockCanvas();
            //drawing a background color for canvas
            canvas.drawColor(Color.BLACK);


            Matrix rotator = new Matrix();

            // rotate around (0,0)
            rotator.postRotate(player.getRotation());

            // or, rotate around x,y
            // NOTE: coords in bitmap-space!
           // int xRotate = ...
            //int yRotate = ...
            //rotator.postRotate(90, xRotate, yRotate);

            rotator.postTranslate(player.getX(), player.getY());

            canvas.drawBitmap(player.getBitmap(), rotator, paint);

            for (Fuel f : fuelManager.getFuel()) {
                canvas.drawBitmap(f.getBitmap(), f.getX(), f.getY(), paint);
            }
/*
            //Drawing the player
            canvas.drawBitmap(
                    player.getBitmap(),
                    player.getX(),
                    player.getY(),
                    paint);
*/

            if (cheerFor > 0) {
                cheerFor--;
                drawText(canvas, true);
            } else if (cryFor > 0) {
                cryFor--;
                drawText(canvas, false);
            }

            //Unlocking the canvas
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }
    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        //when the game is paused
        //setting the variable to false
        playing = false;
        try {
            //stopping the thread
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        //when the game is resumed
        //starting the thread again
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}