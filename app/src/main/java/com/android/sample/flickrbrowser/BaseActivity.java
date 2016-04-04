package com.android.sample.flickrbrowser;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Base activity with action bar/toolbar setup
 */
public abstract class BaseActivity extends AppCompatActivity {
    Toolbar mToolbar;

    protected void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set content view
        // Activity which extends BaseActivity needs to call getLayoutResource
        // Toolbar with id "toolbar" if needed
        setContentView(getLayoutResource());
        setupToolbar();
    }

    protected abstract int getLayoutResource();

    private void setupToolbar() {
        if (mToolbar==null) {
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            if (mToolbar!=null) {
                setSupportActionBar(mToolbar);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }
}
