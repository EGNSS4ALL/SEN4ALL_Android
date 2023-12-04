package org.erasmicoin.euspa.sen4all;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class DownloadLegendHelper {
    private static OkHttpClient httpClient;

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public interface ResultCallback<R> {
        void onComplete(R result);

        void onFail();
    }

    public static void downloadImage(String url, ResultCallback<Bitmap> callback){


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

                    Bitmap resultImage = BitmapFactory.decodeStream(response.body().byteStream());

                    handler.post(() -> {
                        callback.onComplete(resultImage);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                handler.post(callback::onFail);
            }
        });
    }

}
