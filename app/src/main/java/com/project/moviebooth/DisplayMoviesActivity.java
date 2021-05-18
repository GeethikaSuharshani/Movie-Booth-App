package com.project.moviebooth;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.project.moviebooth.database.MovieData;
import com.project.moviebooth.utils.ListViewUtility;

import java.util.ArrayList;

import static android.provider.BaseColumns._ID;
import static com.project.moviebooth.database.Constants.FAVOURITES;
import static com.project.moviebooth.database.Constants.MOVIE_TITLE;
import static com.project.moviebooth.database.Constants.TABLE_NAME;

public class DisplayMoviesActivity extends AppCompatActivity {
    private Button addToFavouritesBtn;
    private MovieData movieData;
    private ArrayList<String> movies = new ArrayList<>(); //ArrayList to store movies saved in the database
    private ArrayList<Integer> movie_ids = new ArrayList<>(); //ArrayList to store record ids of the movies in the movies ArrayList
    private ArrayList<Boolean> favouriteStatus = new ArrayList<>(); //ArrayList to store favourite status of the movies (favourite => true, not favourite => false)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_movies);

        addToFavouritesBtn = findViewById(R.id.add_to_favourites_btn);
        movieData = new MovieData(DisplayMoviesActivity.this);

        //restore the preserved state of DisplayMoviesActivity
        if(savedInstanceState != null) {
            movies = savedInstanceState.getStringArrayList("movies");
            movie_ids = savedInstanceState.getIntegerArrayList("movie_ids");
            int favouriteStatusSize = savedInstanceState.getInt("favouriteStatusSize");
            for(int i = 0; i < favouriteStatusSize; i++) {
                favouriteStatus.add(savedInstanceState.getBoolean(String.format("favouriteStatus %s", i)));
            }
        } else {
            getMoviesList(); //get list of saved movies from the database
            if(!movies.isEmpty()) {
                for (int i = 0; i < movies.size(); i++) {
                    favouriteStatus.add(false);
                }
            }
        }
        if(!movies.isEmpty()) {
            //create listView with data available in movies ArrayList
            CustomAdapter adapter = new CustomAdapter();
            ListView listView = findViewById(R.id.list_view);
            listView.setAdapter(adapter);
            ListViewUtility.setListViewHeight(listView);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //save the UI state of the DisplayMoviesActivity
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("movies", movies);
        outState.putIntegerArrayList("movie_ids", movie_ids);
        outState.putInt("favouriteStatusSize", favouriteStatus.size());
        for(int i = 0; i < favouriteStatus.size(); i++) {
            outState.putBoolean(String.format("favouriteStatus %s", i), favouriteStatus.get(i));
        }
    }

    public void getMoviesList() { //get list of movies saved in the database
        SQLiteDatabase database = movieData.getReadableDatabase();
        String[] columns = {_ID, MOVIE_TITLE};
        //query to get saved movies list, ordered by movie_title
        Cursor cursor = database.query(TABLE_NAME, columns, null, null, null, null, MOVIE_TITLE);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                movie_ids.add(id);
                String movieTitle = cursor.getString(1);
                try {
                    //capitalize the first letter of each word in movieTitle
                    char[] charArray = movieTitle.toCharArray();
                    for (int i = 0; i < movieTitle.length(); i++) {
                        if (i == 0 || charArray[i] != ' ' && charArray[i - 1] == ' ') {
                            charArray[i] = (char) Character.toUpperCase(charArray[i]);
                        }
                    }
                    movieTitle = new String(charArray);
                } finally {
                    //replace any two single quotes in movieTitle with single quotes
                    String formattedMovieTitle = movieTitle.replaceAll("''", "'");
                    movies.add(formattedMovieTitle);
                }
            }
            cursor.close();
            database.close();
        } else {
            Snackbar.make(addToFavouritesBtn, getString(R.string.no_saved_movies_message), Snackbar.LENGTH_LONG).show();
        }
    }

    public void addToFavourites(View view) { //add selected movies as favourite movies to the database
        if (!movies.isEmpty()) {
            SQLiteDatabase database = movieData.getReadableDatabase();
            for(int i = 0; i < movie_ids.size(); i++) { //update the favourite state of each movie in the list based on the user input
                boolean favouriteMovie = favouriteStatus.get(i);
                int favouriteState = 0;
                if(favouriteMovie) {
                    favouriteState = 1;
                }
                ContentValues values = new ContentValues();
                values.put(FAVOURITES, favouriteState);
                String whereClause = _ID + " = " + movie_ids.get(i);
                database.update(TABLE_NAME, values, whereClause, null);
            }
            Snackbar.make(addToFavouritesBtn, getString(R.string.added_to_favourites_message), Snackbar.LENGTH_LONG).show();
            movieData.close();
        }
    }

    //CustomAdapter class to create the listView with movies ArrayList
    private class CustomAdapter extends ArrayAdapter<String> {
        public CustomAdapter() {
            super(DisplayMoviesActivity.this, R.layout.checkbox_list_view_item, movies);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            View rowView = view;
            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.checkbox_list_view_item, parent, false);
            }
            TextView text = (TextView) rowView.findViewById(R.id.label);
            text.setText(movies.get(position));
            CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.checkbox);
            //set OnCheckedChangeListener for each CheckBox to update boolean value in the favouriteStatus ArrayList, based on the current checked status of the CheckBox
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> favouriteStatus.set(position, isChecked));
            checkBox.setChecked(favouriteStatus.get(position));
            return rowView;
        }
    }
}