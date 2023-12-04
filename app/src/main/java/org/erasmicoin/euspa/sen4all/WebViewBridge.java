package org.erasmicoin.euspa.sen4all;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;

import java.text.DecimalFormat;

public class WebViewBridge {

    Activity mainActivity;
    public WebViewBridge(Activity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @JavascriptInterface
    public void showLoader(){
        mainActivity.runOnUiThread(() -> mainActivity.findViewById(R.id.loader).setVisibility(View.VISIBLE));
    }

    @JavascriptInterface
    public void hideLoader(){
        mainActivity.runOnUiThread(() -> mainActivity.findViewById(R.id.loader).setVisibility(View.GONE));
    }

    @JavascriptInterface
    public void setFeatureBoundingBox(String bbox){
        SessionVariables.writeFeatureBBox(bbox, mainActivity.getApplicationContext());
        ((MainActivity)mainActivity).saveTileNewIntent();
    }

    @JavascriptInterface
    public void setZoom(float zoomLevel){
        TextView zoomLevelTV = mainActivity.findViewById(R.id.zoomLevelTextView);
        DecimalFormat df = new DecimalFormat("0.##");
        String zoomStr = df.format(zoomLevel);
        mainActivity.runOnUiThread(() -> {
            zoomLevelTV.setText(mainActivity.getString(R.string.zoom_level, zoomStr));
        });
    }

    @JavascriptInterface
    public void setPixelSize(float pixelSize){
        TextView pixelSizeTV = mainActivity.findViewById(R.id.pixelSizeTextView);
        DecimalFormat df = new DecimalFormat("0.##");
        String pixelSizeStr = df.format(pixelSize);
        mainActivity.runOnUiThread(() -> {
            pixelSizeTV.setText(mainActivity.getString(R.string.pixel_size, pixelSizeStr));
        });
    }

    @JavascriptInterface
    public void loadLegendImage(String legendUrl){
        ImageView legendImage = mainActivity.findViewById(R.id.legendImage);

        DownloadLegendHelper.downloadImage(legendUrl, new DownloadLegendHelper.ResultCallback<Bitmap>() {
            @Override
            public void onComplete(Bitmap result) {
                Bitmap noBackgroundLegend = replaceIntervalColor(result, 250,255,250,255,250,255);
                result.recycle();
                mainActivity.runOnUiThread(() -> {
                    legendImage.setImageBitmap(noBackgroundLegend);
                    SenAnimations.fadeIn(legendImage, 200);
                    //legendImage.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onFail() {
                Log.e("WebViewBridge", "Cannot download legend image from "+legendUrl);
                AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                AlertDialog noInternetDialog = builder.create();
                noInternetDialog.setTitle(mainActivity.getString(R.string.error));
                noInternetDialog.setMessage(mainActivity.getString(R.string.noInternet));
                noInternetDialog.setButton(DialogInterface.BUTTON_NEUTRAL, mainActivity.getString(R.string.ok), (dialog, which) -> {
                    ((TextView)mainActivity.findViewById(R.id.layerSelectorButton)).setText(mainActivity.getString(R.string.select_a_layer));
                    ((TextView)mainActivity.findViewById(R.id.layerSelectorButton)).setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(mainActivity,R.drawable.icons_layersel_map), null, null, null);
                    SessionVariables.clearSession(mainActivity.getApplicationContext());
                    noInternetDialog.dismiss();
                });
                noInternetDialog.show();
            }
        });
    }

    public static Bitmap replaceIntervalColor(Bitmap bitmap, int redStart,int redEnd,int greenStart, int greenEnd,int blueStart, int blueEnd) {
        if (bitmap != null) {
            int picw = bitmap.getWidth();
            int pich = bitmap.getHeight();
            int[] pix = new int[picw * pich];
            bitmap.getPixels(pix, 0, picw, 0, 0, picw, pich);
            for (int y = 0; y < pich; y++) {
                for (int x = 0; x < picw; x++) {
                    int index = y * picw + x;
                    if (
                            ((Color.red(pix[index]) >= redStart)&&(Color.red(pix[index]) <= redEnd))&&
                                    ((Color.green(pix[index]) >= greenStart)&&(Color.green(pix[index]) <= greenEnd))&&
                                    ((Color.blue(pix[index]) >= blueStart)&&(Color.blue(pix[index]) <= blueEnd))
                    ){
                        pix[index] = Color.TRANSPARENT;
                    }
                }
            }
            return Bitmap.createBitmap(pix, picw, pich,Bitmap.Config.ARGB_8888);
        }
        return null;
    }
}
