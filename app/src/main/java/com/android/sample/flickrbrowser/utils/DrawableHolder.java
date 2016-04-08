package com.android.sample.flickrbrowser.utils;

import android.graphics.drawable.Drawable;

/**
 * Created by datnguyen on 5/4/16.
 */
public class DrawableHolder {
    private static Drawable sDrawable;

    private DrawableHolder() {
        throw new AssertionError();
    }

    public static synchronized void set(Drawable drawable) {
        sDrawable = drawable;
    }

    public static synchronized Drawable get() {
        return sDrawable;
    }
}
