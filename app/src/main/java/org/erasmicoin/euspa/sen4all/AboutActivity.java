package org.erasmicoin.euspa.sen4all;

import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        getWindow().setExitTransition(new Slide(Gravity.TOP));
        getWindow().setReenterTransition(new Slide(Gravity.TOP));
        getWindow().setEnterTransition(new Slide(Gravity.TOP));

        setContentView(R.layout.activity_info);

        findViewById(R.id.aboutClose).setOnClickListener(v -> this.backOneActivity());
    }

    private void backOneActivity(){
        finishAfterTransition();
    }
}
