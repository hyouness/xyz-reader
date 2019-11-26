package com.example.xyzreader.ui;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.example.xyzreader.ui.ArticleDetailFragment.HTML_NEW_LINE;

class FetchArticleParagraphs extends AsyncTask<String, Void, List<String>> {
    private final WeakReference<ArticleDetailFragment> activityWeakReference;

    FetchArticleParagraphs(ArticleDetailFragment detailFragment) {
        activityWeakReference = new WeakReference<>(detailFragment);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activityWeakReference.get().showProgress();
    }

    @Override
    protected List<String> doInBackground(String... details) {
        int bodyLengthUpperBound = activityWeakReference.get().getPage() * 5000;

        List<String> paragraphs = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        String[] parags = details[0].split(HTML_NEW_LINE + HTML_NEW_LINE);
        for (String parag : parags) {
            if (stringBuilder.length() > bodyLengthUpperBound && !paragraphs.isEmpty()) {
                break;
            }
            stringBuilder.append(parag);
            paragraphs.add(parag);
        }

        return paragraphs;
    }

    @Override
    protected void onPostExecute(List<String> paragraphs) {
        super.onPostExecute(paragraphs);
        ArticleDetailFragment detailFragment = activityWeakReference.get();
        detailFragment.hideProgress();
        detailFragment.setParagraphs(paragraphs);
    }
}
