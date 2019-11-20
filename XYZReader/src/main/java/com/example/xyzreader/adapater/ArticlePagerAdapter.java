package com.example.xyzreader.adapater;

import android.database.Cursor;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.ui.ArticleDetailActivity;
import com.example.xyzreader.ui.ArticleDetailFragment;

public class ArticlePagerAdapter extends FragmentStatePagerAdapter {
    private ArticleDetailActivity articleDetailActivity;

    public ArticlePagerAdapter(ArticleDetailActivity articleDetailActivity, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.articleDetailActivity = articleDetailActivity;
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
        ArticleDetailFragment fragment = (ArticleDetailFragment) object;
        articleDetailActivity.setSelectedItemUpButtonFloor(fragment.getUpButtonFloor());
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        getCursor().moveToPosition(position);
        return ArticleDetailFragment.newInstance(getCursor().getLong(ArticleLoader.Query._ID));
    }

    @Override
    public int getCount() {
        return (getCursor() != null) ? getCursor().getCount() : 0;
    }

    private Cursor getCursor() {
        return articleDetailActivity.getCursor();
    }
}
