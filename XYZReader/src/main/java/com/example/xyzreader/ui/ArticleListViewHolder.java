package com.example.xyzreader.ui;

import android.text.Html;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.xyzreader.R;
import com.example.xyzreader.adapater.OnArticleClickListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ArticleListViewHolder extends RecyclerView.ViewHolder {
    private final OnArticleClickListener clickListener;

    @BindView(R.id.thumbnail)
    ImageView thumbnailView;

    @BindView(R.id.article_title)
    TextView titleView;

    @BindView(R.id.article_subtitle)
    TextView subtitleView;

    private static int measuredCellWidth = 0;

    public ArticleListViewHolder(View view, OnArticleClickListener clickListener) {
        super(view);
        this.clickListener = clickListener;
        ButterKnife.bind(this, view);
    }

    public void bind(String thumbnailUrl, String title, String subtitle) {

        titleView.setText(title);
        subtitleView.setText(Html.fromHtml(subtitle));

        // Picasso was relied on instead of the outdated volley library
        // Picasso has the advantage of resizing the imageview based on its aspect ratio when resizing with 0 as height
        // Also, a number of classes can be refactored out of the codebase i.e. DynamicHeightNetworkImageView and ImageLoaderHelper
        final RequestCreator requestCreator = Picasso.get()
                .load(thumbnailUrl);
        if (measuredCellWidth == 0) {
            thumbnailView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    measuredCellWidth = thumbnailView.getMeasuredWidth();
                    thumbnailView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    requestCreator
                            .resize(measuredCellWidth, 0)
                            .into(thumbnailView);
                }
            });
        } else {
            requestCreator
                    .resize(measuredCellWidth, 0)
                    .into(thumbnailView);
        }
    }

    @OnClick
    void onClick(View view) {
        clickListener.onArticleClick(getAdapterPosition());
    }
}