package com.project.moviebooth;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.project.moviebooth.database.MovieData;

import java.util.Calendar;

import static android.provider.BaseColumns._ID;
import static com.project.moviebooth.database.Constants.DIRECTOR;
import static com.project.moviebooth.database.Constants.FAVOURITES;
import static com.project.moviebooth.database.Constants.MOVIE_CAST;
import static com.project.moviebooth.database.Constants.MOVIE_TITLE;
import static com.project.moviebooth.database.Constants.RATING;
import static com.project.moviebooth.database.Constants.RELEASED_YEAR;
import static com.project.moviebooth.database.Constants.REVIEW;
import static com.project.moviebooth.database.Constants.TABLE_NAME;

public class UpdateMovieDetailsActivity extends AppCompatActivity {
    private String[] movieDetails; //string array to store currently saved data in the database, about the selected movie
    private TextInputLayout movieTitleInput;
    private TextInputLayout releasedYearInput;
    private TextInputLayout directorInput;
    private TextInputLayout castInput;
    private TextView ratingText;
    private RatingBar ratingInput;
    private TextInputLayout reviewInput;
    private TextView favouriteText;
    private ImageView favouriteImage;
    private MovieData movieData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_movie_details);

        Intent intent = getIntent();
        movieDetails = intent.getStringArrayExtra(EditMoviesActivity.MOVIE_DETAILS); //get currently available data about the selected movie from the intent

        movieTitleInput = findViewById(R.id.movie_title);
        releasedYearInput = findViewById(R.id.released_year);
        directorInput = findViewById(R.id.director);
        castInput = findViewById(R.id.cast);
        ratingText = findViewById(R.id.rating_text);
        ratingInput = findViewById(R.id.rating);
        reviewInput = findViewById(R.id.review);
        favouriteText = findViewById(R.id.favourite_text);
        favouriteImage = findViewById(R.id.favourite_icon);
        movieData = new MovieData(UpdateMovieDetailsActivity.this);

        //restore the preserved state of UpdateMovieDetailsActivity
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
            ratingText.setText(savedInstanceState.getString("rating_text"));
            if(reviewInput.getEditText() != null) {
                reviewInput.getEditText().setText(savedInstanceState.getString("review"));
                reviewInput.setError(savedInstanceState.getString("review_error"));
            }
            favouriteText.setText(savedInstanceState.getString("favourite_text"));
            if(favouriteText.getText().equals(getString(R.string.favourite_movie_text))) {
                favouriteImage.setColorFilter(Color.RED);
            } else {
                favouriteImage.setColorFilter(Color.WHITE);
            }
        } else { //show currently saved data about the selected movie, to the user
            if (movieTitleInput.getEditText() != null) {
                movieTitleInput.getEditText().setText(movieDetails[1]);
            }
            if (releasedYearInput.getEditText() != null) {
                releasedYearInput.getEditText().setText(movieDetails[2]);
            }
            if (directorInput.getEditText() != null) {
                directorInput.getEditText().setText(movieDetails[3]);
            }
            if (castInput.getEditText() != null) {
                castInput.getEditText().setText(movieDetails[4]);
            }
            ratingText.setText(String.format(getString(R.string.rating_label_text), movieDetails[5]));
            try {
                ratingInput.setRating(Float.parseFloat(movieDetails[5]));
            } catch (Exception e) {
                Log.e("UpdateMovieDetails", getString(R.string.float_parsing_exception_message));
                ratingInput.setRating(0);
            }
            if (reviewInput.getEditText() != null) {
                reviewInput.getEditText().setText(movieDetails[6]);
            }
            if (Integer.parseInt(movieDetails[7]) == 1) {
                favouriteText.setText(R.string.favourite_movie_text);
                favouriteImage.setColorFilter(Color.RED);
            } else {
                favouriteText.setText(R.string.not_favourite_movie_text);
                favouriteImage.setColorFilter(Color.WHITE);
            }
        }
        //set OnRatingBarChangeListener to change the ratingText based on the rating given by the user
        ratingInput.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> ratingText.setText(String.format("Rating : %s/10", (int) rating)));
        //set OnClickListener to change the favouriteText & favouriteImage colour based on the user input
        favouriteImage.setOnClickListener(v -> {
            if(favouriteText.getText().equals(getString(R.string.not_favourite_movie_text))) {
                favouriteImage.setColorFilter(Color.RED);
                favouriteText.setText(R.string.favourite_movie_text);
            } else {
                favouriteImage.setColorFilter(Color.WHITE);
                favouriteText.setText(R.string.not_favourite_movie_text);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //save the UI state of the UpdateMovieDetailsActivity
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
        outState.putString("rating_text", ratingText.getText().toString());
        if(reviewInput.getEditText() != null) {
            outState.putString("review", reviewInput.getEditText().getText().toString());
            if(reviewInput.getError() != null) {
                outState.putString("review_error", reviewInput.getError().toString());
            } else {
                outState.putString("review_error", null);
            }
        }
        outState.putString("favourite_text", favouriteText.getText().toString());
    }

    public void updateData(View view) { //validate the data entered by user and if valid, update the details of the movie saved in the database
        String movieTitle = "";
        int releasedYear = 0;
        String director = "";
        String cast = "";
        int rating = 0;
        String review = "";
        int favouriteState;
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
        int ratingText = (int) ratingInput.getRating(); //get rating provided by the user
        if(ratingText == 0) {
            Toast.makeText(UpdateMovieDetailsActivity.this, getString(R.string.rating_required_message), Toast.LENGTH_LONG).show();
            validData = false;
        } else {
            rating = ratingText;
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
        if(favouriteText.getText().equals(getString(R.string.not_favourite_movie_text))) { //check the favourite state of the movie based on the user input
            favouriteState = 0;
        } else {
            favouriteState = 1;
        }

        if(validData) { //update data in the database if all provided details are valid
            Button updateBtn = findViewById(R.id.update_button);
            SQLiteDatabase database = movieData.getReadableDatabase();
            //replace single quotes in user inputs with two single quotes, to use them in query without making errors
            String formattedMovieTitle = movieTitle.replaceAll("'", "''");
            String formattedDirector = director.replaceAll("'", "''");
            String formattedCast = cast.replaceAll("'", "''");
            String formattedReview = review.replaceAll("'", "''");
            ContentValues values = new ContentValues();
            values.put(MOVIE_TITLE, formattedMovieTitle);
            values.put(RELEASED_YEAR, releasedYear);
            values.put(DIRECTOR, formattedDirector);
            values.put(MOVIE_CAST, formattedCast);
            values.put(RATING, rating);
            values.put(REVIEW, formattedReview);
            values.put(FAVOURITES, favouriteState);
            String whereClause = _ID + " = " + movieDetails[0];
            //query to update the details of the movie in the database
            database.update(TABLE_NAME, values, whereClause, null);

            Snackbar.make(updateBtn, getString(R.string.updated_movie_details_message), Snackbar.LENGTH_LONG).show();
            movieTitleInput.getEditText().getText().clear();
            releasedYearInput.getEditText().getText().clear();
            directorInput.getEditText().getText().clear();
            castInput.getEditText().getText().clear();
            ratingInput.setRating(0);
            reviewInput.getEditText().getText().clear();
            movieData.close();
        }
    }
}