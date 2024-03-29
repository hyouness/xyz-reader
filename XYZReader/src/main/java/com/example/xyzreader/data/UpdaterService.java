package com.example.xyzreader.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.example.xyzreader.remote.RemoteEndpointUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.xyzreader.utils.AppConstants.BROADCAST_ACTION_STATE_CHANGE;
import static com.example.xyzreader.utils.AppConstants.EXTRA_ERROR;
import static com.example.xyzreader.utils.AppConstants.EXTRA_REFRESHING;

public class UpdaterService extends IntentService {
    private static final String TAG = "UpdaterService";


    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String error;
        Intent broadcastActionIntent = new Intent(BROADCAST_ACTION_STATE_CHANGE);

        if (isOffline()) {
            error = "Not online, not refreshing.";
            Log.w(TAG, error);
            getApplicationContext().sendStickyBroadcast(broadcastActionIntent.putExtra(EXTRA_REFRESHING, false).putExtra(EXTRA_ERROR, error));
            return;
        }

        getApplicationContext().sendStickyBroadcast(
                broadcastActionIntent.putExtra(EXTRA_REFRESHING, true));

        // Don't even inspect the intent, we only do one thing, and that's fetch content.
        ArrayList<ContentProviderOperation> cpo = new ArrayList<>();

        Uri dirUri = ItemsContract.Items.buildDirUri();

        // Delete all items
        cpo.add(ContentProviderOperation.newDelete(dirUri).build());

        try {
            JSONArray array = RemoteEndpointUtil.fetchJsonArray();
            if (array == null) {
                throw new JSONException("Invalid parsed item array" );
            }

            for (int i = 0; i < array.length(); i++) {
                ContentValues values = getContentValues(array, i);
                cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
            }

            getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpo);

        } catch (JSONException | RemoteException | OperationApplicationException e) {
            error = "Error updating content.";
            Log.e(TAG, error, e);
            broadcastActionIntent.putExtra(EXTRA_ERROR, error);
        }

        getApplicationContext().sendStickyBroadcast(
                broadcastActionIntent.putExtra(EXTRA_REFRESHING, false));
    }

    private static ContentValues getContentValues(JSONArray array, int i) throws JSONException {
        ContentValues values = new ContentValues();
        JSONObject object = array.getJSONObject(i);
        values.put(ItemsContract.Items.SERVER_ID, object.getString("id" ));
        values.put(ItemsContract.Items.AUTHOR, object.getString("author" ));
        values.put(ItemsContract.Items.TITLE, object.getString("title" ));
        values.put(ItemsContract.Items.BODY, object.getString("body" ));
        values.put(ItemsContract.Items.THUMB_URL, object.getString("thumb" ));
        values.put(ItemsContract.Items.PHOTO_URL, object.getString("photo" ));
        values.put(ItemsContract.Items.ASPECT_RATIO, object.getString("aspect_ratio" ));
        values.put(ItemsContract.Items.PUBLISHED_DATE, object.getString("published_date"));
        return values;
    }

    private boolean isOffline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni == null || !ni.isConnected();
    }
}
