package com.android.sample.flickrbrowser.utils;

import android.net.Uri;

/**
 * Created by datnguyen on 29/3/16.
 */
public class FlickrUtils {
    public static final String FLICKR_API_KEY = "2b8fc75daa47791bab6b59b31f032f2f";
    public static final String FLICKR_SECRET_KEY = "46249e7bd2b9e808";
    public static final String APP_SCHEME = "com-android-sample-flickrbrowser";
    public static final Uri OAUTH_CALLBACK_URI = Uri.parse(APP_SCHEME + "://oauth");
    public static final String PREFS_NAME = "com-android-sample-pref";
    public static final String KEY_OAUTH_TOKEN = "com-android-oauthToken";
    public static final String KEY_TOKEN_SECRET = "com-android-tokenSecret";
    public static final String KEY_USER = "com-android-user";


}