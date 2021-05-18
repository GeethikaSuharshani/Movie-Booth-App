package com.project.moviebooth.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;

public class GridViewUtility {
    public static void setGridViewHeight(GridView gridView) { //set height of the grid view to match the sum of the heights of all rows in it
        ListAdapter listAdapter = gridView.getAdapter();
        if(listAdapter == null) {
            return;
        }
        int height = 0;
        for (int i = 0; i < (listAdapter.getCount()/2); i++) { //add the measured height of each row to the height variable
            View listItem = listAdapter.getView(i * 2, null, gridView);
            listItem.measure(ViewGroup.LayoutParams.MATCH_PARENT, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = height + 15 * 6;  //add vertical spacing of each row to the height variable
        gridView.setLayoutParams(params); //set height of the grid view
        gridView.requestLayout();
    }
}
