package com.genius.petr.brnomapbox;

import android.graphics.PointF;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.maps.Projection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Petr on 26. 3. 2017.
 */

public class ClusterHelper {
    private List<Cluster> currentClusters;
    private List<Cluster> allClusters;
    private int maxZoom;
    private int minZoom;
    private Icon clusterIcon;

    /*
    markers are 25dp * 40dp
     */

    int markerWidth;
    int markerHeight;

    private static final double BRNO_LAT = 49.195060;
    private static final int EARTH_CIRCUMFERENCE = 40075017;
    private static final int MARGIN = 5;

    public ClusterHelper(List<MarkerViewOptions> markers, int markerWidth, int markerHeight, Icon clusterIcon) {
        this.markerWidth = markerWidth;
        this.markerHeight = markerHeight;
        allClusters = clustersFromMarkers(markers);
        currentClusters = new ArrayList<>();

        //TODO: probably bad practice
        Cluster.setClusterIcon(clusterIcon);
    }

    public List<MarkerViewOptions> getMarkers() {

        List<MarkerViewOptions> markers = new ArrayList<>();

        for (Cluster c : currentClusters) {
            markers.add(c.getMarker());
        }

        return markers;
    }

    private double metersPerPixel(int zoomLevel) {
        double latitudeRadians = BRNO_LAT * (Math.PI/180);
        return EARTH_CIRCUMFERENCE * Math.cos(latitudeRadians) / Math.pow(2, zoomLevel + 8); //no, i dont know what that +8 means
    };


    public void recluster(Projection projection) {
        Log.i("cluster", "in");
        LinkedList<Cluster> source = new LinkedList<>(allClusters);
        currentClusters.clear();

        for(Cluster c : source) {
            c.updateScreenPosition(projection);
        }

        while (!source.isEmpty()) {
            Log.i("cluster", "looping");
            Iterator<Cluster> iterator = source.iterator();
            if (!iterator.hasNext())
                return;

            Cluster current = iterator.next();
            List<Cluster> clustered = new ArrayList<>();
            clustered.add(current);
            //i always remove at least one -> it has to end
            iterator.remove();
            //TODO: terribly unefficient?
            while (iterator.hasNext()) {
                Log.i("cluster", "looping in");
                Cluster other = iterator.next();
                if (overlap(current, other)) {
                    clustered.add(other);
                    iterator.remove();
                }
            }
            if (clustered.size() > 1) {
                currentClusters.add(new Cluster(clustered));
            } else {
                currentClusters.add(current);
            }
        }
    }

    private boolean overlap(Cluster first, Cluster second) {
        PointF firstPosition = first.getScreenPosition();
        PointF secondPosition = second.getScreenPosition();
        Log.i("cluster", "x: " + Float.toString(firstPosition.x) + "   y: " + Float.toString(firstPosition.y));
        Log.i("cluster", "x: " + Float.toString(secondPosition.x) + "   y: " + Float.toString(secondPosition.y));


        float xdiff = Math.abs(first.getScreenPosition().x - second.getScreenPosition().x);
        float ydiff = Math.abs(first.getScreenPosition().y - second.getScreenPosition().y);

        if ((ydiff < (markerHeight+MARGIN)) && (xdiff < (markerWidth+MARGIN))) {
            return true;
        }
        return false;
    }

    /*
    //marker dimensions should be in pixels
    public ClusterHelper(List<MarkerViewOptions> markers, int markerWidth, int markerHeight) {

        minZoom = 0;
        maxZoom = 16;

        this.markerWidth = markerWidth;
        this.markerHeight = markerHeight;

        currentClusters = new ArrayList<>(maxZoom+1); //this sets capacity not size
        //this sets size
        for (int i = 0; i<maxZoom+1; i++) {
            currentClusters.add(null);
        }

        currentClusters.set(maxZoom, clustersFromMarkers(markers));

        for (int zoom = maxZoom-1; zoom > minZoom; zoom--) {
            double metersPP = metersPerPixel(zoom);
            int widthThresh = (int)(markerWidth * metersPP + 0.5);
            int heightThresh = (int)(markerHeight * metersPP + 0.5);

            ArrayList<Cluster> current = new ArrayList<>();
            //TODO: incredibly inefficient
            LinkedList<Cluster> previous = new LinkedList<>(currentClusters.get(zoom+1));

            while (!previous.isEmpty()) {
                Iterator<Cluster> it = previous.iterator();
                Cluster cluster = it.next();
                List<Cluster> joined = new ArrayList<>();
                while(it.hasNext()) {
                    Cluster processed = it.next();
                    LatLng processedPosition = processed.getLocation();

                }
            }
        }

        Cluster cluster = new Cluster(currentClusters.get(maxZoom));
        List<Cluster> clusterList = new ArrayList<>();
        clusterList.add(cluster);
        currentClusters.set(0, clusterList);


        //TODO: compute all other markers
    }
    */

    private List<Cluster> clustersFromMarkers(List<MarkerViewOptions> markers) {
        List<Cluster> clusters = new ArrayList<>(markers.size());

        for (MarkerViewOptions marker : markers) {
            clusters.add(new Cluster(marker));
        }
        return clusters;
    }
}
