package com.genius.petr.brnomapbox;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by Petr on 3. 4. 2017.
 */


/*
mel bych si uz tady pamatovat, co je destination a co je druhy bod --pri zjistovani closestCutu bych to mel nejak spocitat
 */
public class PathCut {
    private LatLng destination;
    private int destinationIndex;
    private LatLng secondPoint;
    private int secondPointIndex;
    private LatLng cut;

    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }

    public int getDestinationIndex() {
        return destinationIndex;
    }

    public void setDestinationIndex(int destinationIndex) {
        this.destinationIndex = destinationIndex;
    }

    public LatLng getSecondPoint() {
        return secondPoint;
    }

    public void setSecondPoint(LatLng secondPoint) {
        this.secondPoint = secondPoint;
    }

    public int getSecondPointIndex() {
        return secondPointIndex;
    }

    public void setSecondPointIndex(int secondPointIndex) {
        this.secondPointIndex = secondPointIndex;
    }

    public LatLng getCut() {
        return cut;
    }

    public void setCut(LatLng cut) {
        this.cut = cut;
    }

    public void swapPoints() {
        LatLng tempPoint = destination;
        int tempIndex = destinationIndex;
        this.destination = this.secondPoint;
        this.destinationIndex = this.secondPointIndex;
        this.secondPoint = tempPoint;
        this.secondPointIndex = tempIndex;
    }
}
