package com.example.xyzreader.adapater;

import android.database.Cursor;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.ui.ArticleListViewHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.example.xyzreader.AppConstants.DATE_FORMAT;

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListViewHolder> {

    private SimpleDateFormat dateFormat;
    private SimpleDateFormat outputFormat;
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    private static final String TAG = ArticleListAdapter.class.toString();

    private Cursor mCursor;
    private final OnArticleClickListener clickListener;

    public ArticleListAdapter(Cursor cursor, OnArticleClickListener clickListener) {
        mCursor = cursor;
        this.clickListener = clickListener;
        outputFormat = new SimpleDateFormat();
        dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @NonNull
    @Override
    public ArticleListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_article, parent, false);
        return new ArticleListViewHolder(view, clickListener);
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    @Override
    public void onBindViewHolder(ArticleListViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String title = mCursor.getString(ArticleLoader.Query.TITLE);
        String thumbnailUrl = mCursor.getString(ArticleLoader.Query.THUMB_URL);
        holder.bind(thumbnailUrl, title, getSubtitle());
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    private String getSubtitle() {
        String subtitle;
        Date publishedDate = parsePublishedDate();
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            subtitle = DateUtils.getRelativeTimeSpanString(
                    publishedDate.getTime(),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString()
                    + "<br/>" + " by <b>"
                    + mCursor.getString(ArticleLoader.Query.AUTHOR) + "</b>";
        } else {
            subtitle = outputFormat.format(publishedDate)
                    + "<br/>" + " by <b>"
                    + mCursor.getString(ArticleLoader.Query.AUTHOR) + "</b>";
        }
        return subtitle;
    }
}