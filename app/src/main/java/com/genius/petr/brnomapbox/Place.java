package com.genius.petr.brnomapbox;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.internal.Streams;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by Petr on 3. 3. 2017.
 */

public class Place implements Parcelable{
    private final int id;
    private String name;
    //private PLACE_TYPE type;
    private String type;
    //private OpeningHours openingHours;
    private String description;
    private LatLng location;

    Place(int id) {
        this.id = id;
        this.name = null;
        this.location = null;
    }

    protected Place(Parcel in) {
        id = in.readInt();
        name = in.readString();
        type = in.readString();
        description = in.readString();
        location = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
/*
    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }
*/
    public int getId() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean valid() {return ((location!=null) &&(name!=null));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(description);
        dest.writeParcelable(location, flags);
    }
}
