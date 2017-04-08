package com.genius.petr.brnomapbox.dickin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.constraint.solver.widgets.Rectangle;
import android.util.Log;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Petr on 20. 3. 2017.
 */

public class FuelManager {
    private int maxX;
    private int minX;
    private int maxY;

    private Context context;

    private Random generator = new Random();

    private LinkedList<Fuel> fuel = new LinkedList<>();
    private LinkedList<Fuel> unusedFuel = new LinkedList<>();

    public FuelManager(Context context, int screenX, int screenY) {
        maxX = screenX-10;
        minX = 10;
        maxY = screenY;

        this.context = context;

        Fuel f = new Fuel(context, screenY, generator.nextInt(maxX-minX) + minX);
        fuel.add(f);

    }

    public List<Fuel> getFuel() {
        return fuel;
    }

    //returns true if fuel was hit
    public boolean update(Rect playerBB, int rotation) {
        boolean hit = false;

        Iterator<Fuel> iterator = fuel.iterator();
        while (iterator.hasNext()) {
            Fuel f = iterator.next();
            boolean collision = f.update(playerBB, rotation);

            if (f.dead()) {
                Log.i("Game", "dead");
                iterator.remove();
                unusedFuel.add(f);
            }

            if (collision) {
                hit = true;
            }
        }


        boolean add = ((fuel.size() < 3) && (generator.nextInt(100) < 20));

        if (add) {
            int newX = generator.nextInt((maxX-minX)+minX);
            if (unusedFuel.size() > 0) {
                Fuel f = unusedFuel.get(0);


                f.reset(newX);
                fuel.add(f);
                unusedFuel.remove(0);
            } else {
                fuel.add(new Fuel(context, maxY, newX));
            }
        }
        return hit;
    }
}
