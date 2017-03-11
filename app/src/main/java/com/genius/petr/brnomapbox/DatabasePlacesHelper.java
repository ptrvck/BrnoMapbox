package com.genius.petr.brnomapbox;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.genius.petr.brnomapbox.DatabasePlacesContract.PlaceEntry.CREATE_TABLE;
import static com.genius.petr.brnomapbox.DatabasePlacesContract.PlaceEntry.DELETE_TABLE;

/**
 * Created by Petr on 3. 3. 2017.
 */

public class DatabasePlacesHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "Places.db";

    public DatabasePlacesHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        //db.execSQL(DELETE_TABLE);
        //onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
