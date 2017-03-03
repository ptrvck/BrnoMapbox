package com.genius.petr.brnomapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
/**
 * Created by Petr on 11. 2. 2017.
 */

public class Feature {
    private final String id;
    private String name;
    private LatLng location;

    Feature(String id) {
        this.id = id;
        this.name = null;
        this.location = null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setLocation(double lat, double lng) {
        this.location = new LatLng(lat, lng);
    }

    public boolean valid() {
        return ((id!=null) && (location!=null) &&(name!=null));
    }
}
