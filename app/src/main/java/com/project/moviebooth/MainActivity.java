package com.project.moviebooth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.project.moviebooth.utils.GridViewUtility;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<CardView> cardViewArrayList = new ArrayList<>(); //ArrayList to store CardView objects related to the gridView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create new CardView objects with corresponding details
        cardViewArrayList.add(new CardView(R.drawable.ic_register_movie, getString(R.string.register_movie_btn_text), RegisterMovieActivity.class.getName()));
        cardViewArrayList.add(new CardView(R.drawable.ic_display_movies, getString(R.string.display_movies_btn_text), DisplayMoviesActivity.class.getName()));
        cardViewArrayList.add(new CardView(R.drawable.ic_favourites, getString(R.string.favourites_btn_text), FavouritesActivity.class.getName()));
        cardViewArrayList.add(new CardView(R.drawable.ic_edit_movies, getString(R.string.edit_movies_btn_text), EditMoviesActivity.class.getName()));
        cardViewArrayList.add(new CardView(R.drawable.ic_search, getString(R.string.search_btn_text), SearchActivity.class.getName()));
        cardViewArrayList.add(new CardView(R.drawable.ic_ratings, getString(R.string.ratings_btn_text), RatingsActivity.class.getName()));

        //create gridView with data available in cardViewArrayList
        GridView gridView = findViewById(R.id.grid_view);
        CustomAdapter adapter = new CustomAdapter();
        gridView.setAdapter(adapter);
        GridViewUtility.setGridViewHeight(gridView);
    }

    //CustomAdapter class to create the gridView with cardViewArrayList
    private class CustomAdapter extends ArrayAdapter<CardView> {
        public CustomAdapter() {
            super(MainActivity.this, R.layout.card_view_item, cardViewArrayList);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            View cardView = view;
            if (cardView == null) {
                LayoutInflater inflater = getLayoutInflater();
                cardView = inflater.inflate(R.layout.card_view_item, parent, false);
            }
            CardView cardViewItem = getItem(position);
            ImageView image = cardView.findViewById(R.id.image);
            image.setImageResource(cardViewItem.getImageId());
            Button button = cardView.findViewById(R.id.button);
            button.setText(cardViewItem.getButtonText());
            button.setOnClickListener(v -> { //set onClickListener for buttons in the gridView to start new activity when clicked
                Intent intent = null;
                try {
                    intent = new Intent(MainActivity.this, Class.forName(cardViewItem.getClassName()));
                } catch (ClassNotFoundException e) {
                    Log.e("MainActivity", String.format(getString(R.string.class_not_found_message), cardViewItem.getClassName()));
                }
                if (intent != null) {
                    startActivity(intent);
                }
            });
            return cardView;
        }
    }
}