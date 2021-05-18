package com.project.moviebooth;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.project.moviebooth.database.MovieData;

import java.util.Calendar;

import static com.project.moviebooth.database.Constants.DIRECTOR;
import static com.project.moviebooth.database.Constants.MOVIE_CAST;
import static com.project.moviebooth.database.Constants.MOVIE_TITLE;
import static com.project.moviebooth.database.Constants.RATING;
import static com.project.moviebooth.database.Constants.RELEASED_YEAR;
import static com.project.moviebooth.database.Constants.REVIEW;
import static com.project.moviebooth.database.Constants.TABLE_NAME;

public class RegisterMovieActivity extends AppCompatActivity {
    private TextInputLayout movieTitleInput;
    private TextInputLayout releasedYearInput;
    private TextInputLayout directorInput;
    private TextInputLayout castInput;
    private TextInputLayout ratingInput;
    private TextInputLayout reviewInput;
    private MovieData movieData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_movie);

        movieTitleInput = findViewById(R.id.movie_title);
        releasedYearInput = findViewById(R.id.released_year);
        directorInput = findViewById(R.id.director);
        castInput = findViewById(R.id.cast);
        ratingInput = findViewById(R.id.rating);
        reviewInput = findViewById(R.id.review);
        movieData = new MovieData(RegisterMovieActivity.this);

        //restore the preserved state of RegisterMovieActivity
        if(savedInstanceState != null) {
            if(movieTitleInput.getEditText() != null) {
                movieTitleInput.getEditText().setText(savedInstanceState.getString("movie_title"));
                movieTitleInput.setError(savedInstanceState.getString("movie_title_error"));
            }
            if(releasedYearInput.getEditText() != null) {
                releasedYearInput.getEditText().setText(savedInstanceState.getString("released_year"));
                releasedYearInput.setError(savedInstanceState.getString("released_year_error"));
            }
            if(directorInput.getEditText() != null) {
                directorInput.getEditText().setText(savedInstanceState.getString("director"));
                directorInput.setError(savedInstanceState.getString("director_error"));
            }
            if(castInput.getEditText() != null) {
                castInput.getEditText().setText(savedInstanceState.getString("cast"));
                castInput.setError(savedInstanceState.getString("cast_error"));
            }
            if(ratingInput.getEditText() != null) {
                ratingInput.getEditText().setText(savedInstanceState.getString("rating"));
                ratingInput.setError(savedInstanceState.getString("rating_error"));
            }
            if(reviewInput.getEditText() != null) {
                reviewInput.getEditText().setText(savedInstanceState.getString("review"));
                reviewInput.setError(savedInstanceState.getString("review_error"));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //save the UI state of the RegisterMovieActivity
        super.onSaveInstanceState(outState);
        if(movieTitleInput.getEditText() != null) {
            outState.putString("movie_title", movieTitleInput.getEditText().getText().toString());
            if(movieTitleInput.getError() != null) {
                outState.putString("movie_title_error", movieTitleInput.getError().toString());
            } else {
                outState.putString("movie_title_error", null);
            }
        }
        if(releasedYearInput.getEditText() != null) {
            outState.putString("released_year", releasedYearInput.getEditText().getText().toString());
            if(releasedYearInput.getError() != null) {
                outState.putString("released_year_error", releasedYearInput.getError().toString());
            } else {
                outState.putString("released_year_error", null);
            }
        }
        if(directorInput.getEditText() != null) {
            outState.putString("director", directorInput.getEditText().getText().toString());
            if(directorInput.getError() != null) {
                outState.putString("director_error", directorInput.getError().toString());
            } else {
                outState.putString("director_error", null);
            }
        }
        if(castInput.getEditText() != null) {
            outState.putString("cast", castInput.getEditText().getText().toString());
            if(castInput.getError() != null) {
                outState.putString("cast_error", castInput.getError().toString());
            } else {
                outState.putString("cast_error", null);
            }
        }
        if(ratingInput.getEditText() != null) {
            outState.putString("rating", ratingInput.getEditText().getText().toString());
            if(ratingInput.getError() != null) {
                outState.putString("rating_error", ratingInput.getError().toString());
            } else {
                outState.putString("rating_error", null);
            }
        }
        if(reviewInput.getEditText() != null) {
            outState.putString("review", reviewInput.getEditText().getText().toString());
            if(reviewInput.getError() != null) {
                outState.putString("review_error", reviewInput.getError().toString());
            } else {
                outState.putString("review_error", null);
            }
        }
    }

    public void saveData(View view) { //validate the data entered by user and if valid, save the details of the movie in to the database
        String movieTitle = "";
        int releasedYear = 0;
        String director = "";
        String cast = "";
        int rating = 0;
        String review = "";
        boolean validData = true;

        if(movieTitleInput.getEditText() != null) {
            String titleText = movieTitleInput.getEditText().getText().toString().trim().toLowerCase();
            if(!titleText.isEmpty()) { //check whether the movie title is provided & if not provided show an error message
                movieTitle = titleText;
                movieTitleInput.setError(null);
            } else {
                movieTitleInput.setError(getString(R.string.required_error_message));
                validData = false;
            }
        }
        if(releasedYearInput.getEditText() != null) {
            String yearText = releasedYearInput.getEditText().getText().toString().trim();
            if(!yearText.isEmpty()) { //check whether the released year is provided & if not provided show an error message
                int parsedReleasedYear = Integer.parseInt(yearText);
                if(parsedReleasedYear > 1895) { //check whether the provided released year is a year after 1895 & if not, show an error message
                    if(parsedReleasedYear > Calendar.getInstance().get(Calendar.YEAR)) { //check whether the provided released year is a future year & if true, show an error message
                        releasedYearInput.setError(getString(R.string.future_year_error_message));
                        validData = false;
                    } else {
                        releasedYear = parsedReleasedYear;
                        releasedYearInput.setError(null);
                    }
                } else {
                    releasedYearInput.setError(getString(R.string.year_before_1895_error_message));
                    validData = false;
                }
            } else {
                releasedYearInput.setError(getString(R.string.required_error_message));
                validData = false;
            }
        }
        if(directorInput.getEditText() != null) {
            String directorText = directorInput.getEditText().getText().toString().trim().toLowerCase();
            if(!directorText.isEmpty()) { //check whether the director is provided & if not provided show an error message
                director = directorText;
                directorInput.setError(null);
            } else {
                directorInput.setError(getString(R.string.required_error_message));
                validData = false;
            }
        }
        if(castInput.getEditText() != null) {
            String castText = castInput.getEditText().getText().toString().trim().toLowerCase();
            if(!castText.isEmpty()) { //check whether the cast is provided & if not provided show an error message
                cast = castText;
                castInput.setError(null);
            } else {
                castInput.setError(getString(R.string.required_error_message));
                validData = false;
            }
        }
        if(ratingInput.getEditText() != null) {
            String ratingText = ratingInput.getEditText().getText().toString().trim();
            if(!ratingText.isEmpty()) { //check whether the rating is provided & if not provided show an error message
                int parsedRating = Integer.parseInt(ratingText);
                if(parsedRating >= 1 && parsedRating <= 10) { //check whether the rating is a number between 1-10 & if not, show an error message
                    rating = parsedRating;
                    ratingInput.setError(null);
                } else {
                    ratingInput.setError(getString(R.string.out_of_range_no_error_message));
                    validData = false;
                }
            } else {
                ratingInput.setError(getString(R.string.required_error_message));
                validData = false;
            }
        }
        if(reviewInput.getEditText() != null) {
            String reviewText = reviewInput.getEditText().getText().toString().trim().toLowerCase();
            if(!reviewText.isEmpty()) { //check whether the review is provided & if not provided show an error message
                review = reviewText;
                reviewInput.setError(null);
            } else {
                reviewInput.setError(getString(R.string.required_error_message));
                validData = false;
            }
        }

        if(validData) { //save data to the database if all provided details are valid
            Button saveBtn = findViewById(R.id.save_button);
            SQLiteDatabase database = movieData.getReadableDatabase();
            //replace single quotes in user inputs with two single quotes, to use them in query without making errors
            String formattedMovieTitle = movieTitle.replaceAll("'", "''");
            String formattedDirector = director.replaceAll("'", "''");
            String formattedCast = cast.replaceAll("'", "''");
            String formattedReview = review.replaceAll("'", "''");
            String[] columns = {MOVIE_TITLE, RELEASED_YEAR, DIRECTOR};
            String selection = MOVIE_TITLE + " = '" + formattedMovieTitle  + "' AND " + RELEASED_YEAR + " = '" + releasedYear + "' AND " + DIRECTOR + " = '" + formattedDirector + "'";
            //query to check whether the movie has already registered into the database
            Cursor cursor = database.query(TABLE_NAME, columns, selection, null, null, null, null);

            if (cursor.getCount() == 0) { //if movie has not registered before, save movie details into the database
                ContentValues values = new ContentValues();
                values.put(MOVIE_TITLE, formattedMovieTitle);
                values.put(RELEASED_YEAR, String.valueOf(releasedYear));
                values.put(DIRECTOR, formattedDirector);
                values.put(MOVIE_CAST, formattedCast);
                values.put(RATING, String.valueOf(rating));
                values.put(REVIEW, formattedReview);
                database.insertOrThrow(TABLE_NAME, null, values);

                Snackbar.make(saveBtn, getString(R.string.registered_movie_message), Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(saveBtn, getString(R.string.already_registered_movie_message), Snackbar.LENGTH_LONG).show();
            }
            movieTitleInput.getEditText().getText().clear();
            releasedYearInput.getEditText().getText().clear();
            directorInput.getEditText().getText().clear();
            castInput.getEditText().getText().clear();
            ratingInput.getEditText().getText().clear();
            reviewInput.getEditText().getText().clear();
            cursor.close();
            movieData.close();
        }
    }
}