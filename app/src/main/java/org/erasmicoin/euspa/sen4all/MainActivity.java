package org.erasmicoin.euspa.sen4all;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.location.LocationManagerCompat;

import android.os.Handler;
import android.os.Looper;

import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;


import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private static final int MY_LOCATION_PERMISSION_REQUEST_CODE = 11;
    public static final String TAG = "SEN4ALL";
    private WebView mapWebView;

    private TextView fab;

    private TextView zoomLevelTextView;

    private TextView selectedDateTextView;

    private ImageButton goBackOneDay;
    private ImageButton goForwardOneDay;

    private SimpleDateFormat dateFormatter;

    private DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
    private DateTimeFormatter fmtOl = DateTimeFormat.forPattern("yyyy-MM-dd");

    private Long selectedDate;

    private final int INITIAL_ZOOM_LEVEL = 12;
    private final int MIN_ZOOM_LEVEL = 9;
    private final int MAX_ZOOM_LEVEL = 14;


    private ImageButton zoomInButton;
    private ImageButton zoomOutButton;

    private ImageButton myLocationButton;

    private ImageButton drawPolyButton;

    private ImageButton drawCircle;
    private ImageButton drawSquare;
    private ImageButton drawTriangle;

    private SearchAutocomplete searchAutocomplete;

    private Location userLocation;

    private LinearLayout drawDrawer;

    private boolean drawEnabled = false;

    private ActivityResultLauncher<Intent> downloadTileActivityResultLauncher;

    private String dateRangeForOl;

    private LocationCallback initialMapLoadingLocationCallback;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setReenterTransition(new Slide(Gravity.TOP));
        getWindow().setEnterTransition(new Slide(Gravity.TOP));

        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mapWebView = findViewById(R.id.map_view);
        fab = findViewById(R.id.layerSelectorButton);

        WebSettings webViewSettings = mapWebView.getSettings();
        webViewSettings.setAllowFileAccessFromFileURLs(true);
        mapWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        zoomInButton = findViewById(R.id.zoomInButton);
        zoomOutButton = findViewById(R.id.zoomOutButton);

        zoomInButton.setOnClickListener(v -> {
            String setLayerJS = "zoomIn();";
            mapWebView.evaluateJavascript(setLayerJS, null);
        });

        zoomOutButton.setOnClickListener(v -> {
            String setLayerJS = "zoomOut();";
            mapWebView.evaluateJavascript(setLayerJS, null);
        });

        myLocationButton = findViewById(R.id.myLocationButton);
        myLocationButton.setOnClickListener(v -> {
            moveOnUserLocation();
        });

        drawDrawer = findViewById(R.id.drawDrawer);

        drawPolyButton = findViewById(R.id.toggleDraw);
        drawPolyButton.setOnClickListener(v -> {
            //toggleDrawExpanded();
            enableSquareDrawNoAni();
        });

        drawCircle = findViewById(R.id.drawCircle);
        drawSquare = findViewById(R.id.drawSquare);
        drawTriangle = findViewById(R.id.drawTriangle);

        drawCircle.setOnClickListener(v -> enableDrawWithAnimation(0));
        drawSquare.setOnClickListener(v -> enableDrawWithAnimation(1));
        drawTriangle.setOnClickListener(v -> enableDrawWithAnimation(2));

        searchAutocomplete = findViewById(R.id.textInputEditText_searchmap);

        searchAutocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
                //Object item = parent.getItemAtPosition(position);
                GeocodingResult result = searchAutocomplete.getResultAt(position);
                String setCenterJS = "setCenter("+result.coordinate.getLatitude()+","+result.coordinate.getLongitude()+");";
                mapWebView.evaluateJavascript(setCenterJS, null);
            }
        });

        zoomLevelTextView = findViewById(R.id.zoomLevelTextView);
        zoomLevelTextView.setText(getString(R.string.zoom_level, String.valueOf(INITIAL_ZOOM_LEVEL)));
        selectedDateTextView = findViewById(R.id.selectedDateTextView);

        goBackOneDay = findViewById(R.id.goBackOneDay);
        goForwardOneDay = findViewById(R.id.goForwardOneDay);
        //disabled at start because can't go forward from today
        goForwardOneDay.setEnabled(false);
        goForwardOneDay.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.icons_chevron_right_disabled));

        goBackOneDay.setOnClickListener(v -> {
            selectedDate = new DateTime(selectedDate).minusDays(1).getMillis();
            selectedDateTextView.setText(dateFormatter.format(selectedDate));
            //enable going forward again
            goForwardOneDay.setEnabled(true);
            goForwardOneDay.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.icons_chevron_right));
            changeLayerToSelected();
        });

        goForwardOneDay.setOnClickListener(v -> {
            DateTime newDate = new DateTime(selectedDate).minusDays(-1);
            if(!newDate.isAfterNow()){
                selectedDate = newDate.getMillis();
                selectedDateTextView.setText(dateFormatter.format(selectedDate));
                changeLayerToSelected();
            }
            DateTime newSelectedDate = new DateTime(selectedDate).withTimeAtStartOfDay();
            DateTime todayWithTimeReset = new DateTime().withTimeAtStartOfDay();
            if(newSelectedDate.equals(todayWithTimeReset)){
                //disable goingForward because can't go forward from today
                goForwardOneDay.setEnabled(false);
                goForwardOneDay.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.icons_chevron_right_disabled));
            }
        });

        selectedDateTextView.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.AppTheme_DatePickerDialog, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    DateTime newDate = fmt.parseDateTime(dayOfMonth+"/"+(month+1)+"/"+year);
                    selectedDate = newDate.getMillis();
                    selectedDateTextView.setText(dateFormatter.format(selectedDate));

                    DateTime newSelectedDate = new DateTime(selectedDate).withTimeAtStartOfDay();
                    DateTime todayWithTimeReset = new DateTime().withTimeAtStartOfDay();
                    if(newSelectedDate.equals(todayWithTimeReset)){
                        //disable goingForward because can't go forward from today
                        goForwardOneDay.setEnabled(false);
                        goForwardOneDay.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.icons_chevron_right_disabled));
                    }

                    changeLayerToSelected();
                }
            }, new DateTime(selectedDate).getYear(), new DateTime(selectedDate).getMonthOfYear()-1, new DateTime(selectedDate).getDayOfMonth());
            datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
            datePickerDialog.show();
        });

        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);

        selectedDate = new Date().getTime();

        selectedDateTextView.setText(dateFormatter.format(selectedDate));

        mapWebView.setWebViewClient(new WebViewClient());
        mapWebView.getSettings().setJavaScriptEnabled(true);
        WebViewBridge webViewBridge = new WebViewBridge(this);
        mapWebView.addJavascriptInterface(webViewBridge, "WebViewBridge");

        initialMapLoadingLocationCallback = new LocationCallback() {
            @Override
            public void gotLocation(Location location) {
                if(location != null) {
                    mapWebView.loadUrl("file:///android_asset/map.html?zoom=" + INITIAL_ZOOM_LEVEL + "&minZoom=" + MIN_ZOOM_LEVEL + "&maxZoom=" + MAX_ZOOM_LEVEL + "&lat=" + location.getLatitude() + "&lon=" + location.getLongitude());
                    Log.d(TAG, "LOAD1");
                }else {
                    mapWebView.loadUrl("file:///android_asset/map.html?zoom=" + INITIAL_ZOOM_LEVEL + "&minZoom=" + MIN_ZOOM_LEVEL + "&maxZoom=" + MAX_ZOOM_LEVEL + "&lat=&lon=");
                    Log.d(TAG, "LOAD2");
                }
            }

            @Override
            public void locationDisabled() {
                //showNoLocationDialog();
                mapWebView.loadUrl("file:///android_asset/map.html?zoom=" + INITIAL_ZOOM_LEVEL + "&minZoom=" + MIN_ZOOM_LEVEL + "&maxZoom=" + MAX_ZOOM_LEVEL + "&lat=&lon=");
                Log.d(TAG, "LOAD3");
            }
        };

        if(userLocation == null){
            getPermissionsAndUserLocation(initialMapLoadingLocationCallback);
        }else{
            Log.d(TAG, "LOAD4");
            mapWebView.loadUrl("file:///android_asset/map.html?zoom="+INITIAL_ZOOM_LEVEL+"&minZoom="+MIN_ZOOM_LEVEL+"&maxZoom="+MAX_ZOOM_LEVEL+"&lat="+userLocation.getLatitude()+"&lon="+userLocation.getLongitude());
        }

        ((TextView)findViewById(R.id.pixelSizeTextView)).setText(getString(R.string.pixel_size, "0"));

        fab.setOnClickListener(view -> gotoContextSelectionActivity());

        downloadTileActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri uri = null;
                        if (result.getData() != null) {
                            uri = result.getData().getData();
                            createTileFile(uri);
                        }
                    }
                });

        SessionVariables.clearSession(this);
        if(!haveNetworkConnection()){
            showNoInternetDialog();
        }
    }

    private void enableSquareDrawNoAni(){

        if(!drawEnabled){
            //drawPolyButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.icons_draw_square));
            drawPolyButton.getDrawable().setTint(getColor(R.color.primary));
            drawPolyButton.setBackground(AppCompatResources.getDrawable(this, R.drawable.buttons_fab_enabled));
            String setLayerJS = "enableDraw('square');";
            mapWebView.evaluateJavascript(setLayerJS, null);
            drawEnabled = true;
            isDrawDrawerExpanded = false;
        }else{
            drawPolyButton.setBackground(AppCompatResources.getDrawable(this, R.drawable.buttons_fab));
            drawPolyButton.getDrawable().setTint(getColor(R.color.dark));
            //drawPolyButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.icon_draw_btn_map));
            String setLayerJS = "disableDraw();";
            mapWebView.evaluateJavascript(setLayerJS, null);
            drawEnabled = false;
        }

    }

    private void enableDrawWithAnimation(int shape) {
        //0 circle 1 square 2 triangle
        switch(shape){
            case (0):{
                SenAnimations.collapseHorizontal(drawDrawer, () -> {
                    drawPolyButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.icons_draw_circle));
                    drawPolyButton.getDrawable().setTint(getColor(R.color.primary));
                    drawPolyButton.setBackground(AppCompatResources.getDrawable(this, R.drawable.buttons_fab_enabled));
                    String setLayerJS = "enableDraw('circle');";
                    mapWebView.evaluateJavascript(setLayerJS, null);
                    drawEnabled = true;
                });
                isDrawDrawerExpanded = false;
                break;
            }
            case (1):{
                SenAnimations.collapseHorizontal(drawDrawer, () -> {
                    drawPolyButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.icons_draw_square));
                    drawPolyButton.getDrawable().setTint(getColor(R.color.primary));
                    drawPolyButton.setBackground(AppCompatResources.getDrawable(this, R.drawable.buttons_fab_enabled));
                    String setLayerJS = "enableDraw('square');";
                    mapWebView.evaluateJavascript(setLayerJS, null);
                    drawEnabled = true;
                });
                isDrawDrawerExpanded = false;
                break;
            }
            case (2):{
                SenAnimations.collapseHorizontal(drawDrawer, () -> {
                    drawPolyButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.icons_draw_triangle));
                    drawPolyButton.getDrawable().setTint(getColor(R.color.primary));
                    drawPolyButton.setBackground(AppCompatResources.getDrawable(this, R.drawable.buttons_fab_enabled));
                    String setLayerJS = "enableDraw('triangle');";
                    mapWebView.evaluateJavascript(setLayerJS, null);
                    drawEnabled = true;
                });
                isDrawDrawerExpanded = false;
                break;
            }
        }
    }

    private boolean isDrawDrawerExpanded = false;

    private String selectedLayer;
    private boolean openedMenu = false;

    Handler changeLayerHandler = new Handler(Looper.getMainLooper());
    @Override
    protected void onResume() {
        super.onResume();
        if(!haveNetworkConnection()){
            showNoInternetDialog();
        }else{
            if(openedMenu){
                openedMenu = false;
                if(SessionVariables.readContext(this) != null && SessionVariables.readLayer(this) != null){
                    runOnUiThread(() -> SenAnimations.fadeOut(findViewById(R.id.legendImage), 200));
                    changeLayerToSelected();
                    runOnUiThread(() -> SenAnimations.fadeIn(findViewById(R.id.datePicker), 200));
                }

            }
        }
    }

    private void changeLayerToSelected(){
        selectedLayer = SessionVariables.readLayer(this);

        dateRangeForOl = new DateTime(selectedDate).minusDays(7).toString(fmtOl)+"/"+new DateTime(selectedDate).toString(fmtOl);
        if(!selectedLayer.isEmpty()){
            changeLayerHandler.postDelayed(() -> {
                String setLayerJS = "setLayer('" + selectedLayer + "','"+ dateRangeForOl+"');";
                mapWebView.evaluateJavascript(setLayerJS, null);
            },5);
            ContextEnum selectedContext = SessionVariables.readContext(this);
            String selectedLayerName = SessionVariables.readLayerName(this);
            Drawable icon = AppCompatResources.getDrawable(this, selectedContext.getIcon());
            fab.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            fab.setText(selectedLayerName);
            findViewById(R.id.toggleDraw).setVisibility(View.VISIBLE);
        }
    }

    private void gotoContextSelectionActivity(){
        openedMenu = true;
        Intent intent = new Intent(this, SelectContextActivity.class);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private interface LocationCallback{
        void gotLocation(Location location);
        void locationDisabled();
    };

    private void getPermissionsAndUserLocation(LocationCallback callback){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_LOCATION_PERMISSION_REQUEST_CODE);
        }else{
            getLocation(callback);
        }

    }

    @SuppressLint("MissingPermission")
    private void getLocation(LocationCallback callback){
        LocationManager mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if(LocationManagerCompat.isLocationEnabled(mLocationManager)){
            LocationManagerCompat.getCurrentLocation(mLocationManager, LocationManager.FUSED_PROVIDER, null, getMainExecutor(), location -> {
                callback.gotLocation(location);
            } );
        }else{
            callback.locationDisabled();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == MY_LOCATION_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getLocation(initialMapLoadingLocationCallback);
        }else{
            getPermissionsAndUserLocation(initialMapLoadingLocationCallback);
        }
    }

    private void moveOnUserLocation(){
        getLocation(new LocationCallback() {
            @Override
            public void gotLocation(Location location) {
                userLocation = location;
                String setCenterJS = "setCenter("+location.getLatitude()+","+location.getLongitude()+");";
                mapWebView.evaluateJavascript(setCenterJS, null);
            }

            @Override
            public void locationDisabled() {
                showNoLocationDialog();
            }
        });
    }

    private void showNoLocationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog noLocationDialog = builder.create();
        noLocationDialog.setTitle(getString(R.string.error));
        noLocationDialog.setMessage(getString(R.string.noLocation));
        noLocationDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.ok), (dialog, which) -> {
            noLocationDialog.dismiss();
        });
        noLocationDialog.show();
    }


    public void saveTileNewIntent(){


        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm").format(new Date());

        String filename = SessionVariables.readSatellite(this)+"_"+SessionVariables.readContext(this).getName()+"_"+SessionVariables.readLayerName(this)+"_"+timeStamp+".tif";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/tiff");
        intent.putExtra(Intent.EXTRA_TITLE, filename);

        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("/Documents"));
        downloadTileActivityResultLauncher.launch(intent);
    }

    private void createTileFile(Uri uri) {
        try {
            ParcelFileDescriptor pfd = getContentResolver().
                    openFileDescriptor(uri, "w");
            if (pfd != null) {
                TileDownloader.downloadTile(SessionVariables.readFeatureBBox(this), dateRangeForOl, SessionVariables.readLayer(this), pfd, this, new TileDownloader.ResultCallback() {
                    @Override
                    public void onComplete() {
                        try {
                            pfd.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onFail(String message) {
                        try {
                            pfd.closeWithError(message);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean haveNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            return true;
        } else {
            return false;
        }
    }

    private void showNoInternetDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog noInternetDialog = builder.create();
        noInternetDialog.setTitle(getString(R.string.error));
        noInternetDialog.setMessage(getString(R.string.noInternet));
        noInternetDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.ok), (dialog, which) -> {
            ((TextView)findViewById(R.id.layerSelectorButton)).setText(getString(R.string.select_a_layer));
            ((TextView)findViewById(R.id.layerSelectorButton)).setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this,R.drawable.icons_layersel_map), null, null, null);
            SessionVariables.clearSession(this);
            finish();
            noInternetDialog.dismiss();
        });
        noInternetDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapWebView.destroy();
    }
}