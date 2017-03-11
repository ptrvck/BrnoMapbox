package com.genius.petr.brnomapbox;

import android.provider.BaseColumns;

/**
 * Created by Petr on 3. 3. 2017.
 */

public final class DatabasePlacesContract {
    private DatabasePlacesContract(){};

    public static class PlaceEntry implements BaseColumns {
        public static final String TABLE_NAME = "places";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LNG = "lng";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
       // public static final String COLUMN_NAME_OPEN = "open";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                PlaceEntry.TABLE_NAME + " (" +
                PlaceEntry._ID + " INTEGER PRIMARY KEY," +
                PlaceEntry.COLUMN_NAME_NAME + " TEXT," +
                PlaceEntry.COLUMN_NAME_TYPE + " TEXT," +
                PlaceEntry.COLUMN_NAME_LAT + " DOUBLE," +
                PlaceEntry.COLUMN_NAME_LNG + " DOUBLE," +
                PlaceEntry.COLUMN_NAME_DESCRIPTION + " TEXT" +
          ")";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + PlaceEntry.TABLE_NAME;
    }
}
