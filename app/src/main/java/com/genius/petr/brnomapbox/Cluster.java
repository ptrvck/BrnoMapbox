package com.genius.petr.brnomapbox;

import android.graphics.PointF;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.services.Constants;

import java.util.List;

/**
 * Created by Petr on 26. 3. 2017.
 */

public class Cluster {
    private List<Cluster> children;
    private MarkerViewOptions marker;
    private PointF screenPosition;

    //TODO: maybe bad practice
    private static Icon clusterIcon;
    //private int[] contains = new int[3];

    public static void setClusterIcon(Icon clusterIcon) {
        Cluster.clusterIcon = clusterIcon;
    }

    public MarkerViewOptions getMarker() {
        return marker;
    }

    public LatLng getLocation() {
        return marker.getPosition();
    }

    public Cluster(MarkerViewOptions marker) {
        children = null;
        this.marker = marker;
    }

    public void updateScreenPosition(Projection projection){
        this.screenPosition = projection.toScreenLocation(this.getLocation());
    }

    public PointF getScreenPosition(){
        return screenPosition;
    }

    public Cluster(List<Cluster> children) {
        this.children = children;
        int n = children.size();
        double lat = 0;
        double lng = 0;

        for (Cluster c : children) {
            lat += c.getLocation().getLatitude();
            lng += c.getLocation().getLongitude();
        }

        lat /= n;
        lng /= n;

        marker = new MarkerViewOptions()
                .position(new LatLng(lat, lng))
                .title("who cares")
                .snippet(Integer.toString(MyConstants.CLUSTER))
                .icon(clusterIcon)
                .anchor(0.5f,1.0f);
    }
}
