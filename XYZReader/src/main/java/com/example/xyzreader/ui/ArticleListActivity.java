package com.example.xyzreader.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.xyzreader.R;
import com.example.xyzreader.adapter.ArticleListAdapter;
import com.example.xyzreader.adapter.OnArticleClickListener;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.xyzreader.utils.AppConstants.BROADCAST_ACTION_STATE_CHANGE;
import static com.example.xyzreader.utils.AppConstants.EXTRA_ERROR;
import static com.example.xyzreader.utils.AppConstants.EXTRA_REFRESHING;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, OnArticleClickListener {

    private static final String RECYCLER_VIEW_POSITION_KEY = "recycler_view_position_key";

    @BindView(R.id.container_layout)
    CoordinatorLayout containerLayout;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.toolbar_container)
    AppBarLayout toolbarContainerView;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private Snackbar snackBar;

    private ArticleListAdapter adapter;

    private boolean mIsRefreshing = false;
    private int recyclerViewPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mToolbar.setOnClickListener(v -> mRecyclerView.smoothScrollToPosition(0));
        mSwipeRefreshLayout.setOnRefreshListener(this);
        LoaderManager.getInstance(this).initLoader(0, null, this);

        if (savedInstanceState == null) {
            onRefresh();
        } else {
            recyclerViewPosition = savedInstanceState.getInt(RECYCLER_VIEW_POSITION_KEY);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(RECYCLER_VIEW_POSITION_KEY, recyclerViewPosition);
    }

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                recyclerViewPosition = -1;
                mIsRefreshing = intent.getBooleanExtra(EXTRA_REFRESHING, false);
                updateRefreshingUI();
                checkForUpdaterServiceErrors(intent.getStringExtra(EXTRA_ERROR));
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    private void checkForUpdaterServiceErrors(String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }

        if (snackBar == null) {
            snackBar = Snackbar.make(containerLayout, message, Snackbar.LENGTH_LONG);
        }
        snackBar.show();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter = new ArticleListAdapter(cursor, this);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        if (recyclerViewPosition != -1)
            mRecyclerView.scrollToPosition(recyclerViewPosition);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    @Override
    public void onArticleClick(int position, View sharedView) {
        if (!mIsRefreshing) {
            this.recyclerViewPosition = position;
            long itemId = adapter.getItemId(this.recyclerViewPosition);
            Bundle optionsBundle = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Credit for finding a way to prevent status bar, appbar & navigation overlap : https://stackoverflow.com/q/43851489/5826864
                Pair<View, String> sharedStatusBar = Pair.create(findViewById(android.R.id.statusBarBackground), Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME);
                Pair<View, String> sharedImageView = Pair.create(sharedView, ViewCompat.getTransitionName(sharedView));
                Pair<View, String> sharedNavigationBar = Pair.create(findViewById(android.R.id.navigationBarBackground), Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
                Pair<View, String> sharedToolbarContainer = Pair.create(toolbarContainerView, "toolbar_container_transition");
                optionsBundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        sharedStatusBar,
                        sharedToolbarContainer,
                        sharedImageView,
                        sharedNavigationBar).toBundle();
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, ItemsContract.Items.buildItemUri(itemId));
            startActivity(intent, optionsBundle);
        }
    }

    @Override
    public void onRefresh() {
        startService(new Intent(this, UpdaterService.class));
    }
}
