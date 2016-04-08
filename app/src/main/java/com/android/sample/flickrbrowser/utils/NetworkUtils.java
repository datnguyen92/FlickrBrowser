package com.android.sample.flickrbrowser.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {
    //Network states
    public static int TYPE_WIFI=1;
    public static int TYPE_MOBILE=2;
    public static int TYPE_NOT_CONNECT=0;
    public static int TYPE_OTHER=3;

    public static boolean isConnected=false;

    //Determine current network state
    public static int getConnectionStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info!=null) {
            return info.getType()==ConnectivityManager.TYPE_WIFI?TYPE_WIFI:info.getType()==ConnectivityManager.TYPE_MOBILE?TYPE_MOBILE:TYPE_OTHER;
        }
        return TYPE_NOT_CONNECT;
    }

    //Get connection status
    public static boolean isNetworkAvailable(Context context) {
        return !(getConnectionStatus(context)==TYPE_NOT_CONNECT);
    }
}
