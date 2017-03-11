package com.genius.petr.brnomapbox;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private SparseArray<Place> places;
    private SparseArray<MarkerViewOptions> placesMarkers;
    private List<MarkerViewOptions> dbMarkers;
    private List<MarkerViewOptions> restaurantMarkers;
    private List<MarkerViewOptions> barMarkers;

    private List<Place> databases = new ArrayList<>();
    private List<Place> restaurants = new ArrayList<>();
    private List<Place> bars = new ArrayList<>();


    public PlacesManager(Context context){
        super(context);
        places = new SparseArray<>();
        placesMarkers = new SparseArray<>();
        loadDb();
    }

    private void initIcons() {
        IconFactory iconFactory = IconFactory.getInstance(getApplicationContext());
        Drawable iconDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.restaurant);
        restaurantsIcon = iconFactory.fromDrawable(iconDrawable);
        iconDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.restaurant_active);
        restaurantsActiveIcon = iconFactory.fromDrawable(iconDrawable);

        iconDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.bar);
        barsIcon = iconFactory.fromDrawable(iconDrawable);
        iconDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.bar);
        barsActiveIcon = iconFactory.fromDrawable(iconDrawable);

        iconDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.database);
        databaseIcon = iconFactory.fromDrawable(iconDrawable);
        iconDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.database);
        databaseActiveIcon = iconFactory.fromDrawable(iconDrawable);
    }


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

    /*
    public ArrayList<MarkerViewOptions> getMarkers() {
        ArrayList<MarkerViewOptions> markers = new ArrayList<>();
        for (int i=0; i<places.size(); i++) {
            Place p = places.valueAt(i);

            MarkerViewOptions m = new MarkerViewOptions()
                    .position(p.getLocation())
                    .title(p.getName())
                    .snippet(Integer.toString(p.getId()))
                    .icon(databaseIcon)
                    .anchor(0.5f,1.0f);
            placesMarkers.put(p.getId(),m);
            markers.add(m);
        }
        return markers;
    }
    */

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
        // Get the database. If it does not exist, this is where it will
        // also be created.
        SQLiteDatabase db = dbHelper.getReadableDatabase();

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



        List itemIds = new ArrayList<>();
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
        cursor.close();
        dbHelper.close();
    }


    void initMarkers() {
        for (int i=0; i<places.size(); i++) {
            Place p = places.valueAt(i);
            Icon icon;
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

            placesMarkers.put(p.getId(),m);
        }
    }

}
