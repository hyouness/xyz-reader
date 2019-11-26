package com.example.xyzreader.ui;

import android.view.View;

import androidx.core.widget.NestedScrollView;

// Credit: https://androidfreetutorial.wordpress.com/2018/01/01/nested-scrollview-inside-recyclerview-not-triggering-onscrolllistener-when-scrolling-down-pagination-in-nestedscrollview-android/
abstract class PagingScrollListener implements NestedScrollView.OnScrollChangeListener {

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        int lastChildIndex = v.getChildCount() - 1;
        View lastChild = v.getChildAt(lastChildIndex);
        if (lastChild != null) {
            if ((scrollY >= (lastChild.getMeasuredHeight() - v.getMeasuredHeight())) && scrollY > oldScrollY) {
                System.out.println(String.format("scrollY: %s - lastChild MeasureHeight: %s - NestedScrollView MeasuredHeight: %s",
                        scrollY, lastChild.getMeasuredHeight(), v.getMeasuredHeight()));
                loadMoreItems();
            }
        }
    }

    abstract void loadMoreItems();
}

