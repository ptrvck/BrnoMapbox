package com.genius.petr.brnomapbox.horsin;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;

public class HorsinGameActivity extends AppCompatActivity {

    //declaring gameview
    private HorsinGameView horsinGameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Getting display object
        Display display = getWindowManager().getDefaultDisplay();

        //Getting the screen resolution into point object
        Point size = new Point();
        display.getSize(size);

        //Initializing game view object
        horsinGameView = new HorsinGameView(this, size.x, size.y);

        //adding it to contentview
        setContentView(horsinGameView);
    }

    //pausing the game when activity is paused
    @Override
    protected void onPause() {
        super.onPause();
        horsinGameView.pause();
    }

    //running the game when activity is resumed
    @Override
    protected void onResume() {
        super.onResume();
        horsinGameView.resume();
    }
}