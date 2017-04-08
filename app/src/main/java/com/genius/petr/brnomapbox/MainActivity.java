package com.genius.petr.brnomapbox;
import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.genius.petr.brnomapbox.dickin.DickinGameActivity;
import com.genius.petr.brnomapbox.horsin.HorsinGameActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.services.Constants;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.geojson.Point;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.TextUtils;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;
import com.mapbox.services.directions.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private PlacesManager placesManager;
    private static boolean mySQLInit = true;

    private MapView mapView;
    private MapboxMap map;
    private FloatingActionButton floatingActionButton;
    private LocationServices locationServices;
    private boolean markersVisible;
    private ArrayList<MarkerViewOptions> markersRestaurant = new ArrayList<>();
    private ArrayList<MarkerViewOptions> markersBar = new ArrayList<>();
    private ArrayList<MarkerViewOptions> markers = new ArrayList<>();
    private HashMap<Integer, MarkerViewOptions> markersMap = new HashMap<>();

    private Circuit circuit;

    private DirectionsRoute currentRoute;


    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MARKERS = "markers";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_LAT = "lat";
    private static final String TAG_LNG = "lng";
    private static final String TAG_TYPE = "type";
    private static final String TAG_DESCRIPTION = "description";

    private double lastZoom = 0;

    private SymbolLayer bars;

    private CheckBox checkBoxFilters;
    boolean displayFilters;
    private CheckBox checkBoxRestaurants;
    private CheckBox checkBoxPubs;
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

    private ImageButton navigationButton;

    private static Context contextOfApplication;


    //private List<LatLng> circuit;
    //private List<LatLng> circuitComplete;
    //private List<LatLng> circuitRemaining;

    private static final int PERMISSIONS_LOCATION = 0;
    private static final int MARKERS_ZOOM_THRESHOLD = 16;

    private Timer clusterTimer = new Timer();


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

    /*
    protected ArrayList<MarkerViewOptions> loadMarkers(){
        // Create new helper
        DatabasePlacesHelper dbHelper = new DatabasePlacesHelper(this);
        // Get the database. If it does not exist, this is where it will
        // also be created.
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ArrayList<MarkerViewOptions> markers = new ArrayList<>();
        Log.d("Debug", "Loading markers");
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
            Log.d("Debug","Reading");
            //long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabasePlacesContract.PlaceEntry._ID));
            //tady si proste musim pamatovat, v jakym jsou poradi...
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String type = cursor.getString(2);
            double lat = cursor.getDouble(3);
            double lng = cursor.getDouble(4);
            String description = cursor.getString(5);

            MarkerViewOptions m = new MarkerViewOptions()
                    .position(new LatLng(lat,lng))
                    .title(name)
                    .snippet(type)
                    .icon(databaseIcon)
                    .anchor(0.5f,1.0f);

            markers.add(m);
        }
        cursor.close();
        dbHelper.close();
        return markers;
    }
    */
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


    private void initMarkers(){
        Log.d("BrnoMarkersInit", "INSIDE");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(contextOfApplication);
        String lastUpdate = sharedPref.getString(getString(R.string.last_update_tag), "0");

        final List<Place> places = new ArrayList<Place>();
        AsyncHttpClient client = new AsyncHttpClient();
        String url_all_markers = getString(R.string.server_ip) + getString(R.string.script_url);

        RequestParams params = new RequestParams();
        params.put("lastupdate", Integer.parseInt(lastUpdate));
        Log.d("BrnoMarkersInit", "lastupdate: " + Integer.parseInt(lastUpdate));
        //params.put("lastupdate", 156258);

        client.get(url_all_markers, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    Log.d("BrnoMarkersInit", "Success");
                    Log.d("BrnoMarkersInit", response.toString());
                    String str = new String(response, "UTF-8");
                    Log.d("BrnoMarkersInit", str);
                    // Create JSON object out of the response sent by getdbrowcount.php
                    JSONObject json = new JSONObject(str);
                    Log.d("BrnoMarkersInit", json.toString());


                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        // products found
                        // Getting Array of Products
                        JSONArray markersJSON = json.getJSONArray(TAG_MARKERS);

                        Long timeStampLong = System.currentTimeMillis()/1000;
                        String timeStamp = timeStampLong.toString();
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(contextOfApplication);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.last_update_tag), timeStamp);
                        editor.commit();

                        // looping through All Products
                        for (int i = 0; i < markersJSON.length(); i++) {
                            JSONObject c = markersJSON.getJSONObject(i);

                            // Storing each json item in variable
                            int id = c.getInt(TAG_ID);
                            String name = c.getString(TAG_NAME);
                            String type = c.getString(TAG_TYPE);
                            double lat = c.getDouble(TAG_LAT);
                            double lng = c.getDouble(TAG_LNG);
                            String description = c.getString(TAG_DESCRIPTION);

                            Place place = new Place(id);
                            place.setName(name);
                            place.setType(type);
                            place.setLocation(lat, lng);
                            place.setDescription(description);
                            places.add(place);
                        }
                            DatabasePlacesHelper dbHelper = new DatabasePlacesHelper(contextOfApplication);
                            // Get the database. If it does not exist, this is where it will
                            // also be created.
                            for (Place p : places){
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(DatabasePlacesContract.PlaceEntry.COLUMN_NAME_NAME, p.getName());
                                values.put(DatabasePlacesContract.PlaceEntry.COLUMN_NAME_TYPE, p.getType());
                                values.put(DatabasePlacesContract.PlaceEntry.COLUMN_NAME_LAT, p.getLocation().getLatitude());
                                values.put(DatabasePlacesContract.PlaceEntry.COLUMN_NAME_LNG, p.getLocation().getLongitude());
                                values.put(DatabasePlacesContract.PlaceEntry.COLUMN_NAME_DESCRIPTION, p.getDescription());
                                long newRowId = db.insert(DatabasePlacesContract.PlaceEntry.TABLE_NAME, null, values);
                            }

                            dbHelper.close();
                           // markers = loadMarkers();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    placesManager = new PlacesManager(contextOfApplication);
                                    placesManager.cluster(map.getProjection());
                                    updateMarkers();
                                }
                            });

                    } else {
                        Log.d("BrnoMarkersInit", "MySQL success = 0");
                    }

                } catch (JSONException | UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    Log.d("BrnoMarkersInit", "EXCEPTION");
                    Log.d("BrnoMarkersInit", e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse , Throwable error) {
                // TODO Auto-generated method stub
                Log.d("BrnoMarkersInit", "FAIL");
                /*
                try {
                    Log.e("Connection", new String(errorResponse, "UTF-8"));
                } catch (UnsupportedEncodingException e) {

                }*/
            }
        });
    }

    void findViewsById() {
        markerPopupLayout = (LinearLayout) findViewById(R.id.markerPopupLayout);
        markerPopupName = (TextView) findViewById(R.id.markerPopupName);
        markerPopupType = (TextView) findViewById(R.id.markerPopupType);
        markerPopupOpen = (TextView) findViewById(R.id.markerPopupOpen);
        markerPopupAddress = (TextView) findViewById(R.id.markerPopupAddress);
        //markerPopupWeb = (TextView) findViewById(R.id.markerPopupWeb);
        mapView = (MapView) findViewById(R.id.mapView);
        filtersLayout = (LinearLayout) findViewById(R.id.filtersLayout);
        filterButtonsLayout = (LinearLayout) findViewById(R.id.filterButtonsLayout);
        checkBoxFilters = (CheckBox) findViewById(R.id.checkBoxFilters);
        checkBoxRestaurants = (CheckBox) findViewById(R.id.checkBoxRestaurants);
        checkBoxPubs = (CheckBox) findViewById(R.id.checkBoxPubs);

        navigationButton = (ImageButton) findViewById(R.id.navigationButton);
        navigationButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.i("Navigation", "clicked");

                //when the button is visible it should never be null, but...
                Marker activeMarker = placesManager.getActiveMarker();
                if (activeMarker == null) {
                    Log.i("Navigation", "active null");
                    return;
                }

                Location user = map.getMyLocation();

                if (user == null) {
                    Log.i("Navigation", "user null");
                    return;
                    //TODO: display message that location is unknown
                }

                Position current = Position.fromCoordinates(user.getLongitude(), user.getLatitude());

                Position target = Position.fromCoordinates(activeMarker.getPosition().getLongitude(), activeMarker.getPosition().getLatitude());


                // Get route from API
                try {
                    getRoute(current, target);
                } catch (ServicesException servicesException) {
                    Log.i("Navigation", "service exception");
                    servicesException.printStackTrace();
                }
            }
        });
    }

    private void showMarkerPopup(Place place) {
        markerPopupName.setText(place.getName());
        markerPopupType.setText(place.getType());
        //TODO
        //markerPopupOpen.setText(place.getOpenToday());
        //markerPopupAddress.setText(place.getAddress());
        //markerPopupWeb.setText(place.getWeb());
        markerPopupLayout.setVisibility(View.VISIBLE);
        markerPopupLayout.requestFocus();
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        View v = menu.findItem(R.id.action_search).getActionView();

        AutoCompleteTextView searchView = ( AutoCompleteTextView ) v.findViewById(R.id.search_view);
        searchView.setThreshold(1);
        Log.i("Search", searchView.toString());

        List<Place> places = placesManager.getPlaces().getAllPlaces();

        PlaceAutocompleteAdapter adapter = new PlaceAutocompleteAdapter(this, R.layout.search_list_item, places);
        searchView.setAdapter(adapter);
        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
                Object item = parent.getItemAtPosition(position);
                Log.i("Search", Integer.toString(position));
                InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
                if (item instanceof Place){
                    Place p = (Place) item;
                    showMarkerPopup(p);
                }
            }
        });

        /*
        List<String> names = placesManager.getPlaces().getPlacesNames();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.search_list_item, names);
        searchView.setAdapter(adapter);
        */
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_categories:
                Intent intent = new Intent(this, PlacesActivity.class);
                intent.putExtra("places", placesManager.getPlaces());
                startActivity(intent);

                return true;
            case R.id.action_game:
                startActivity(new Intent(this, DickinGameActivity.class));
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }



    //this is called when i get back from browsing markers
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        int id = intent.getExtras().getInt("id");
        MarkerViewOptions marker = placesManager.getMarker(id);
        placesManager.updateActiveMarker(marker.getMarker());
        showMarkerPopup(placesManager.getPlace(id));



        map.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(marker.getPosition())  // set the camera's center position
                        .zoom(14)  // set the camera's zoom level
                        .build()));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // This contains the MapView in XML and needs to be called after the account manager
        setContentView(R.layout.activity_location_basic);

        contextOfApplication = getApplicationContext();
        placesManager = new PlacesManager(contextOfApplication);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        MapboxAccountManager.start(this, getString(R.string.access_token));



        locationServices = LocationServices.getLocationServices(MainActivity.this);

        this.findViewsById();

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


        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {
                mapboxMap.setStyleUrl("mapbox://styles/ptrvck/cj0zdc5wt001t2ro9vtelxd0m");
                mapboxMap.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition position) {
                        Log.i("MarkerPosition", "zoom: " + position.zoom);

                        if(position.zoom < MARKERS_ZOOM_THRESHOLD && markersVisible){
                            toggleMarkers();
                        }
                        if(position.zoom >= MARKERS_ZOOM_THRESHOLD && !markersVisible){
                            toggleMarkers();
                        }

                        /*this is called all the time...
                        first of all ensure it updates clusters only when zoom changes
                         */

                        if (position.zoom != lastZoom) {
                            lastZoom = position.zoom;

                            //we want to update clusters only when zooming finishes
                            clusterTimer.cancel();
                            clusterTimer = new Timer();
                            clusterTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    placesManager.cluster(map.getProjection());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateMarkers();
                                        }
                                    });

                                }
                            }, 500);


                        }

                    }
                });



                mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {
                        markerPopupLayout.setVisibility(View.INVISIBLE);
                        //updateActiveMarker(null);
                        placesManager.deactivateActiveMarker();
                    }
                });

                mapboxMap.setOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(@NonNull LatLng point) {
                        circuit.update(point);
                        //updateMarkers();
                    }
                });


                mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    //TODO create database, set marker title as database ID and get all data from there
                    @Override
                    public boolean onMarkerClick(final Marker marker) {
                        int id = Integer.parseInt(marker.getSnippet());

                        Log.i("Cluster", Integer.toString(id));
                        //cluster
                        if (id == MyConstants.CLUSTER) {
                            Place p = new Place(-1);



                            p.setType("TODO");
                            p.setName("Cluster");
                            showMarkerPopup(p);
                            return true;
                        }

                        Place p = placesManager.getPlace(id);

                        //markerPopupName.setText(marker.getTitle());
                        showMarkerPopup(p);
                        placesManager.updateActiveMarker(marker);
                        //updateActiveMarker(marker);
                        return true;
                    }
                });

                map = mapboxMap;
                placesManager.cluster(map.getProjection());


                //initIcons();
                Log.d("BrnoMarkersInit", "INIT INIT INIT");
                if (mySQLInit) {
                    initMarkers();
                    mySQLInit = false;
                }

                //convert from dp to px for animation purposes
                Resources r = getResources();
                filtersButtonsLayoutWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, filtersButtonsLayoutWidth, r.getDisplayMetrics());

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

                checkBoxRestaurants.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        placesManager.toggleRestaurants();
                        updateMarkers();
                    }
                });
                checkBoxPubs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        placesManager.toggleBars();
                        updateMarkers();
                    }
                });

                /*
                markersVisible = (mapboxMap.getCameraPosition().zoom >= MARKERS_ZOOM_THRESHOLD);

                if (markersVisible) {
                    updateMarkers();
                }
                */



                new DrawGeoJson().execute();
                updateMarkers();
            }
        });

        floatingActionButton = (FloatingActionButton) findViewById(R.id.location_toggle_fab);
        //testing button
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (map != null) {
                    toggleGps(!map.isMyLocationEnabled());
                    //printMarkers();
                    //placesManager.cluster(map.getProjection());
                   // updateMarkers();
                }
            }
        });
    }

    private void printMarkers(){
        Projection projection = map.getProjection();
        for (MarkerViewOptions m : placesManager.getMarkers()) {
            PointF screenLocation = projection.toScreenLocation(m.getPosition());
            Log.i("MarkerPosition", "x: " + Float.toString(screenLocation.x) + "y: " + Float.toString(screenLocation.y));
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
        map.clear();
        drawCircuit();
/*
        markerPopupLayout.setVisibility(View.INVISIBLE);
        map.addMarkerViews(placesManager.getMarkers());
  */





        /*
        //if (markersVisible) {
        if (true) {
            map.clear();

            drawCircuit();
            map.addMarkerViews(placesManager.getDbMarkers());


            if (displayRestaurants) {
                map.addMarkerViews(placesManager.getRestaurantMarkers());
            }

            if (displayPubs) {
                map.addMarkerViews(placesManager.getBarMarkers());
            }
        }
        else {
            map.clear();
            drawCircuit();
            map.addMarkerViews(placesManager.getDbMarkers());
        }
        */
    }

    private void getRoute(Position origin, Position destination) throws ServicesException {

        MapboxDirections client = new MapboxDirections.Builder()
                .setOrigin(origin)
                .setDestination(destination)
                .setProfile(DirectionsCriteria.PROFILE_WALKING)
                .setAccessToken(MapboxAccountManager.getInstance().getAccessToken())
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                Log.d("Navigation", "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e("Navigation", "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().getRoutes().size() < 1) {
                    Log.e("Navigation", "No routes found");
                    return;
                }

                // Print some info about the route
                currentRoute = response.body().getRoutes().get(0);
                Log.d("Navigation", "Distance: " + currentRoute.getDistance());
                Toast.makeText(
                        MainActivity.this,
                        "Route is " + currentRoute.getDistance() + " meters long.",
                        Toast.LENGTH_SHORT).show();

                // Draw the route on the map
                drawRoute(currentRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Log.e("Navigation", "Error: " + throwable.getMessage());
                Toast.makeText(MainActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoute(DirectionsRoute route) {
        // Convert LineString coordinates into LatLng[]
        LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.OSRM_PRECISION_V5);
        List<Position> coordinates = lineString.getCoordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
        }

        // Draw Points on MapView
        map.addPolyline(new PolylineOptions()
                .add(points)
                .color(Color.parseColor("#009688"))
                .width(5));
    }

    private void drawCircuit() {
        if(circuit == null) {
            return;
        }

        map.addMarkerViews(circuit.getMarkers());

        map.addPolyline(new PolylineOptions()
                .addAll(circuit.getPath())
                .color(Color.parseColor("#ff0000"))
                .width(5));

        /*
        PolylineOptions p = new PolylineOptions();
        Polyline line = p.getPolyline();
*/

        /*
        for (LatLng position : circuit.getPath()) {

            map.addMarker(new MarkerViewOptions()
                    .position(position)
                    .title("AA")
            );
        }
        */

        List<PathSegment> visited = circuit.getVisitedSegments();
        Log.i("Circuit","segments: "+circuit.getVisitedSegments().size());

        for (PathSegment segment : visited) {
            map.addPolyline(new PolylineOptions()
                    .addAll(segment.getPath())
                    .color(Color.parseColor("#00FFFF"))
                    .width(5));
        }

        /*
        map.addMarker(new MarkerViewOptions()
                .position(new LatLng(49.1972,16.6038))
            .title("AA")
        );

        if (circuit == null || circuit.size() == 0) {
            Log.d("Debug","circuit null");
            return;
        }
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
        */

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
            List<Place> stops = new ArrayList<>();
            Place p = new Place(924);
            p.setName("Turn 01");
            p.setType("Circuit stop");
            p.setLocation(points.get(8));
            stops.add(p);

            p = new Place(925);
            p.setName("Turn 02");
            p.setType("Circuit stop");
            p.setLocation(points.get(9));
            stops.add(p);

            p = new Place(926);
            p.setName("Turn 03");
            p.setType("Circuit stop");
            p.setLocation(points.get(14));
            stops.add(p);


            circuit = new Circuit(points, stops, contextOfApplication);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateMarkers();
                }
            });

            /*
        16.603716,
        49.194988
     */
            //this should probably be async (TODO)
            /*
            Pair<List<LatLng>,List<LatLng>> divided = dividePath(new LatLng(49.1972,16.6038), points);


            circuitComplete = divided.first;
            circuitRemaining = divided.second;

            circuit = points;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateMarkers();
                }
            });
            */
        }
    }

    class LoadMarkersMySQL extends AsyncTask<String, Void, List<Place>> {



        /**
         * getting All products from url
         * */
        protected List<Place> doInBackground(String... args) {
            final List<Place> places = new ArrayList<Place>();
            AsyncHttpClient client = new AsyncHttpClient();
            String url_all_markers = getString(R.string.server_ip) + getString(R.string.script_url);
            client.get(url_all_markers, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                    try {
                        String str = new String(response, "UTF-8");
                        // Create JSON object out of the response sent by getdbrowcount.php
                        JSONObject json = new JSONObject(str);
                        Log.d("Debug", "Connected");
                        if (json == null){
                            Log.d("Debug", "Screwed");
                        }else {
                            // Check your log cat for JSON reponse
                            Log.d("Debug", json.toString());
                        }

                        int success = json.getInt(TAG_SUCCESS);

                        if (success == 1) {
                            // products found
                            // Getting Array of Products
                            JSONArray markersJSON = json.getJSONArray(TAG_MARKERS);


                            // looping through All Products
                            for (int i = 0; i < markersJSON.length(); i++) {
                                JSONObject c = markersJSON.getJSONObject(i);

                                // Storing each json item in variable
                                int id = c.getInt(TAG_ID);
                                String name = c.getString(TAG_NAME);
                                double lat = c.getDouble(TAG_LAT);
                                double lng = c.getDouble(TAG_LNG);

                                Place place = new Place(id);
                                place.setName(name);
                                place.setLocation(lat,lng);
                                places.add(place);
                            }
                        } else {
                            // no products found
                            // Launch Add New product Activity
                        /*
                        Intent i = new Intent(getApplicationContext(),
                                NewProductActivity.class);
                        // Closing all previous activities
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        */
                        }

                    } catch (JSONException | UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse , Throwable error) {
                    // TODO Auto-generated method stub
                    try {
                        Log.e("Connection", new String(errorResponse, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {

                    }
                }
            });

            return places;
        }

        /*
        protected void onPostExecute(List<Place> places) {
            // dismiss the dialog after getting all products
            // pDialog.dismiss();
            // updating UI from Background Thread
            Log.d("Debug", "On post execute");
            markers.clear();
            for (Place p : places){

                    markers.add(new MarkerViewOptions()
                            .position(p.getLocation())
                            .title(p.getName())
                            .snippet("database")
                            .icon(databaseIcon)
                            .anchor(0.5f,1.0f));
            }

            runOnUiThread(new Runnable() {
                public void run() {
                    updateMarkers();
                }
            });

        }
        */
    }
}