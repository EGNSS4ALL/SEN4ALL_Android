package org.erasmicoin.euspa.sen4all;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class TileDownloader {
    private static OkHttpClient httpClient;

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public interface ResultCallback {
        void onComplete();

        void onFail(String message);
    }

    public static void downloadTile(String bbox, String dateRange, String tileLayerName, ParcelFileDescriptor pfd, Context ctx, ResultCallback callback){

        String url = "https://<your layer provider url>/";

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

        httpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (response.code() == 200) {



                    FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                    fileOutputStream.write(response.body().bytes());
                    fileOutputStream.close();

                    handler.post(() -> {
                        callback.onComplete();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                handler.post(() -> {
                    callback.onFail(e.getMessage());
                });
            }
        });
    }
}
