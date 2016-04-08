package com.android.sample.flickrbrowser.ui;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;

import com.android.sample.flickrbrowser.R;
import com.gordonwong.materialsheetfab.AnimatedFab;

/**
 * Created by datnguyen on 1/4/16.
 */
public class Fab extends FloatingActionButton implements AnimatedFab {

    public Fab(Context context, AttributeSet attrs) {
        super(context, attrs);
        setImageDrawable(getResources().getDrawable(R.drawable.add));
    }

    /**
     * Shows the FAB.
     */
    @Override
    public void show() {
        show(0, 0);
    }

    /**
     * Shows the FAB and sets the FAB's translation.
     *
     * @param translationX translation X value
     * @param translationY translation Y value
     */
    @Override
    public void show(float translationX, float translationY) {
        // NOTE: Using the parameters is only needed if you want
        // to support moving the FAB around the screen.
        // NOTE: This immediately hides the FAB. An animation can
        // be used instead - see the sample app.
        // Set FAB's translation
        setTranslation(translationX, translationY);
        // Only use scale animation if FAB is hidden
        if (getVisibility() != View.VISIBLE) {
            // Pivots indicate where the animation begins from
            float pivotX = getPivotX() + translationX;
            float pivotY = getPivotY() + translationY;

            ScaleAnimation anim;
            // If pivots are 0, that means the FAB hasn't been drawn yet so just use the
            // center of the FAB
            if (pivotX == 0 || pivotY == 0) {
                anim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
            } else {
                anim = new ScaleAnimation(0, 1, 0, 1, pivotX, pivotY);
            }

            // Animate FAB expanding
            anim.setDuration(200);
            anim.setInterpolator(getInterpolator());
            startAnimation(anim);
        }
        setVisibility(View.VISIBLE);
    }

    /**
     * Hides the FAB.
     */
    @Override
    public void hide() {
        // NOTE: This immediately hides the FAB. An animation can
        // be used instead - see the sample app.
        // Only use scale animation if FAB is visible
        if (getVisibility() == View.VISIBLE) {
            // Pivots indicate where the animation begins from
            float pivotX = getPivotX() + getTranslationX();
            float pivotY = getPivotY() + getTranslationY();

            // Animate FAB shrinking
            ScaleAnimation anim = new ScaleAnimation(1, 0, 1, 0, pivotX, pivotY);
            anim.setDuration(200);
            anim.setInterpolator(getInterpolator());
            startAnimation(anim);
        }
        setVisibility(View.INVISIBLE);
    }

    /**
     * Hides the FAB without animation.
     */
    public void hideNow() {
        // This immediately hides the FAB
        setVisibility(View.INVISIBLE);
    }

    private void setTranslation(float translationX, float translationY) {
        animate().setInterpolator(getInterpolator()).setDuration(200)
                .translationX(translationX).translationY(translationY);
    }

    private Interpolator getInterpolator() {
        return AnimationUtils.loadInterpolator(getContext(), R.interpolator.msf_interpolator);
    }
}
