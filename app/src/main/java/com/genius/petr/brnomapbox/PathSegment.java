package com.genius.petr.brnomapbox;

import android.graphics.Path;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petr on 1. 4. 2017.
 */

public class PathSegment {
    List<LatLng> keyPoints;
    PathCut currentCut;
    //LatLng position;
    //LatLng nextKeypoint;

    //if i am closer to keypoint than stub, i am going backwards -> wrong

    public PathSegment(PathCut cut) {
        this.keyPoints = new ArrayList<>();
        //this.keyPoints.add(cut.getSecondPoint());
        this.keyPoints.add(cut.getCut());
        this.keyPoints.add(cut.getCut());
        this.currentCut = cut;
    }

    public List<LatLng> getKeyPoints(){
        return keyPoints;
    }

    public PathCut getCurrentCut(){
        return currentCut;
    }

    public List<LatLng> getPath(){
        return keyPoints;
    }

    /*
    public PathSegment(LatLng keypoint, LatLng nextKeypoint, LatLng position) {
        keyPoints = new ArrayList<>();
        keyPoints.add(keypoint);
        this.nextKeypoint = nextKeypoint;
        this.position = position;
    }
    */


    //path cut will NEVER skip two points at one call
    //returns false if this segment should be abandoned
    public boolean update(PathCut cut) {
        Log.i("Circuit", "in");
        if (currentCut.getDestination().equals(cut.getDestination())) {
            currentCut = cut;
            keyPoints.set(keyPoints.size()-1, cut.getCut());
            return true;
        }
        //preskocilo se na novy bod
        if (currentCut.getDestination().equals((cut.getSecondPoint()))) {
            keyPoints.set(keyPoints.size()-1, currentCut.getDestination());
            keyPoints.add(cut.getCut());
            /*
            keyPoints.add(currentCut.getSecondPoint()); //add old second point
            */
            currentCut = cut;
            return true;
        }

        //we are heading backwards to the beginning
        if (keyPoints.get(0).equals(cut.getDestination())) {
            return false;
        }

        //at this point i should be somewhere on the path, but not at either end
        return true;
    }


    /*
    public void update(LatLng position) {
        if (position.distanceTo(nextKeypoint) < this.position.distanceTo(nextKeypoint)) {
            this.position = position;
        }
    }

*/


    public void addKeypoint(LatLng keypoint) {
        this.keyPoints.add(keypoint);
    }
    /*

    //returns false if this segment should be abandoned
    public boolean update (LatLng position, LatLng nextKeypoint) {
        this.update(position);
        if (nextKeypoint.equals(this.nextKeypoint)) {
            return true;
        }

        //if next keypoint should be original keypoint, we are going backwards
        if (nextKeypoint.equals(keyPoints.get(keyPoints.size()-1))) {
            return false;
        }

        for (LatLng keypoint : keyPoints) {
            if (keypoint.equals(nextKeypoint)) {
                //it was visited before, but we are still on the segment
                return true;
            }
        }

        keyPoints.add(this.nextKeypoint);
        this.nextKeypoint = nextKeypoint;
        return true;
    }
    */
}
