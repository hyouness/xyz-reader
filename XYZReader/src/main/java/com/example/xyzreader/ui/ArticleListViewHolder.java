package com.example.xyzreader.ui;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.xyzreader.R;
import com.example.xyzreader.adapater.OnArticleClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ArticleListViewHolder extends RecyclerView.ViewHolder {
    private final OnArticleClickListener clickListener;

    @BindView(R.id.thumbnail)
    DynamicHeightNetworkImageView thumbnailView;

    @BindView(R.id.article_title)
    TextView titleView;

    @BindView(R.id.article_subtitle)
    TextView subtitleView;

    public ArticleListViewHolder(View view, OnArticleClickListener clickListener) {
        super(view);
        this.clickListener = clickListener;
        ButterKnife.bind(this, view);
    }

    public void bind(String title, String subtitle, String thumbnailUrl, float aspectRatio, Context context) {
        titleView.setText(title);
        subtitleView.setText(Html.fromHtml(subtitle));
        thumbnailView.setImageUrl(thumbnailUrl,
                ImageLoaderHelper.getInstance(context).getImageLoader());
        thumbnailView.setAspectRatio(aspectRatio);
    }

    @OnClick
    void onClick(View view) {
        clickListener.onArticleClick(getAdapterPosition());
    }
}