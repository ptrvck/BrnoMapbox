package com.genius.petr.brnomapbox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.util.JsonToken;

import com.google.gson.stream.JsonReader;


/**
 * Created by Petr on 11. 2. 2017.
 */

public class GeojsonParser {

    private static final String TAG_FEATURES = "features";
    private static final String TAG_TYPE = "type";
    private static final String TAG_ID = "id";
    private static final String TAG_PROPERTIES = "properties";
    private static final String TAG_AMENITY = "amenity";
    private static final String TAG_NAME = "name";
    private static final String TAG_GEOMETRY = "geometry";
    private static final String TAG_COORDINATES = "coordinates";

    public ArrayList<Feature> getFeatures(String json) {

        if (json != null) {
            try {
                ArrayList<Feature> features = new ArrayList<>();
                JSONObject data = new JSONObject(json);

                // Getting JSON Array node
                JSONArray places = data.getJSONArray(TAG_FEATURES);

                // looping through all places
                for (int i = 0; i < places.length(); i++) {
                    JSONObject current = places.getJSONObject(i);
                    //in case some vital tag is missing, it will just skip that place and continue with others
                    try {
                        String id = current.getString(TAG_ID);
                        JSONObject properties = current.getJSONObject(TAG_PROPERTIES);
                        String name = properties.getString(TAG_NAME);
                        JSONObject geometry = current.getJSONObject(TAG_GEOMETRY);
                        JSONArray coordinates = geometry.getJSONArray(TAG_COORDINATES);
                        String lat = coordinates.getString(1);
                        String lng = coordinates.getString(0);

                        Feature feature = new Feature(id);
                        feature.setName(name);

                        try {
                            feature.setLocation(Double.parseDouble(lat), Double.parseDouble(lng));
                        }
                        catch (NumberFormatException e) {
                            System.err.println("invalid LatLng");
                        }

                        if (feature.valid())
                            features.add(feature);
                        }
                    catch (JSONException e) {
                        System.out.println("JSON exception");
                        e.printStackTrace();
                        return null;
                    }
                }
                return features;
            } catch (JSONException e) {
                for (int i=0; i<20; i++)
                    System.out.println("JSON exception");
                e.printStackTrace();
                return null;
            }
        } else {
            //Log.e("ServiceHandler", "No data received from HTTP request");
            for (int i=0; i<20; i++)
                System.out.println("NULL");
            return null;
        }
    }

    public ArrayList<Feature> getFeatures(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readFeatures(reader);
        } finally {
            reader.close();
        }
    }

    public ArrayList<Feature> readFeatures(JsonReader reader) throws IOException {

        //first get to array
        reader.beginObject();

        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals(TAG_FEATURES)) {
                return readFeaturesArray(reader);
            } else {
                reader.skipValue();
            }
        }
        return null;
    }

    public ArrayList<Feature> readFeaturesArray(JsonReader reader) throws IOException {
        ArrayList<Feature> features = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            Feature r = readFeature(reader);
            if (r!= null && r.valid())
                features.add(r);
        }
        reader.endArray();
        return features;
    }

    public Feature readFeature(JsonReader reader) throws IOException {
        String id = null;
        String featureName = null;
        double lat = 0;
        double lng = 0;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals(TAG_ID)) {
                id = reader.nextString();
                //handle properties here, im sick of all those nested methods
            } else if (name.equals(TAG_PROPERTIES)) {
                reader.beginObject();
                while (reader.hasNext()) {
                    name = reader.nextName();
                    if (name.equals(TAG_NAME)&& (!reader.peek().equals(JsonToken.NULL))) {
                        featureName = reader.nextString();
                    }
                    else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
            } else if (name.equals(TAG_GEOMETRY) && (!reader.peek().equals(JsonToken.NULL))) {
                reader.beginObject();
                while (reader.hasNext()) {
                    name = reader.nextName();
                    if (name.equals(TAG_COORDINATES)&& (!reader.peek().equals(JsonToken.NULL))) {
                        reader.beginArray();
                        ArrayList<Double> doubles = new ArrayList<Double>();
                        while (reader.hasNext()) {
                            doubles.add(reader.nextDouble());
                        }
                        lat = doubles.get(1);
                        lng = doubles.get(0);
                        reader.endArray();
                    }
                    else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        Feature r = new Feature(id);
        r.setLocation(lat, lng);
        r.setName(featureName);
        return r;
    }
}
