package com.project.moviebooth;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.project.moviebooth.utils.ListViewUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class IMDbResultsActivity extends AppCompatActivity {
    private String searchedMovie;
    private TextView text;
    private ListView listView;
    private ArrayList<String[]> moviesResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imdb_results);

        text = findViewById(R.id.text);
        listView = findViewById(R.id.list_view);
        Intent intent = getIntent();
        searchedMovie = intent.getStringExtra(RatingsActivity.SEARCH_TERM); //get searchTerm from the intent

        //restore the preserved state of IMDbResultsActivity
        if (savedInstanceState != null) {
            int moviesResultsSize = savedInstanceState.getInt("moviesResultsSize");
            if(moviesResultsSize > 0) {
                for(int i = 0; i < moviesResultsSize; i++) {
                    moviesResults.add(savedInstanceState.getStringArray(String.format("moviesResults %s", i)));
                }
                //create listView with data available in moviesResults ArrayList
                CustomAdapter adapter = new CustomAdapter();
                listView.setAdapter(adapter);
                ListViewUtility.setListViewHeight(listView);
            }
        }
        if(moviesResults.isEmpty()) {
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new MovieDataLoadingTask().execute(); //start new MovieDataLoadingTask to load details of the movies matching with the searchTerm
            } else {
                Snackbar.make(text, "No network connection available !", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //save the UI state of the IMDbResultsActivity
        super.onSaveInstanceState(outState);
        outState.putInt("moviesResultsSize", moviesResults.size());
        if(!moviesResults.isEmpty()) {
            for(int i = 0; i < moviesResults.size(); i++) {
                outState.putStringArray(String.format("moviesResults %s", i), moviesResults.get(i));
            }
        }
    }

    //CustomAdapter class to create the listView with moviesResults ArrayList
    private class CustomAdapter extends ArrayAdapter<String[]> {
        public CustomAdapter() {
            super(IMDbResultsActivity.this, R.layout.imdb_results_card_view_item, moviesResults);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            View rowView = view;
            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.imdb_results_card_view_item, parent, false);
            }
            String[] movieDetails = moviesResults.get(position);
            ImageView image = (ImageView) rowView.findViewById(R.id.image);
            TextView title = (TextView) rowView.findViewById(R.id.movie);
            title.setText(movieDetails[0]);
            //set OnClickListener to the movie title, to show or hide movie image based on the visibility of the image
            title.setOnClickListener(v -> {
                if(image.getVisibility() == View.GONE) {
                    new MovieImageLoadingTask(image).execute(movieDetails[1]); //start new MovieImageLoadingTask to load the image of the selected movie
                    image.setVisibility(View.VISIBLE);
                    //set height of the list view to match the new content
                    ViewGroup.LayoutParams params = listView.getLayoutParams();
                    params.height = params.height + 850;
                    listView.setLayoutParams(params);
                    listView.requestLayout();
                } else {
                    image.setVisibility(View.GONE);
                    //set height of the list view to match content height
                    ViewGroup.LayoutParams params = listView.getLayoutParams();
                    params.height = params.height - 850;
                    listView.setLayoutParams(params);
                    listView.requestLayout();
                }
            });
            TextView rating = (TextView) rowView.findViewById(R.id.rating);
            rating.setText(String.format("Rating : %s", ((!movieDetails[2].equals("null") && movieDetails[2].length() != 0) ? movieDetails[2] : "Not available")));
            return rowView;
        }
    }

    //AsyncTask to load details(movieId, movieTitle, movieImageLink) of each movie that matching to searched movie title
    private class MovieDataLoadingTask extends AsyncTask<Void, Integer, String> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() { //create a progressDialog to show user the progress of the data loading process
            super.onPreExecute();
            progressDialog = new ProgressDialog(IMDbResultsActivity.this);
            progressDialog.setTitle("Loading Movie Data");
            progressDialog.setMessage("You'll be presented with results soon");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) { //set progress value
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected String doInBackground(Void... params) {
            final String BASE_URL = "https://imdb-api.com/en/API/SearchTitle/k_e65w2w7q/";
            Uri uri = Uri.parse(BASE_URL).buildUpon().appendPath(searchedMovie.toLowerCase()).build();

            try {
                URL requestURL = new URL(uri.toString());
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                try {
                    connection = (HttpURLConnection) requestURL.openConnection();
                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(15000);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.connect();
                    int response = connection.getResponseCode();

                    if (response == 200) {
                        inputStream = connection.getInputStream();
                        String contentAsString = convertStreamToString(inputStream);
                        return contentAsString;
                    }
                } catch (IOException e) {
                    Log.e("IMDbResults", "Error has occurred while trying to establish Http Connection");
                    progressDialog.dismiss();
                    Snackbar.make(text, getString(R.string.can_not_load_data_message),Snackbar.LENGTH_LONG).show();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } catch (MalformedURLException e) {
                Log.e("IMDbResults", "Error has occurred due to providing an invalid URL");
                progressDialog.dismiss();
                Snackbar.make(text, getString(R.string.can_not_load_data_message),Snackbar.LENGTH_LONG).show();
            } catch (IOException e) {
                Log.e("IMDbResults", "Error has occurred while trying to establish Http Connection");
                progressDialog.dismiss();
                Snackbar.make(text, getString(R.string.can_not_load_data_message),Snackbar.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) { //extract data from the returned response
            try {
                if (response != null && !response.isEmpty()) {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray resultsArray = jsonObject.getJSONArray("results");
                    ArrayList<String[]> results = new ArrayList<>();
                    for(int i = 0; i < resultsArray.length(); i++) {
                        JSONObject movieDetails = resultsArray.getJSONObject(i);
                        String movieId = (String) movieDetails.getString("id");
                        String movieTitle = (String) movieDetails.getString("title");
                        String movieImage = (String) movieDetails.getString("image");
                        results.add(new String[]{movieId, movieTitle, movieImage});
                    }
                    new RatingDetailsLoadingTask(results).execute(); //start new RatingDetailsLoadingTask to get rating details of each movie
                    progressDialog.dismiss();
                } else {
                    progressDialog.dismiss();
                    Snackbar.make(text, getString(R.string.no_similar_movies_results_message),Snackbar.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Log.e("IMDbResults", "Error has occurred while trying to get data from the retrieved response");
                progressDialog.dismiss();
                Snackbar.make(text, getString(R.string.can_not_load_data_message),Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class RatingDetailsLoadingTask extends AsyncTask<Void, Integer, ArrayList<String[]>> { //AsyncTask to load rating data of each movie
        private ProgressDialog progressDialog;
        private ArrayList<String[]> resultsList; //ArrayList that store all the data(movieId, movieTitle, movieImageLink) of the movies, retrieved from the MovieDataLoadingTask

        public RatingDetailsLoadingTask(ArrayList<String[]> resultsList) {
            this.resultsList = resultsList;
        }

        @Override
        protected void onPreExecute() { //create a progressDialog to show user the progress of the data loading process
            super.onPreExecute();
            progressDialog = new ProgressDialog(IMDbResultsActivity.this);
            progressDialog.setTitle("Loading Movie Rating Data");
            progressDialog.setMessage("You'll be presented with results soon");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) { //set progress value
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected ArrayList<String[]> doInBackground(Void... params) {
            ArrayList<String[]> results = new ArrayList<>(); //ArrayList that store movieTitle, movieImageLink & rating of each movie
            for(String[] movieDetails : resultsList) { //get rating data for each movie returned from the MovieDataLoadingTask
                final String BASE_URL = "https://imdb-api.com/en/API/UserRatings/k_e65w2w7q/";
                Uri uri = Uri.parse(BASE_URL).buildUpon().appendPath(movieDetails[0].trim()).build();

                try {
                    URL requestURL = new URL(uri.toString());
                    HttpURLConnection connection = null;
                    InputStream inputStream = null;
                    try {
                        connection = (HttpURLConnection) requestURL.openConnection();
                        connection.setReadTimeout(10000);
                        connection.setConnectTimeout(15000);
                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);
                        connection.connect();
                        int response = connection.getResponseCode();

                        if (response == 200) {
                            inputStream = connection.getInputStream();
                            String contentAsString = convertStreamToString(inputStream);
                            try {
                                if (contentAsString != null && !contentAsString.isEmpty()) {
                                    JSONObject jsonObject = new JSONObject(contentAsString);
                                    String rating = (String) jsonObject.getString("totalRating");
                                    results.add(new String[]{movieDetails[1], movieDetails[2], rating});
                                }
                            } catch (JSONException e) {
                                Log.e("IMDbResults", "Error has occurred while trying to get data from the retrieved response");
                                progressDialog.dismiss();
                                Snackbar.make(text, getString(R.string.can_not_load_data_message),Snackbar.LENGTH_LONG).show();
                            }
                        }
                    } catch (IOException e) {
                        Log.e("IMDbResults", "Error has occurred while trying to establish Http Connection");
                        progressDialog.dismiss();
                        Snackbar.make(text, getString(R.string.can_not_load_data_message),Snackbar.LENGTH_LONG).show();
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                } catch (MalformedURLException e) {
                    Log.e("IMDbResults", "Error has occurred due to providing an invalid URL");
                    progressDialog.dismiss();
                    Snackbar.make(text, getString(R.string.can_not_load_data_message),Snackbar.LENGTH_LONG).show();
                } catch (IOException e) {
                    Log.e("IMDbResults", "Error has occurred while trying to establish Http Connection");
                    progressDialog.dismiss();
                    Snackbar.make(text, getString(R.string.can_not_load_data_message),Snackbar.LENGTH_LONG).show();
                }
            }
            return results;
        }

        @Override
        protected void onPostExecute(ArrayList<String[]> response) {
            if(response != null && !response.isEmpty()) {
                moviesResults = response;
                //create listView with data available in moviesResults ArrayList
                CustomAdapter adapter = new CustomAdapter();
                listView.setAdapter(adapter);
                ListViewUtility.setListViewHeight(listView);
            }
            progressDialog.dismiss();
        }
    }

    public String convertStreamToString(InputStream inputStream) { //convert the returned InputStream into a String
        try {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            if (builder.length() == 0) {
                return null;
            }
            String resultString = builder.toString();
            return resultString;
        } catch (IOException e) {
            Log.e("IMDbResults", "Error has occurred while trying to collect data");
        }
        return null;
    }

    private class MovieImageLoadingTask extends AsyncTask<String, Integer, Bitmap> { //AsyncTask to load movie image, when user clicked on the movie title
        private ImageView imageView;
        private ProgressDialog progressDialog;

        public MovieImageLoadingTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected void onPreExecute() { //create a progressDialog to show user the progress of the image loading process
            super.onPreExecute();
            progressDialog = new ProgressDialog(IMDbResultsActivity.this);
            progressDialog.setTitle("Loading Movie Image");
            progressDialog.setMessage("You'll be presented with results soon");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) { //set progress value
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                //get corresponding image from the URL & return it as a Bitmap
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 600, 800, true);
                return resizedBitmap;
            } catch (MalformedURLException e) {
                Log.e("IMDbResults", "Error has occurred due to providing an invalid URL");
                progressDialog.dismiss();
                Snackbar.make(text, getString(R.string.can_not_load_image_message),Snackbar.LENGTH_LONG).show();
            } catch (IOException e) {
                Log.e("IMDbResults", "Error has occurred while trying to establish Http Connection");
                progressDialog.dismiss();
                Snackbar.make(text, getString(R.string.can_not_load_image_message),Snackbar.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) { //show returned Bitmap using ImageView
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
            progressDialog.dismiss();
        }
    }
}