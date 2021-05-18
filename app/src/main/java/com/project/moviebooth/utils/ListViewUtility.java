package com.project.moviebooth.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ListViewUtility {
    public static void setListViewHeight(ListView listView) { //set height of the list view to match the sum of the heights of all list items in it
        ListAdapter listAdapter = listView.getAdapter();
        if(listAdapter == null) {
            return;
        }
        int height = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) { //add the measured height of each list item to the height variable
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(ViewGroup.LayoutParams.MATCH_PARENT, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = height + (listView.getDividerHeight() * (listAdapter.getCount() - 1)); //add divider height of each list item to the height variable
        listView.setLayoutParams(params); //set height of the list view
        listView.requestLayout();
    }
}