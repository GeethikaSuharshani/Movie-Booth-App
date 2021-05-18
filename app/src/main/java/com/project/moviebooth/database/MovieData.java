package com.project.moviebooth.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.provider.BaseColumns._ID;
import static com.project.moviebooth.database.Constants.DIRECTOR;
import static com.project.moviebooth.database.Constants.FAVOURITES;
import static com.project.moviebooth.database.Constants.MOVIE_CAST;
import static com.project.moviebooth.database.Constants.MOVIE_TITLE;
import static com.project.moviebooth.database.Constants.RATING;
import static com.project.moviebooth.database.Constants.RELEASED_YEAR;
import static com.project.moviebooth.database.Constants.REVIEW;
import static com.project.moviebooth.database.Constants.TABLE_NAME;

public class MovieData extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "movie_data.db";
    public static final int DATABASE_VERSION = 1;

    //Create a helper object for movie_data database
    public MovieData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create movie_data table in the database
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MOVIE_TITLE + " TEXT NOT NULL,"
                + RELEASED_YEAR + " INTEGER NOT NULL,"
                + DIRECTOR + " TEXT NOT NULL,"
                + MOVIE_CAST + " TEXT NOT NULL,"
                + RATING + " INTEGER NOT NULL,"
                + REVIEW + " TEXT NOT NULL,"
                + FAVOURITES + " BOOLEAN DEFAULT 0);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}