package com.project.moviebooth.database;

import android.provider.BaseColumns;

public interface Constants extends BaseColumns {
    String TABLE_NAME = "movie_data";
    //Columns in movie_data table
    String MOVIE_TITLE = "movie_title";
    String RELEASED_YEAR = "released_year";
    String DIRECTOR = "director";
    String MOVIE_CAST = "movie_cast";
    String RATING = "rating";
    String REVIEW = "review";
    String FAVOURITES = "favourites";
}
