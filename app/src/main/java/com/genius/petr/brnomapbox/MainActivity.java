package com.genius.petr.brnomapbox;
import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.VectorSource;
import com.mapbox.services.commons.geojson.Point;
import com.mapbox.services.commons.utils.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap map;
    private FloatingActionButton floatingActionButton;
    private LocationServices locationServices;
    private boolean markersVisible;
    private ArrayList<MarkerViewOptions> markersRestaurant = new ArrayList<>();
    private ArrayList<MarkerViewOptions> markersBar = new ArrayList<>();
    private Marker activeMarker = null;
    private Icon activeMarkerNormalIcon; //just for convenience, not that anything of what I'm doing here was really convenient
    private Icon restaurantsIcon;
    private Icon restaurantsActiveIcon;
    private Icon barsIcon;
    private Icon barsActiveIcon;


    private CheckBox checkBoxFilters;
    boolean displayFilters;
    private CheckBox checkBoxRestaurants;
    boolean displayRestaurants;
    private CheckBox checkBoxPubs;
    boolean displayPubs;
    private LinearLayout filtersLayout;
    private LinearLayout filterButtonsLayout;
    private int filtersButtonsLayoutWidth = 60; //in dp // TODO: 15. 2. 2017 THIS SHOULD NOT BE HARDCODED

    //markerPopupStuff
    private LinearLayout markerPopupLayout;
    private TextView markerPopupName;
    private TextView markerPopupType;
    private TextView markerPopupOpen;
    private TextView markerPopupAddress;
    private TextView markerPopupWeb;



    private List<LatLng> circuit;
    private List<LatLng> circuitComplete;
    private List<LatLng> circuitRemaining;


    private static final int PERMISSIONS_LOCATION = 0;
    private static final int MARKERS_ZOOM_THRESHOLD = 16;


    private Pair<List<LatLng>,List<LatLng>> dividePath(LatLng position, List<LatLng> path) {
        if (path.size()<=1)
            return null;

        List<LatLng> complete = new ArrayList<>();
        List<LatLng> remaining = new ArrayList<>();
        int first=0; //index
        int second;
        double closestDistance = Double.MAX_VALUE;
        double distance;

        for (int i = 0; i<path.size(); i++) {
            distance = position.distanceTo(path.get(i));
            if (distance < closestDistance) {
                first = i;
                closestDistance = distance;
            }
        }

        if (first == 0) {
            second = 1;
            remaining.addAll(path);
        }
        else if (first == (path.size()-1)) {
            second = first;
            first--;
            complete.addAll(path);
        }
        else {
            double distanceToPrevious = position.distanceTo(path.get(first-1));
            double distanceToNext = position.distanceTo(path.get(first+1));
            if (distanceToPrevious < distanceToNext) {
                second = first;
                first--;
            }
            else {
                second = first+1;
            }


            LatLng firstPoint = path.get(first);
            LatLng secondPoint = path.get(second);
            double lineLength = firstPoint.distanceTo(secondPoint);

            complete.clear();
            if (lineLength == 0) {
                complete = path.subList(0,first);
                remaining = path.subList(second,path.size());
/*
                var t = ((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l2;
                t = Math.max(0, Math.min(1, t));
                return dist2(p, { x: v.x + t * (w.x - v.x),
                        y: v.y + t * (w.y - v.y) });
  */

            }
            else {
                double t = ( (position.getLongitude() - firstPoint.getLongitude())
                        * (secondPoint.getLongitude() - firstPoint.getLongitude())
                        + (position.getLatitude() - firstPoint.getLatitude())
                        * (secondPoint.getLatitude() - firstPoint.getLatitude()))
                        / lineLength;
                t = Math.max(0, Math.min(1,t));
                LatLng closestPoint = new LatLng(firstPoint.getLatitude() + t * (secondPoint.getLatitude()-firstPoint.getLatitude()),
                        firstPoint.getLongitude() + t * (secondPoint.getLongitude()-firstPoint.getLongitude()));

                complete = path.subList(0,first);
                complete.add(closestPoint);
                remaining.add(closestPoint);
                remaining.addAll(path.subList(second,path.size()));
            }
        }

        return new Pair<>(complete,remaining);

    }

    //TODO read type from JSON
    protected ArrayList<MarkerViewOptions> loadFeatures(String jsonResource, Icon icon, String type){
        ArrayList<MarkerViewOptions> markers = new ArrayList<>();
        try {
            InputStream is = getResources().openRawResource(
                    getResources().getIdentifier(jsonResource,
                            "raw", getPackageName()));


            ArrayList<Feature> features;
            GeojsonParser parser = new GeojsonParser();
            features = parser.getFeatures(is);


            //just test now...
            if (features != null && !features.isEmpty()) {
                for (Feature r : features) {
                       markers.add(new MarkerViewOptions()
                                .position(r.getLocation())
                                .title(r.getName())
                                .snippet(type)
                                .icon(icon)
                                .anchor(0.5f,1.0f));
                }
            }
        }
        catch (IOException e) {
            System.out.println("Unable to parse JSON");
        }
        finally{
            return markers;
        }


    }

    private void initIcons() {
        IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
        Drawable iconDrawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.restaurant);
        restaurantsIcon = iconFactory.fromDrawable(iconDrawable);
        iconDrawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.restaurant_active);
        restaurantsActiveIcon = iconFactory.fromDrawable(iconDrawable);

        iconDrawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.bar);
        barsIcon = iconFactory.fromDrawable(iconDrawable);
        iconDrawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.bar);
        barsActiveIcon = iconFactory.fromDrawable(iconDrawable);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create new helper
        DatabasePlacesHelper dbHelper = new DatabasePlacesHelper(this);
        // Get the database. If it does not exist, this is where it will
        // also be created.
       // SQLiteDatabase db = dbHelper.getWriteableDatabase();


        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        MapboxAccountManager.start(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the account manager
        setContentView(R.layout.activity_location_basic);

        locationServices = LocationServices.getLocationServices(MainActivity.this);


        markerPopupLayout = (LinearLayout) findViewById(R.id.markerPopupLayout);
        markerPopupName = (TextView) findViewById(R.id.markerPopupName);
        markerPopupType = (TextView) findViewById(R.id.markerPopupType);
        markerPopupOpen = (TextView) findViewById(R.id.markerPopupOpen);
        markerPopupAddress = (TextView) findViewById(R.id.markerPopupAddress);
        markerPopupWeb = (TextView) findViewById(R.id.markerPopupWeb);

        /* Doesnt work TODO make it work
        markerPopupLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                for (int i = 0; i<50; i++)
                    System.out.println("HERE");
                if (!hasFocus)
                    markerPopupLayout.setVisibility(View.INVISIBLE);
            }
        });
        */

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.setStyleUrl("mapbox://styles/ptrvck/ciz5ld1mw00c72sphhc35amyt");



                mapboxMap.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition position) {

                        if(position.zoom < MARKERS_ZOOM_THRESHOLD && markersVisible){
                            toggleMarkers();
                        }
                        if(position.zoom >= MARKERS_ZOOM_THRESHOLD && !markersVisible){
                            toggleMarkers();
                        }
                    }
                });

                mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {
                        markerPopupLayout.setVisibility(View.INVISIBLE);
                        updateActiveMarker(null);
                    }
                });

                

                mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    //TODO create database, set marker title as database ID and get all data from there
                    @Override
                    public boolean onMarkerClick(final Marker marker) {
                        markerPopupName.setText(marker.getTitle());
                        markerPopupLayout.setVisibility(View.VISIBLE);
                        markerPopupLayout.requestFocus();
                        updateActiveMarker(marker);
                        return true;
                    }
                });

                map = mapboxMap;



                initIcons();
                markersRestaurant =  loadFeatures("restaurants", restaurantsIcon, "Restaurant");
                markersBar = loadFeatures("bars", barsIcon, "Bar");


                filtersLayout = (LinearLayout) findViewById(R.id.filtersLayout);
                filterButtonsLayout = (LinearLayout) findViewById(R.id.filterButtonsLayout);

                Resources r = getResources();
                //convert from dp to px for animation purposes
                filtersButtonsLayoutWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, filtersButtonsLayoutWidth, r.getDisplayMetrics());


                checkBoxFilters = (CheckBox) findViewById(R.id.checkBoxFilters);
                displayFilters = checkBoxFilters.isChecked();

                if (!displayFilters) {
                    filterButtonsLayout.setVisibility(View.GONE);
                }

                checkBoxFilters.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        toggleFilters();
                    }
                });


                checkBoxRestaurants = (CheckBox) findViewById(R.id.checkBoxRestaurants);
                displayRestaurants = checkBoxRestaurants.isChecked();
                checkBoxPubs = (CheckBox) findViewById(R.id.checkBoxPubs);
                displayPubs = checkBoxPubs.isChecked();

                checkBoxRestaurants.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        displayRestaurants = checkBoxRestaurants.isChecked();
                        updateMarkers();
                    }
                });
                checkBoxPubs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        displayPubs = checkBoxPubs.isChecked();
                        updateMarkers();
                    }
                });

                markersVisible = (mapboxMap.getCameraPosition().zoom >= MARKERS_ZOOM_THRESHOLD);

                if (markersVisible) {
                    updateMarkers();
                }


                /*
                VectorSource circuitSource = new VectorSource("circuitSource", "mapbox://ptrvck.ciznv22tk00152wppyv0osiqn-1jb52");
                map.addSource(circuitSource);

                LineLayer circuitLayer = new LineLayer("circuit", "circuitSource");
                circuitLayer.setSourceLayer("");
                circuitLayer.setProperties(visibility(VISIBLE),
                        lineWidth(5f),
                        lineColor(Color.argb(1, 55, 148, 179)));

                map.addLayer(circuitLayer);

                //LineLayer circuit = new LineLayer("circuitSource", "mapbox://ptrvck.ciznv22tk00152wppyv0osiqn-1jb52");
*/
                new DrawGeoJson().execute();

            }
        });


        floatingActionButton = (FloatingActionButton) findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (map != null) {
                    toggleGps(!map.isMyLocationEnabled());
                }
            }
        });

    }

    private void updateActiveMarker(Marker marker) {

        if (activeMarker != null) {
            activeMarker.setIcon(activeMarkerNormalIcon);
        }

        activeMarker = marker;

        if (marker == null)
            return;

        //switch on type, because i can't really extend Marker, because idiotic reason
        String type = marker.getSnippet();
        if (type.equals("Restaurant")){
            activeMarkerNormalIcon = restaurantsIcon;
            marker.setIcon(restaurantsActiveIcon);
        } else if (type.equals("Bar")) {
            activeMarkerNormalIcon = barsIcon;
            marker.setIcon(barsActiveIcon);
        }


    }

    private void toggleFilters(){
        displayFilters = !displayFilters;
        if (displayFilters) {
            filtersLayout.setTranslationX(filtersButtonsLayoutWidth);
            filtersLayout.animate()
                    .translationXBy(-filtersButtonsLayoutWidth)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationEnd(animation);
                            filterButtonsLayout.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            //filtersLayout.setTranslationX();
            filtersLayout.animate()
                    .translationXBy(filtersButtonsLayoutWidth)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            filterButtonsLayout.setVisibility(View.INVISIBLE);
                        }
                    });
        }
    }

    private void toggleMarkers(){
        markersVisible = !markersVisible;
        updateMarkers();
    }

    private void updateMarkers() {
        //this is stupid workaround
        //TODO find better solution
        markerPopupLayout.setVisibility(View.INVISIBLE);
        if (activeMarker!=null) {
            updateActiveMarker(null);
        }

        if (markersVisible) {
            map.clear();
            drawCircuit();


            if (displayRestaurants) {
                map.addMarkerViews(markersRestaurant);
            }

            if (displayPubs) {
                map.addMarkerViews(markersBar);
            }
        }
        else {
            map.clear();
            drawCircuit();
        }
    }

    private void drawCircuit() {
        map.addMarker(new MarkerViewOptions()
                .position(new LatLng(49.1972,16.6038))
            .title("AA")
        );

        if (circuit == null || circuit.size() == 0)
            return;
        if (circuitComplete != null && circuitRemaining != null) {
            map.addPolyline(new PolylineOptions()
                    .addAll(circuitComplete)
                    .color(Color.parseColor("#ff0000"))
                    .width(5));
            map.addPolyline(new PolylineOptions()
                    .addAll(circuitRemaining)
                    .color(Color.parseColor("#3bb2d0"))
                    .width(5));
        } else {
            map.addPolyline(new PolylineOptions()
                    .addAll(circuit)
                    .color(Color.parseColor("#3bb2d0"))
                    .width(5));
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            if (!locationServices.areLocationPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
            } else {
                enableLocation(true);
            }
        } else {
            enableLocation(false);
        }
    }

    private void enableLocation(boolean enabled) {
        if (enabled) {
            // If we have the last location of the user, we can move the camera to that position.
            Location lastLocation = locationServices.getLastLocation();
            if (lastLocation != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16));
            }

            locationServices.addLocationListener(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        // Move the map camera to where the user location is and then remove the
                        // listener so the camera isn't constantly updating when the user location
                        // changes. When the user disables and then enables the location again, this
                        // listener is registered again and will adjust the camera once again.
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
                        locationServices.removeLocationListener(this);
                    }
                }
            });
            floatingActionButton.setImageResource(R.drawable.ic_location_disabled_24dp);
        } else {
            floatingActionButton.setImageResource(R.drawable.ic_my_location_24dp);
        }
        // Enable or disable the location layer on the map
        map.setMyLocationEnabled(enabled);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocation(true);
            }
        }
    }

    private class DrawGeoJson extends AsyncTask<Void, Void, List<LatLng>> {
        @Override
        protected List<LatLng> doInBackground(Void... voids) {

            ArrayList<LatLng> points = new ArrayList<>();

            try {
                // Load GeoJSON file
                InputStream inputStream = getAssets().open("shortRoute.geojson");
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
                StringBuilder sb = new StringBuilder();
                int cp;
                while ((cp = rd.read()) != -1) {
                    sb.append((char) cp);
                }

                inputStream.close();

                // Parse JSON
                JSONObject json = new JSONObject(sb.toString());
                JSONArray features = json.getJSONArray("features");
                JSONObject feature = features.getJSONObject(0);
                JSONObject geometry = feature.getJSONObject("geometry");
                if (geometry != null) {
                    String type = geometry.getString("type");

                    // Our GeoJSON only has one feature: a line string
                    if (!TextUtils.isEmpty(type) && type.equalsIgnoreCase("LineString")) {

                        // Get the Coordinates
                        JSONArray coords = geometry.getJSONArray("coordinates");
                        for (int lc = 0; lc < coords.length(); lc++) {
                            JSONArray coord = coords.getJSONArray(lc);
                            LatLng latLng = new LatLng(coord.getDouble(1), coord.getDouble(0));
                            points.add(latLng);
                        }
                    }
                }
            } catch (Exception exception) {
               // Log.e(TAG, "Exception Loading GeoJSON: " + exception.toString());
            }

            return points;
        }

        @Override
        protected void onPostExecute(List<LatLng> points) {
            super.onPostExecute(points);

            /*
        16.603716,
        49.194988
     */
            //this should probably be async (TODO)
            Pair<List<LatLng>,List<LatLng>> divided = dividePath(new LatLng(49.1972,16.6038), points);


            circuitComplete = divided.first;
            circuitRemaining = divided.second;

            circuit = points;
            drawCircuit();
        }
    }
}