package com.project.moviebooth;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.project.moviebooth.database.MovieData;
import com.project.moviebooth.utils.ListViewUtility;

import java.util.ArrayList;

import static android.provider.BaseColumns._ID;
import static com.project.moviebooth.database.Constants.DIRECTOR;
import static com.project.moviebooth.database.Constants.FAVOURITES;
import static com.project.moviebooth.database.Constants.MOVIE_CAST;
import static com.project.moviebooth.database.Constants.MOVIE_TITLE;
import static com.project.moviebooth.database.Constants.RATING;
import static com.project.moviebooth.database.Constants.RELEASED_YEAR;
import static com.project.moviebooth.database.Constants.REVIEW;
import static com.project.moviebooth.database.Constants.TABLE_NAME;

public class EditMoviesActivity extends AppCompatActivity {
    private ListView listView;
    private MovieData movieData;
    private ArrayList<String[]> movies = new ArrayList<>(); //ArrayList to store details of every movie saved in the database
    private ArrayList<String> movieTitles = new ArrayList<>(); //ArrayList to store titles of the movies saved in the database
    public static final String MOVIE_DETAILS = "com.project.moviebooth.extra.MOVIE_DETAILS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_movies);

        movieData = new MovieData(EditMoviesActivity.this);
        getMoviesList(); //get list of all movies saved in the database
        if(!movies.isEmpty()) {
            //create listView with data available in the movieTitles ArrayList
            ArrayAdapter<String> adapter = new ArrayAdapter<>(EditMoviesActivity.this, R.layout.plain_list_view_item, movieTitles);
            listView = findViewById(R.id.list_view);
            listView.setAdapter(adapter);
            ListViewUtility.setListViewHeight(listView);

            //set OnItemClickListener to each list item in the listView, to provide user the chance to edit movie details when clicked on movie title
            AdapterView.OnItemClickListener itemClickedHandler = (parent, view, position, id) -> {
                Intent intent = new Intent(EditMoviesActivity.this, UpdateMovieDetailsActivity.class);
                intent.putExtra(MOVIE_DETAILS, movies.get(position));
                startActivity(intent);
            };
            listView.setOnItemClickListener(itemClickedHandler);
        }
    }

    public void getMoviesList() { //get list of all the saved movies in the database
        SQLiteDatabase database = movieData.getReadableDatabase();
        String[] columns = {_ID, MOVIE_TITLE, RELEASED_YEAR, DIRECTOR, MOVIE_CAST, RATING, REVIEW, FAVOURITES};
        //query to get all details of each movie saved in the database
        Cursor cursor = database.query(TABLE_NAME, columns, null, null, null, null, MOVIE_TITLE);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String movieTitle = cursor.getString(1);
                int releasedYear = cursor.getInt(2);
                String director = cursor.getString(3);
                String cast = cursor.getString(4);
                int rating = cursor.getInt(5);
                String review = cursor.getString(6);
                int favouriteState = cursor.getInt(7);

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
                    String formattedDirector = director.replaceAll("''", "'");
                    String formattedCast = cast.replaceAll("''", "'");
                    String formattedReview = review.replaceAll("''", "'");
                    movieTitles.add(formattedMovieTitle);
                    String[] movieDetails = {String.valueOf(id), formattedMovieTitle, String.valueOf(releasedYear), formattedDirector, formattedCast,
                            String.valueOf(rating), formattedReview, String.valueOf(favouriteState)};
                    movies.add(movieDetails);
                }
            }
            cursor.close();
            database.close();
        } else {
            TextView text = findViewById(R.id.text);
            Snackbar.make(text, getString(R.string.no_saved_movies_message), Snackbar.LENGTH_LONG).show();
        }
    }
}