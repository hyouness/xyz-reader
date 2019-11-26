package com.example.xyzreader.adapter;

import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xyzreader.R;

import java.util.ArrayList;
import java.util.List;

public class ArticleBodyAdapter extends RecyclerView.Adapter<ArticleBodyAdapter.ViewHolder> {

    private static Typeface rosarioReg;
    private List<String> paragraphs = new ArrayList<>();


    public ArticleBodyAdapter(Typeface rosarioReg) {
        ArticleBodyAdapter.rosarioReg = rosarioReg;
    }

    public ArrayList<String> getParagraphs() {
        return (ArrayList<String>) paragraphs;
    }

    public void setParagraphs(List<String> paragraphs) {
        this.paragraphs = paragraphs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_article_body, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(paragraphs.get(position));
    }

    @Override
    public int getItemCount() {
        return paragraphs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView subtitleView;

        ViewHolder(View view) {
            super(view);
            subtitleView = view.findViewById(R.id.article_paragraph);
            subtitleView.setTypeface(rosarioReg);
        }

        void bind(String paragraph) {
            subtitleView.setText(Html.fromHtml(paragraph));
        }
    }
}
