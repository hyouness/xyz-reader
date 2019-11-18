package com.example.xyzreader.remote;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RemoteEndpointUtil {
    private static final String TAG = "RemoteEndpointUtil";

    private RemoteEndpointUtil() {
    }

    public static JSONArray fetchJsonArray() {
        String itemsJson;
        try {
            itemsJson = fetchPlainText();
        } catch (IOException e) {
            Log.e(TAG, "Error fetching items JSON", e);
            return null;
        }

        // Parse JSON
        try {
            JSONTokener jsonTokener = new JSONTokener(itemsJson);
            Object val = jsonTokener.nextValue();
            if (!(val instanceof JSONArray)) {
                throw new JSONException("Expected JSONArray");
            }
            return (JSONArray) val;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing items JSON", e);
        }

        return null;
    }

    private static String fetchPlainText() throws IOException {
        OkHttpClient client = new OkHttpClient();

        try {
            TLSSocketFactory socketFactory = new TLSSocketFactory();
            X509TrustManager trustManager = socketFactory.getTrustManager();
            if (trustManager != null) {
                client = new OkHttpClient.Builder()
                        .sslSocketFactory(socketFactory, trustManager)
                        .build();
            }
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder()
                .url(Config.BASE_URL)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
