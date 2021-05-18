package com.project.moviebooth;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.project.moviebooth.database.MovieData;
import com.project.moviebooth.utils.ListViewUtility;

import java.util.ArrayList;

import static com.project.moviebooth.database.Constants.MOVIE_TITLE;
import static com.project.moviebooth.database.Constants.TABLE_NAME;

public class RatingsActivity extends AppCompatActivity {
    public static final String SEARCH_TERM = "com.project.moviebooth.extra.SEARCH_TERM";
    private Button findInIMDbBtn;
    private MovieData movieData;
    private ArrayList<String> movies = new ArrayList<>(); //ArrayList to store list of movies saved in the database
    private int selectedPosition = 0;
    private String searchTerm = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);

        findInIMDbBtn = findViewById(R.id.find_in_imdb_button);
        movieData = new MovieData(RatingsActivity.this);
        //restore the preserved state of RatingsActivity
        if(savedInstanceState != null) {
            searchTerm = savedInstanceState.getString("searchTerm");
            selectedPosition = savedInstanceState.getInt("selectedPosition");
        }
        getMoviesList(); //get list of all movies saved in the database
        if(!movies.isEmpty()) {
            //create listView with data available in the movies ArrayList
            CustomAdapter adapter = new CustomAdapter();
            ListView listView = findViewById(R.id.list_view);
            listView.setAdapter(adapter);
            ListViewUtility.setListViewHeight(listView);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //save the UI state of the RatingsActivity
        super.onSaveInstanceState(outState);
        outState.putString("searchTerm", searchTerm);
        outState.putInt("selectedPosition", selectedPosition);
    }

    public void getMoviesList() { //get list of all the saved movies in the database
        SQLiteDatabase database = movieData.getReadableDatabase();
        String[] columns = {MOVIE_TITLE};
        //query to get all movies saved in the database
        Cursor cursor = database.query(TABLE_NAME, columns, null, null, null, null, MOVIE_TITLE);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                String movieTitle = cursor.getString(0);
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
                    //replace any two single quotes with single quotes
                    String formattedMovieTitle = movieTitle.replaceAll("''", "'");
                    movies.add(formattedMovieTitle);
                }
            }
            cursor.close();
            database.close();
        } else {
            Snackbar.make(findInIMDbBtn, getString(R.string.no_saved_movies_message), Snackbar.LENGTH_LONG).show();
        }
    }

    public void findInIMBb(View view) { //start IMDbResultsActivity if searchTerm is not empty
        if(!searchTerm.isEmpty()) {
            Intent intent = new Intent(RatingsActivity.this, IMDbResultsActivity.class);
            intent.putExtra(SEARCH_TERM, searchTerm);
            startActivity(intent);
        }
    }

    //CustomAdapter class to create the listView with movies ArrayList
    private class CustomAdapter extends ArrayAdapter<String> {
        public CustomAdapter() {
            super(RatingsActivity.this, R.layout.radio_btn_list_view_item, movies);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            View rowView = view;
            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.radio_btn_list_view_item, parent, false);
            }
            TextView text = (TextView) rowView.findViewById(R.id.label);
            text.setText(movies.get(position));
            RadioButton radioButton = (RadioButton) rowView.findViewById(R.id.radio_button);
            //set the RadioButton checked if user has selected the movie associated with that RadioButton, else make the RadioButton unchecked
            if(position == selectedPosition) {
                radioButton.setChecked(true);
                searchTerm = movies.get(position);
            } else {
                radioButton.setChecked(false);
            }
            radioButton.setTag(position);
            //set OnClickListener for each RadioButton to update the selectedPosition & to refresh the ListView, each time when user select an option
            radioButton.setOnClickListener(v -> {
                selectedPosition = (Integer) v.getTag();
                notifyDataSetChanged();
            });
            return rowView;
        }
    }
}