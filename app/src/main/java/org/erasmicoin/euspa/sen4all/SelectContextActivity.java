package org.erasmicoin.euspa.sen4all;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

public class SelectContextActivity extends AppCompatActivity {

    ImageView previousContext;
    ImageView nextContext;

    int newContext = 0;
    int oldContext = 0;

    Animation fadeIn;
    Animation fadeOut;

    ImageButton aboutButton;

    int animationDuration = 500;

    private boolean isAnimating = false;
    private ContextEnum currentContext;
    private LinearLayout contextButtonsContainer;
    private ScrollView contextScrollView;

    private static final Handler handler = new Handler(Looper.getMainLooper());

    private OnSwipeTouchListener gestureListener;

    private OnSwipeTouchListener buttonListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
        getWindow().setExitTransition(new Slide(Gravity.BOTTOM));

        setContentView(R.layout.activity_context_select);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        previousContext = findViewById(R.id.previousContext);
        nextContext = findViewById(R.id.nextContext);

        nextContext.setOnClickListener(v -> {
            if(!isAnimating)
                nextContext();
        });

        previousContext.setOnClickListener(v -> {
            if(!isAnimating)
                previousContext();
        });

        aboutButton = findViewById(R.id.about_button);
        aboutButton.setOnClickListener(v -> {
            gotoAbout();
        });


        findViewById(R.id.closeButton).setOnClickListener(v -> {
            gotoMainActivity();
        });

        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(animationDuration);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
             public void onAnimationStart(Animation animation) {
                 isAnimating = true;
             }
             public void onAnimationRepeat(Animation animation) {}
             public void onAnimationEnd(Animation animation) {
                 isAnimating = false;
             }
         });

        fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(animationDuration);

        currentContext = ContextEnum.ATMOSPHERE;

        initLayerButtons();

        gestureListener = new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                previousContext();
            }
            @Override
            public void onSwipeRight(){
                nextContext();
            }
        };

        getWindow().getDecorView().setOnTouchListener(gestureListener);

        /*findViewById(R.id.landContext).setOnTouchListener(gestureListener);
        findViewById(R.id.atmosphereContext).setOnTouchListener(gestureListener);
        findViewById(R.id.marineContext).setOnTouchListener(gestureListener);
        findViewById(R.id.includeContainer).setOnTouchListener(gestureListener);*/

        //handler.postDelayed(this::checkInitialContext, 200);
        checkInitialContext();


    }

    private void checkInitialContext(){
        ContextEnum savedContext = SessionVariables.readContext(this);
        String layerTag = SessionVariables.readLayer(this);
        //Previously selected a context
        if(savedContext != null && layerTag != null){
            currentContext = savedContext;
            oldContext = 0;
            newContext = ContextEnum.toInteger(currentContext);
            drawContext();
            for(int i=0; i<contextButtonsContainer.getChildCount(); i++){
                if(contextButtonsContainer.getChildAt(i) instanceof Button){
                    Button buttonObj = (Button) contextButtonsContainer.getChildAt(i);
                    String buttonTag = contextButtonsContainer.getChildAt(i).getTag().toString();
                    buttonTag = buttonTag.substring(buttonTag.indexOf("/")+1);
                    if(buttonTag.equals(layerTag)){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            runOnUiThread(() -> contextScrollView.scrollToDescendant(buttonObj));
                        }else{
                            runOnUiThread(() -> contextScrollView.scrollTo((int) buttonObj.getX(), (int) buttonObj.getY()));
                        }
                        runOnUiThread(() -> buttonObj.setPressed(true));

                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initLayerButtons() {

        contextScrollView = findViewById(currentContext.getIncludeId()); //(ScrollView) ((LinearLayout)findViewById(currentContext.getIncludeId())).getChildAt(0);
        contextButtonsContainer = (LinearLayout) contextScrollView.getChildAt(0);

        contextButtonsContainer.setOnTouchListener(gestureListener);

        for(int i=0; i<contextButtonsContainer.getChildCount(); i++){
            if(contextButtonsContainer.getChildAt(i) instanceof Button){
                int finalI = i;
               /* contextButtonsContainer.getChildAt(i).setOnClickListener(v -> {
                    SessionVariables.writeContext(currentContext, this);
                    String buttonTag = contextButtonsContainer.getChildAt(finalI).getTag().toString();
                    String layer = buttonTag.substring(buttonTag.indexOf("/")+1);
                    String satellite = buttonTag.substring(0, buttonTag.indexOf("/"));
                    String layerName = ((TextView)contextButtonsContainer.getChildAt(finalI)).getText().toString();

                    setLayer(layer, layerName, satellite);
                });*/
                contextButtonsContainer.getChildAt(i).setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()){
                    @Override
                    public void onSwipeLeft() {
                        previousContext();
                    }

                    @Override
                    public void onSwipeRight() {
                        nextContext();
                    }

                    @Override
                    public void onSelectButton(View v) {
                        SessionVariables.writeContext(currentContext, getApplicationContext());
                        String buttonTag = v.getTag().toString();
                        String layer = buttonTag.substring(buttonTag.indexOf("/")+1);
                        String satellite = buttonTag.substring(0, buttonTag.indexOf("/"));
                        String layerName = ((Button)v).getText().toString();
                        runOnUiThread(() -> {
                            v.setPressed(true);
                        });

                        setLayer(layer, layerName, satellite);
                    }
                });
            }
        }

        handler.postDelayed(()->{
            checkScrollable();
            contextScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                /*int maxScroll = ((ScrollView)v).getMaxScrollAmount();
                if(maxScroll == scrollY){
                    findViewById(R.id.moreAnimation).setRotation(180);
                }*/
                checkScrollable();
            });
        }, 200);

    }

    private void checkScrollable(){
        if(!contextScrollView.canScrollVertically(1)){
            runOnUiThread(() -> findViewById(R.id.moreAnimation).setVisibility(View.INVISIBLE));
        }else{
            runOnUiThread(() -> findViewById(R.id.moreAnimation).setVisibility(View.VISIBLE));
        }
    }

    private void setLayer(String layerTag, String layerName, String satellite){
        SessionVariables.writeLayer(layerTag, this);
        SessionVariables.writeLayerName(layerName, this);
        SessionVariables.writeSatellite(satellite, this);
        gotoMainActivity();
    }

    private void gotoMainActivity(){
        //startActivity(new Intent(this, MainActivity.class), ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        finishAfterTransition();
    }

    private void gotoAbout(){
        startActivity(new Intent(this, AboutActivity.class), ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    private void nextContext(){
        oldContext = newContext;
        newContext++;
        if(newContext > 2){
            newContext = 0;
        }
        drawContext();
    }

    private void previousContext(){
        oldContext = newContext;
        newContext--;
        if(newContext < 0){
            newContext = 2;
        }
        drawContext();
    }


    private void drawContext(){
        if(oldContext != newContext) {
            ContextEnum oldContextObj = ContextEnum.fromInteger(oldContext);
            ContextEnum newContextObj = ContextEnum.fromInteger(newContext);

            currentContext = newContextObj;

            assert newContextObj != null;
            findViewById(newContextObj.getIncludeId()).startAnimation(fadeIn);
            findViewById(newContextObj.getIncludeId()).setVisibility(View.VISIBLE);
            findViewById(newContextObj.getTextId()).startAnimation(fadeIn);
            findViewById(newContextObj.getTextId()).setVisibility(View.VISIBLE);
            findViewById(newContextObj.getIconId()).startAnimation(fadeIn);
            findViewById(newContextObj.getIconId()).setVisibility(View.VISIBLE);

            findViewById(oldContextObj.getIncludeId()).startAnimation(fadeOut);
            findViewById(oldContextObj.getIncludeId()).setVisibility(View.GONE);
            findViewById(oldContextObj.getTextId()).startAnimation(fadeOut);
            findViewById(oldContextObj.getTextId()).setVisibility(View.GONE);
            findViewById(oldContextObj.getIconId()).startAnimation(fadeOut);
            findViewById(oldContextObj.getIconId()).setVisibility(View.GONE);
        }
        initLayerButtons();



    }

}
