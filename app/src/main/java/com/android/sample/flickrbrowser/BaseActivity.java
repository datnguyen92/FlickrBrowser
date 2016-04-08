package com.android.sample.flickrbrowser;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.android.sample.flickrbrowser.receivers.NetworkReceiver;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Base activity with action bar/toolbar setup
 */
public abstract class BaseActivity extends AppCompatActivity {
    @Nullable @Bind(R.id.toolbar) Toolbar mToolbar;

    @Nullable @Bind(R.id.parent) CoordinatorLayout parentView;

    Snackbar snackbar;
    Context context;
    NetworkReceiver networkReceiver;

    protected abstract int getLayoutResource();
    protected abstract void onConnectedTask();
    protected abstract void onDisconnectedTask();

    protected void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    protected void displayBackButton() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set content view
        // Activity which extends BaseActivity needs to call getLayoutResource
        // Toolbar with id "toolbar" if needed
        setContentView(getLayoutResource());
        ButterKnife.bind(this);
        setupToolbar();

        context = this;

        // A bottom message to show internet missing warning to user
        if (parentView!=null) {
            snackbar = Snackbar.make(parentView, "Unable to connect to the Internet", Snackbar.LENGTH_INDEFINITE).
                    setAction("Dismiss", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (snackbar != null) {
                                snackbar.dismiss();
                            }
                        }
                    });
        }

        //Register network receiver to listen to network's state change
        networkReceiver = new NetworkReceiver() {
            @Override
            protected void onConnected() {
                hideNetworkNotice();
                onConnectedTask();
            }

            @Override
            protected void onDisconnected() {
                showNetworkNotice();
                onDisconnectedTask();
            }
        };

        registerReceiver(networkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //registerReceiver(networkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    private void setupToolbar() {
        if (mToolbar!=null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    void showNetworkNotice() {
        if (snackbar==null || snackbar.isShown()) return;
        snackbar.show();
    }

    void hideNetworkNotice() {
        if (snackbar==null || !snackbar.isShown()) return;
        snackbar.dismiss();
    }
}
