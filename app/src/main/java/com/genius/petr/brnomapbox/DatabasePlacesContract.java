package com.genius.petr.brnomapbox;

import android.provider.BaseColumns;

/**
 * Created by Petr on 3. 3. 2017.
 */

public final class DatabasePlacesContract {
    private DatabasePlacesContract(){};

    public static class PlaceEntry implements BaseColumns {
        public static final String TABLE_NAME = "places";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_OPEN = "open";
    }
}
