package com.android.sample.flickrbrowser;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;

import com.mikepenz.iconics.context.IconicsLayoutInflater;

/**
 * Base activity with action bar/toolbar setup
 */
public class BaseActivity extends AppCompatActivity {
    Toolbar mToolbar;

    protected void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        super.onCreate(savedInstanceState);

    }

    @Override
    public void setContentView(int layoutResID) {
        CoordinatorLayout view = (CoordinatorLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout container = (FrameLayout) view.findViewById(R.id.base_content);
        getLayoutInflater().inflate(layoutResID, container, true);
        super.setContentView(view);
        if (mToolbar==null) {
            mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
            if (mToolbar!=null) {
                setSupportActionBar(mToolbar);
            }
        }
    }
}
