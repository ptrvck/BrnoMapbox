package com.genius.petr.brnomapbox;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petr on 11. 3. 2017.
 */

public class Places implements Parcelable {
    private ArrayList<Place> databases = new ArrayList<>();
    private ArrayList<Place> restaurants = new ArrayList<>();
    private ArrayList<Place> bars = new ArrayList<>();

    public Places(){

    }

    public List<String> getPlacesNames() {
        List<String> names = new ArrayList<>();

        for (Place p : databases) {
            names.add(p.getName());
        }

        for (Place p : restaurants) {
            names.add(p.getName());
        }

        for (Place p : bars) {
            names.add(p.getName());
        }

        return names;
    }

    public List<Place> getAllPlaces() {
        List<Place> places = new ArrayList<>();
        places.addAll(databases);
        places.addAll(restaurants);
        places.addAll(bars);
        return places;
    }

    protected Places(Parcel in) {
        databases = in.createTypedArrayList(Place.CREATOR);
        restaurants = in.createTypedArrayList(Place.CREATOR);
        bars = in.createTypedArrayList(Place.CREATOR);
    }

    public static final Creator<Places> CREATOR = new Creator<Places>() {
        @Override
        public Places createFromParcel(Parcel in) {
            return new Places(in);
        }

        @Override
        public Places[] newArray(int size) {
            return new Places[size];
        }
    };

    public ArrayList<Place> getDatabases() {
        return databases;
    }

    public void setDatabases(ArrayList<Place> databases) {
        this.databases = databases;
    }

    public ArrayList<Place> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(ArrayList<Place> restaurants) {
        this.restaurants = restaurants;
    }

    public ArrayList<Place> getBars() {
        return bars;
    }

    public void setBars(ArrayList<Place> bars) {
        this.bars = bars;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(databases);
        dest.writeTypedList(restaurants);
        dest.writeTypedList(bars);
    }
}
