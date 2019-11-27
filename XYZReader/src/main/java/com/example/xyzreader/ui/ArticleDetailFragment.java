package com.example.xyzreader.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xyzreader.R;
import com.example.xyzreader.adapter.ArticleBodyAdapter;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.model.Article;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.example.xyzreader.utils.AppConstants.DATE_FORMAT;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment {

    private static final String TAG = "ArticleDetailFragment";
    private static final String ARG_ITEM_ID = "item_id";
    private static final int DEFAULT_MUTED_COLOR = 0xFF333333;
    static final String HTML_NEW_LINE = "<br />";
    private static final String ARTICLE_KEY = "article_key";
    private static final String PAGE_KEY = "page_key";
    private static final String ARTICLE_PARAGRAPHS_KEY = "article_paragraph_key";

    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private Unbinder unbinder;
    private Typeface rosarioReg;
    // credit: https://www.omnibus-type.com/fonts/rosario/
    private Typeface rosarioLight;
    private Article article;
    private ArticleBodyAdapter bodyAdapter;
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    @BindView(R.id.scrollview)
    NestedScrollView mScrollView;
    @BindView(R.id.photo)
    ImageView mPhotoView;
    @BindView(R.id.share_fab)
    FloatingActionButton fab;
    @BindView(R.id.article_title)
    TextView titleView;
    @BindView(R.id.article_byline)
    TextView bylineView;
    @BindView(R.id.pb_rv)
    ProgressBar progressBar;
    @BindView(R.id.article_body_rv)
    RecyclerView bodyViewRV;
    @BindView(R.id.meta_bar)
    LinearLayout metaBarLL;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private int page = 1;
    private boolean isLoading;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(ARG_ITEM_ID)) {
            mItemId = arguments.getLong(ARG_ITEM_ID);
        }

        if (savedInstanceState != null) {
            page = savedInstanceState.getInt(PAGE_KEY);
            article = savedInstanceState.getParcelable(ARTICLE_KEY);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        rosarioReg = Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf");
        rosarioLight = Typeface.createFromAsset(getResources().getAssets(), "Rosario-Light.ttf");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            loadArticleParagraphs();
        } else {
            List<String> savedParagraphs = savedInstanceState.getStringArrayList(ARTICLE_PARAGRAPHS_KEY);
            bodyAdapter.setParagraphs(savedParagraphs);
        }
    }

    private void loadArticleParagraphs() {
        if (bodyViewRV.getAdapter() != null)
            new FetchArticleParagraphs(this).execute(article.getBody());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        unbinder = ButterKnife.bind(this, mRootView);

        setupToolbar();

        fab.setOnClickListener(view -> {
            Intent intent = ShareCompat.IntentBuilder.from(getActivity())
                    .setType("text/plain")
                    .setText("Some sample text")
                    .getIntent();
            startActivity(Intent.createChooser(intent, getString(R.string.action_share)));
        });


        bindViews();
        toolbar.setTitle(article.getTitle());
        return mRootView;
    }

    private void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> activity.onBackPressed());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private Date parsePublishedDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        if (article == null)
            article = getArticleById();

        if (article != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);

            titleView.setText(article.getTitle());
            titleView.setTypeface(rosarioLight);

            bylineView.setTypeface(rosarioLight);
            bylineView.setText(Html.fromHtml(article.getByline()));
            bylineView.setMovementMethod(new LinkMovementMethod());

            bindPhotoView();

            bindArticleBodyRV();
        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A" );
        }
    }

    private void bindArticleBodyRV() {
        bodyAdapter = new ArticleBodyAdapter(rosarioReg);
        bodyViewRV.setAdapter(bodyAdapter);
        bodyViewRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        bodyViewRV.setLayoutManager(layoutManager);
        mScrollView.setOnScrollChangeListener(new PagingScrollListener() {
            @Override
            void loadMoreItems() {
                if (!isLoading && getSelectedItemId() == mItemId) {
                    incrementPage();
                    loadArticleParagraphs();
                }
            }
        });
    }

    private void bindPhotoView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPhotoView.setTransitionName(article.getTitle());
            mPhotoView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (mPhotoView != null) {
                        mPhotoView.getViewTreeObserver().removeOnPreDrawListener(this);
                        getActivity().startPostponedEnterTransition();
                        mPhotoView.setTransitionName(null);
                    }
                    return true;
                }
            });
        }
        Picasso.get().load(article.getPhotoUrl()).noFade().into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (bitmap != null) {
                    Palette p = Palette.from(bitmap).maximumColorCount(12).generate();
                    mMutedColor = p.getDarkMutedColor(DEFAULT_MUTED_COLOR);
                    mPhotoView.setImageBitmap(bitmap);
                    metaBarLL.setBackgroundColor(mMutedColor);
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {}

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        });
    }

    void hideProgress() {
        if (progressBar != null) {
            isLoading = false;
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    void showProgress() {
        if (progressBar != null) {
            isLoading = true;
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    void setParagraphs(List<String> paragraphs) {
        bodyAdapter.setParagraphs(paragraphs);
    }

    private void incrementPage() {
        page++;
    }

    int getPage() {
        return page;
    }

    private long getSelectedItemId() {
        return ((ArticleDetailActivity) getActivity()).getSelectedItemId();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE_KEY, page);
        outState.putParcelable(ARTICLE_KEY, article);
        outState.putStringArrayList(ARTICLE_PARAGRAPHS_KEY, bodyAdapter.getParagraphs());
    }

    private String getByline(String author, Date publishedDate) {
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            return DateUtils.getRelativeTimeSpanString(
                    publishedDate.getTime(),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString()
                    + " by <font color='#ffffff'>" + author + "</font>";

        }
        // If date is before 1902, just show the string
        return outputFormat.format(publishedDate) + " by <font color='#ffffff'>" + author + "</font>";
    }

    private Article getArticleById() {
        Uri itemsUri = ItemsContract.Items.buildDirUri();
        ContentResolver contentResolver = Objects.requireNonNull(getContext()).getContentResolver();
        Cursor cursor = contentResolver.query(itemsUri, null, ItemsContract.Items._ID + " = ?", new String[]{String.valueOf(mItemId)}, null);
        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndex(ItemsContract.Items.TITLE));
            String author = cursor.getString(cursor.getColumnIndex(ItemsContract.Items.AUTHOR));
            String date = cursor.getString(cursor.getColumnIndex(ItemsContract.Items.PUBLISHED_DATE));
            Date publishDate = parsePublishedDate(date);
            String byline = getByline(author, publishDate);
            String body = cursor.getString(cursor.getColumnIndex(ItemsContract.Items.BODY)).replaceAll("(\r\n)", HTML_NEW_LINE).replaceAll("(\n)", HTML_NEW_LINE);
            String photoUrl = cursor.getString(cursor.getColumnIndex(ItemsContract.Items.PHOTO_URL));
            cursor.close();
            return new Article(mItemId, title, byline, author, body, photoUrl, publishDate);
        }
        return null;
    }
}
