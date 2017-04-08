package com.genius.petr.brnomapbox;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.util.TypedValue;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.Projection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

/**
 * Created by Petr on 7. 3. 2017.
 */

public class PlacesManager extends ContextWrapper {

    private Marker activeMarker = null;
    private Icon activeMarkerNormalIcon; //just for convenience, not that anything of what I'm doing here was really convenient
    private Icon restaurantsIcon;
    private Icon restaurantsActiveIcon;
    private Icon barsIcon;
    private Icon barsActiveIcon;
    private Icon databaseIcon;
    private Icon databaseActiveIcon;
    private Icon clusterIcon;
    private boolean barsVisible = true;
    private boolean databaseVisible = true;
    private boolean restaurantsVisible = true;

    private ClusterHelper clusterHelper;



    private SparseArray<Place> places;
    private SparseArray<MarkerViewOptions> markers;
    private List<MarkerViewOptions> dbMarkers = new ArrayList<>();
    private List<MarkerViewOptions> restaurantMarkers = new ArrayList<>();
    private List<MarkerViewOptions> barMarkers = new ArrayList<>();
    private List<MarkerViewOptions> visibleMarkers = new ArrayList<>();
    private List<MarkerViewOptions> clusteredMarkers = new ArrayList<>();

    private Places placesByCategory = new Places();


    public PlacesManager(Context context){
        super(context);
        places = new SparseArray<>();
        markers = new SparseArray<>();

        loadDb();
        Log.d("Debug","DB loaded");
        Log.d("Debug","Places loaded: "+Integer.toString(places.size()));
        initIcons();
        Log.d("Debug","icons loaded");
        initMarkers();
        Log.d("Debug","markers created");

        //TODO: probably shouldnt be hardcoded
        //in dp
        int width = 25;
        int height = 40;

        width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, getResources().getDisplayMetrics());
        height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());



        //test
        MarkerIconHelper help = new MarkerIconHelper(getApplicationContext());
        Bitmap icoBitmap = help.getMarkerBitmapWithText("1");
        Drawable iconDrawable = new BitmapDrawable(getResources(), icoBitmap);
        IconFactory iconFactory = IconFactory.getInstance(getApplicationContext());
        Icon ico = iconFactory.fromDrawable(iconDrawable);
        MarkerViewOptions m = new MarkerViewOptions()
                .position(new LatLng(49.19106, 16.611419))
                .title("experiment")
                .snippet(Integer.toString(1))
                .anchor(0.5f,1.0f)
                .icon(ico);
        visibleMarkers.add(m);
        //test over

        clusterHelper = new ClusterHelper(visibleMarkers, width, height, clusterIcon);
    }

    public void cluster(Projection projection) {
        clusterHelper.recluster(projection);
        clusteredMarkers = clusterHelper.getMarkers();
    }

    private void initIcons() {
        Context context = getApplicationContext();
        IconFactory iconFactory = IconFactory.getInstance(getApplicationContext());
        Drawable iconDrawable = VectorDrawableCompat.create(context.getResources(), R.drawable.restaurant, context.getTheme());
        //Drawable iconDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.restaurant);
        restaurantsIcon = iconFactory.fromDrawable(iconDrawable);
        iconDrawable = VectorDrawableCompat.create(context.getResources(), R.drawable.restaurant_active, context.getTheme());
        //iconDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.restaurant_active);
        restaurantsActiveIcon = iconFactory.fromDrawable(iconDrawable);

        iconDrawable = VectorDrawableCompat.create(context.getResources(), R.drawable.bar, context.getTheme());
        //iconDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.bar);
        barsIcon = iconFactory.fromDrawable(iconDrawable);
        iconDrawable = VectorDrawableCompat.create(context.getResources(), R.drawable.bar_active, context.getTheme());
        //iconDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.bar);
        barsActiveIcon = iconFactory.fromDrawable(iconDrawable);

        iconDrawable = VectorDrawableCompat.create(context.getResources(), R.drawable.database, context.getTheme());
        //iconDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.database);
        databaseIcon = iconFactory.fromDrawable(iconDrawable);
        iconDrawable = VectorDrawableCompat.create(context.getResources(), R.drawable.database_active, context.getTheme());
        //iconDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.database_active);
        databaseActiveIcon = iconFactory.fromDrawable(iconDrawable);

        iconDrawable = VectorDrawableCompat.create(context.getResources(), R.drawable.cluster, context.getTheme());
        clusterIcon = iconFactory.fromDrawable(iconDrawable);
    }

    public Place getPlace(int id) {
        return places.get(id);
    }

    public MarkerViewOptions getMarker(int id) {
        return markers.get(id);
    }

    public void reloadDb(){

    }

    public List<MarkerViewOptions> getMarkers(){
        //return visibleMarkers;
        return clusteredMarkers;
    }

    public void toggleDb() {
        databaseVisible = !databaseVisible;
        updateVisibleMarkers();
    }

    public void toggleRestaurants() {
        restaurantsVisible = !restaurantsVisible;
        updateVisibleMarkers();
    }

    public void toggleBars() {
        barsVisible = !barsVisible;
        updateVisibleMarkers();
    }

    private void updateVisibleMarkers() {
        visibleMarkers.clear();

        if (barsVisible) {
            visibleMarkers.addAll(barMarkers);
        }
        if (restaurantsVisible) {
            visibleMarkers.addAll(restaurantMarkers);
        }
        if (databaseVisible) {
            visibleMarkers.addAll(dbMarkers);
        }
    }

/*
    public Icon getActiveIcon(MarkerViewOptions marker){
        Integer id = Integer.parseInt(marker.getSnippet());
        Place p = places.get(id);

        if (p.getType() == "database") {
            return databaseActiveIcon;
        } else if (p.getType() == "restaurant"){
            return restaurantsActiveIcon;
        } else if (p.getType() == "bar") {
            return barsActiveIcon;
        } else {
            //TODO
            return databaseActiveIcon;
        }
    }
*/

    public Marker getActiveMarker(){
        return activeMarker;
    }

    public Places getPlaces() {
        return placesByCategory;
    }

    public void deactivateActiveMarker() {
        if (activeMarker != null && activeMarkerNormalIcon != null) {
            activeMarker.setIcon(activeMarkerNormalIcon);
        }
        activeMarker = null;
        activeMarkerNormalIcon = null;
    }

    public void updateActiveMarker(Marker marker) {
        deactivateActiveMarker();
        if (marker == null) {
            return;
        }

        activeMarker = marker;
        activeMarkerNormalIcon = marker.getIcon();

        Integer id = Integer.parseInt(marker.getSnippet());
        Place p = places.get(id);

        Icon icon;

        if (p.getType().equals("database")) {
            icon = databaseActiveIcon;
        } else if (p.getType().equals("restaurant")){
            icon = restaurantsActiveIcon;
        } else if (p.getType().equals("bar")) {
            icon = barsActiveIcon;
        } else {
            //TODO
            icon = databaseIcon;
        }

        marker.setIcon(icon);
    }

    public List<MarkerViewOptions> getRestaurantMarkers() {
        return restaurantMarkers;
    }

    public List<MarkerViewOptions> getBarMarkers() {
        return barMarkers;
    }

    public List<MarkerViewOptions> getDbMarkers() {
        return dbMarkers;
    }

    public void loadDb(){
        DatabasePlacesHelper dbHelper = new DatabasePlacesHelper(getApplicationContext());
        Log.d("Debug","helper initialized");
        // Get the database. If it does not exist, this is where it will
        // also be created.
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Log.d("Debug","db opened");

        String[] projection = {
                DatabasePlacesContract.PlaceEntry._ID,
                DatabasePlacesContract.PlaceEntry.COLUMN_NAME_NAME,
                DatabasePlacesContract.PlaceEntry.COLUMN_NAME_TYPE,
                DatabasePlacesContract.PlaceEntry.COLUMN_NAME_LAT,
                DatabasePlacesContract.PlaceEntry.COLUMN_NAME_LNG,
                DatabasePlacesContract.PlaceEntry.COLUMN_NAME_DESCRIPTION
        };

        Cursor cursor = db.query(DatabasePlacesContract.PlaceEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        Log.d("Debug","projection created");

        ArrayList<Place> databases = new ArrayList<>();
        ArrayList<Place> restaurants = new ArrayList<>();
        ArrayList<Place> bars = new ArrayList<>();

        while(cursor.moveToNext()) {
            //tady si proste musim pamatovat, v jakym jsou poradi...
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String type = cursor.getString(2);
            double lat = cursor.getDouble(3);
            double lng = cursor.getDouble(4);
            String description = cursor.getString(5);

            Place p = new Place(id);
            p.setName(name);
            p.setType(type);
            p.setLocation(lat, lng);
            p.setDescription(description);


            if (p.getType().equals("database")) {
                databases.add(p);
            }

            if (p.getType().equals("restaurant")) {
                restaurants.add(p);
            }

            if (p.getType().equals("bar")) {
                bars.add(p);
            }

            places.put(p.getId(), p);
        }

        placesByCategory.setDatabases(databases);
        placesByCategory.setRestaurants(restaurants);
        placesByCategory.setBars(bars);

        Log.d("Debug","closing");
        cursor.close();
        dbHelper.close();
        Log.d("Debug","closed");
    }

    void initMarkers() {
        for (int i=0; i<places.size(); i++) {
            Place p = places.valueAt(i);
            MarkerViewOptions m = new MarkerViewOptions()
                    .position(p.getLocation())
                    .title(p.getName())
                    .snippet(Integer.toString(p.getId()))
                    .anchor(0.5f,1.0f);

            if (p.getType().equals("database")) {
                m.icon(databaseIcon);
                dbMarkers.add(m);
            }

            if (p.getType().equals("restaurant")) {
                m.icon(restaurantsIcon);
                restaurantMarkers.add(m);
            }

            if (p.getType().equals("bar")) {
                m.icon(barsIcon);
                barMarkers.add(m);
            }

            markers.put(p.getId(),m);
        }
        updateVisibleMarkers();
    }

}
