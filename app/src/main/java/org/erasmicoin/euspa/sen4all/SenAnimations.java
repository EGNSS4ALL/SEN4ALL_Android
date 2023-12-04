package org.erasmicoin.euspa.sen4all;

import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class SenAnimations {

    public interface AnimationEndListener{
        void onAnimationEnd();
    }

    public static void expandVertical(final View v, AnimationEndListener listener) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        //a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(200);

        a.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                if(listener != null)
                    listener.onAnimationEnd();
            }
        });

        v.startAnimation(a);
    }

    public static void expandHorizontal(final View v, AnimationEndListener listener) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        //v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        v.measure(wrapContentMeasureSpec, wrapContentMeasureSpec);
        final int targetWidth = v.getMeasuredWidth();
        final int initialWidth = v.getMeasuredHeight();

        //Inizio dalla dimensione dell'altezza altrimenti si vedono i separator uscire
        v.getLayoutParams().width = initialWidth;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int nextWidth = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : Math.max((int)(targetWidth * interpolatedTime),initialWidth);
                Log.d("anim", String.valueOf(nextWidth));
                v.getLayoutParams().width = nextWidth;
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return false;
            }
        };

        // Expansion speed of 1dp/ms
        //a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setDuration(600);
        a.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                if(listener != null)
                    listener.onAnimationEnd();
            }
        });
        v.startAnimation(a);
    }

    public static void collapseVertical(final View v, AnimationEndListener listener) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        //a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));

        a.setDuration(200);
        a.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                if(listener != null)
                    listener.onAnimationEnd();
            }
        });
        v.startAnimation(a);
    }

    public static void collapseHorizontal(final View v, AnimationEndListener listener) {
        final int initialWidth = v.getMeasuredWidth();

        //Finisco alla dimensione dell'altezza altrimenti si vedono i separator uscire (è rotonda)
        final int maxReduction = v.getMeasuredHeight();


        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().width = Math.max(initialWidth - (int)(initialWidth * interpolatedTime),maxReduction);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return false;
            }
        };

        // Collapse speed of 1dp/ms
        //a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));

        a.setDuration(600);
        a.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                if(listener != null)
                    listener.onAnimationEnd();
            }
        });
        v.startAnimation(a);
    }

    public static void rotate(final View v, float degrees){
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.setRotation(degrees);
            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setDuration(1000);
        v.startAnimation(a);
    }

    public static void rotate2(final View v, float startDegrees, float stopDegrees, int duration){
        RotateAnimation rotate = new RotateAnimation(startDegrees, stopDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);


        rotate.setFillAfter(true);
        rotate.setDuration(duration);
        rotate.setRepeatCount(0);
        rotate.setInterpolator(new LinearInterpolator());
        v.setAnimation(rotate);
        v.startAnimation(rotate);
    }

    public static void fadeIn(final View v, int duration){
        //v.setAlpha(0);
        v.setVisibility(View.VISIBLE);
        AlphaAnimation alpha = new AlphaAnimation(0,1);
        alpha.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {

            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {

            }
        });
        alpha.setDuration(duration);
        v.setAnimation(alpha);
        v.startAnimation(alpha);
    }
    public static void fadeOut(final View v, int duration){
        AlphaAnimation alpha = new AlphaAnimation(1,0);
        alpha.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {

            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                v.setVisibility(View.INVISIBLE);
            }
        });
        alpha.setDuration(duration);
        v.setAnimation(alpha);
        v.startAnimation(alpha);
    }

}
