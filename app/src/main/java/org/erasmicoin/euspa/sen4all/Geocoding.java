package org.erasmicoin.euspa.sen4all;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Geocoding {

    private static OkHttpClient httpClient;

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public interface ResultCallback<R> {
        void onComplete(R result);
    }

    public static void geocode(String search, ResultCallback<ArrayList<GeocodingResult>> callback){

        String url = "https://nominatim.openstreetmap.org/search?format=json&polygon=1&addressdetails=1&q="+search;

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .build();

        okhttp3.Response response = null;

        ArrayList<GeocodingResult> result = new ArrayList<>();

        httpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (response.code() != 200) {
                    GeocodingResult gr = new GeocodingResult(false);
                    gr.setErrorMessage("HTTP STATUS "+response.code());
                    result.add(gr);
                } else {
                    try {

                        JSONArray networkResp = new JSONArray(response.body().string());

                        if (response.body() != null)
                            response.close();

                        for(int i = 0; i < networkResp.length(); i++){
                            GeocodingResult gr = new GeocodingResult(true);
                            JSONObject resp = networkResp.getJSONObject(i);
                            gr.setCoordinate(new Coordinate(resp.getDouble("lat"), resp.getDouble("lon")));
                            gr.setAddressType(resp.getString("addresstype"));
                            gr.setDisplayName(resp.getString("display_name"));
                            JSONArray bb = resp.getJSONArray("boundingbox");

                            ArrayList<Coordinate> boundingBox = new ArrayList<>();
                            boundingBox.add(new Coordinate(bb.getDouble(0), bb.getDouble(2)));
                            boundingBox.add(new Coordinate(bb.getDouble(1), bb.getDouble(3)));

                            gr.setBoundingBox(boundingBox);
                            result.add(gr);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        result.clear();
                        GeocodingResult gr = new GeocodingResult(false);
                        gr.setErrorMessage(e.getMessage());
                        result.add(gr);
                    }

                    handler.post(() -> {
                        callback.onComplete(result);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                result.clear();
                GeocodingResult gr = new GeocodingResult(false);
                gr.setErrorMessage(e.getMessage());
                result.add(gr);
                handler.post(() -> {
                    callback.onComplete(result);
                });
            }
        });
    }
}
