package com.project.moviebooth;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
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

public class SearchActivity extends AppCompatActivity {
    private TextInputLayout searchInput;
    private ListView listView;
    private MovieData movieData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchInput = findViewById(R.id.search_bar);
        listView = findViewById(R.id.list_view);
        movieData = new MovieData(SearchActivity.this);

        //restore the preserved state of SearchActivity
        if(savedInstanceState != null) {
            if (searchInput.getEditText() != null) {
                searchInput.getEditText().setText(savedInstanceState.getString("search_term"));
                searchInput.setError(savedInstanceState.getString("search_term_error"));
                getSearchResults(searchInput);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //save the UI state of the SearchActivity
        super.onSaveInstanceState(outState);
        if (searchInput.getEditText() != null) {
            outState.putString("search_term", searchInput.getEditText().getText().toString());
            if (searchInput.getError() != null) {
                outState.putString("search_term_error", searchInput.getError().toString());
            } else {
                outState.putString("search_term_error", null);
            }
        }
    }

    public void getSearchResults(View view) { //search for the movies that has the search term in its title, director or cast & give back results
        ArrayList<String> movies = new ArrayList<>(); //ArrayList to store result movies
        ArrayList<Integer> movie_ids = new ArrayList<>(); //ArrayList to store ids of the result movies
        listView.setAdapter(null);
        String searchTerm = "";
        boolean valid = true;
        if(searchInput.getEditText() != null) {
            searchTerm = searchInput.getEditText().getText().toString().toLowerCase();
            if(searchTerm.isEmpty()) { //check whether the search term is provided & if not provided show an error message
                searchInput.setError(getString(R.string.required_error_message));
                valid = false;
            } else {
                searchInput.setError(null);
            }
        }

        if(valid) { //search for results in the database, if search term is provided
            SQLiteDatabase database = movieData.getReadableDatabase();
            //replace single quotes in user input with two single quotes, to use them in query without making errors
            String formattedSearchTerm = searchTerm.replaceAll("'", "''");
            String[] columns = {_ID, MOVIE_TITLE};
            String selection = MOVIE_TITLE + " LIKE '%" + formattedSearchTerm + "%' OR " + DIRECTOR + " LIKE '%" + formattedSearchTerm + "%' OR " + MOVIE_CAST + " LIKE '%" + formattedSearchTerm + "%'";
            //query to get matching results from the database
            Cursor cursor = database.query(TABLE_NAME, columns, selection, null, null, null, MOVIE_TITLE);
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
                Snackbar.make(searchInput, getString(R.string.no_matching_results_message), Snackbar.LENGTH_LONG).show();
            }

            if(!movies.isEmpty()) {
                //create the listView with movies ArrayList
                ArrayAdapter<String> adapter = new ArrayAdapter<>(SearchActivity.this, R.layout.plain_list_view_item, movies);
                listView.setAdapter(adapter);
                ListViewUtility.setListViewHeight(listView);

                //set OnItemClickListener to show details of the movie, when a movie in the result list is selected
                AdapterView.OnItemClickListener itemClickedHandler = (parent, v, position, id) -> {
                    SQLiteDatabase db = movieData.getReadableDatabase();
                    String[] columnsList = {MOVIE_TITLE, RELEASED_YEAR, DIRECTOR, MOVIE_CAST, RATING, REVIEW, FAVOURITES};
                    String selectionString = _ID + " = " + movie_ids.get(position);
                    //query to get all details of the selected movie saved in the database
                    Cursor dataCursor = db.query(TABLE_NAME, columnsList, selectionString, null, null, null, null);
                    if (dataCursor.getCount() != 0) {
                        while (dataCursor.moveToNext()) {
                            String movieTitle = dataCursor.getString(0);
                            int releasedYear = dataCursor.getInt(1);
                            String director = dataCursor.getString(2);
                            String cast = dataCursor.getString(3);
                            int rating = dataCursor.getInt(4);
                            String review = dataCursor.getString(5);
                            int favouriteState = dataCursor.getInt(6);
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
                                //create a popup window with the details of the selected movie
                                LayoutInflater inflater = getLayoutInflater();
                                View popupView = inflater.inflate(R.layout.search_results_popup, parent, false);

                                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                                PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

                                TextView title = popupView.findViewById(R.id.title);
                                title.setText(movieTitle.replaceAll("''", "'"));
                                TextView year = popupView.findViewById(R.id.released_year);
                                year.setText(String.format("Released Year : %s", releasedYear));
                                TextView movieDirector = popupView.findViewById(R.id.director);
                                movieDirector.setText(String.format("Director : %s", director.replaceAll("''", "'")));
                                TextView movieCast = popupView.findViewById(R.id.cast);
                                movieCast.setText(String.format("Cast : %s", cast.replaceAll("''", "'")));
                                TextView movieRating = popupView.findViewById(R.id.rating);
                                movieRating.setText(String.format("Rating : %s", rating));
                                TextView movieReview = popupView.findViewById(R.id.review);
                                movieReview.setText(String.format("Review : %s", review.replaceAll("''", "'")));
                                TextView favState = popupView.findViewById(R.id.favourite);
                                favState.setText(String.format("Favourite State : %s", (favouriteState == 1 ? "Favourite" : "Not Favourite")));

                                popupWindow.showAtLocation(v, Gravity.CENTER, Gravity.CENTER, Gravity.CENTER);
                                //set OnClickListener to close popupWindow
                                popupView.setOnClickListener(v1 -> popupWindow.dismiss());
                            }
                        }
                        dataCursor.close();
                        db.close();
                    }
                };
                listView.setOnItemClickListener(itemClickedHandler);
            }
        }
    }
}