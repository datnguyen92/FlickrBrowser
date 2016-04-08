package com.android.sample.flickrbrowser.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.android.sample.flickrbrowser.utils.NetworkUtils;

/**
 * An indicator to detect network state change
 * Provide the function to be invoked when network is available
 */
public abstract class NetworkReceiver extends BroadcastReceiver {

    protected abstract void onConnected();
    protected abstract void onDisconnected();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            int status = NetworkUtils.getConnectionStatus(context);

            if (status==NetworkUtils.TYPE_NOT_CONNECT) {
                NetworkUtils.isConnected=false;
                onDisconnected();
            } else {
                NetworkUtils.isConnected=true;
                onConnected();
            }


        }
    }
}
