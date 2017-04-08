package com.genius.petr.brnomapbox.horsin;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Petr on 15. 3. 2017.
 */

public class Buildings {
    private int maxX;
    private int maxY;

    private int closeY;
    private int farY;

    private static HorsinGameData gameData = new HorsinGameData();

    private LinkedList<Building> farBuildings = new LinkedList<>();
    private LinkedList<Building> closeBuildings = new LinkedList<>();
    private LinkedList<Building> unusedBuildings = new LinkedList<>();

    public Buildings(int screenX, int screenY) {
        maxX = screenX;
        maxY = screenY;

        farY = gameData.getRockBottom() - 60;
        closeY = gameData.getRockBottom() - 30;


        Building b = new Building(screenX, screenY, 0, closeY);
        int currentX = b.getRectangle().right;
        closeBuildings.add(b);

        while (currentX < maxX) {
            b = new Building(maxX, maxY,currentX, closeY);
            closeBuildings.add(b);
            currentX = b.getRectangle().right;
        }

        b = new Building(screenX, screenY, 0, farY);
        currentX = b.getRectangle().right;
        farBuildings.add(b);

        while (currentX < maxX) {
            b = new Building(maxX, maxY,currentX, farY);
            farBuildings.add(b);
            currentX = b.getRectangle().right;
        }
    }

    public List<Building> getCloseBuildings() {
        return closeBuildings;
    }

    public List<Building> getFarBuildings() {
        return farBuildings;
    }

    public void update() {
        update(closeBuildings, closeY, gameData.getSpeed());
        update(farBuildings, farY, (int)(gameData.getSpeed() * 0.8f));
    }

    private void update(List<Building> buildings, int y, int speed) {
        int counter = buildings.size();

        boolean add = false;

        Iterator<Building> iterator = buildings.iterator();
        while (iterator.hasNext()) {
            counter--;
            Building b = iterator.next();
            b.update(speed);


            //last item
            if (counter == 0 && b.clear()) {
                add = true;
            }


            if (b.out()) {
                iterator.remove();
                unusedBuildings.add(b);
            }
        }

        if (add) {
            if (unusedBuildings.size() > 0) {
                Building b = unusedBuildings.get(0);
                b.reset(y);
                buildings.add(b);
                unusedBuildings.remove(0);
            } else {
                buildings.add(new Building(maxX, maxY, maxX, y));
            }
        }
    }

}
