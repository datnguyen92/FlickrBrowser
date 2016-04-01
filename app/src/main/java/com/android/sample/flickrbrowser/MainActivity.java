package com.android.sample.flickrbrowser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.sample.flickrbrowser.utils.FlickrUtils;
import com.android.sample.flickrbrowser.views.Fab;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.people.User;
import com.gordonwong.materialsheetfab.MaterialSheetFab;

public class MainActivity extends BaseActivity {
    User user = null;
    Context context;
    Activity activity;
    MaterialSheetFab materialSheetFab;
    RecyclerView photoListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        activity = this;

        //Set activity title
        setTitle("PHOTO COLLECTION");

        /*
        * Init activity components
        *    - Floating Action Button - mFab
        *    - RecyclerView - photoListView
        *    - User profile button
         */
        // Initialize material sheet FAB
        Fab mFab = (Fab) findViewById(R.id.custom_fab);
        View sheetView = findViewById(R.id.fab_sheet);
        View overlay = findViewById(R.id.overlay);
        int sheetColor = getResources().getColor(R.color.colorPrimary);
        int fabColor = getResources().getColor(R.color.colorPrimary);
        materialSheetFab = new MaterialSheetFab<>(mFab, sheetView, overlay,
                sheetColor, fabColor);

        // Init Photo list view and adapter
        photoListView = (RecyclerView) findViewById(R.id.photoList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_profile) {
            Toast.makeText(context, user.getUsername(), Toast.LENGTH_SHORT).show();
            SharedPreferences sp = getSharedPreferences(FlickrUtils.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(FlickrUtils.KEY_USER, "");
            editor.commit();
            finish();
            startActivity(new Intent(context, LoginActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Get current login user from shared preference
        SharedPreferences sp = getSharedPreferences(FlickrUtils.PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(FlickrUtils.KEY_USER, null);
        if (json!=null && !json.equals("")){
            user = gson.fromJson(json, User.class);
        }

        if (user != null) {
            Toast.makeText(context, "Welcome, " + user.getUsername(), Toast.LENGTH_SHORT).show();
            return;
        }

        finish();
        Intent LoginActivity = new Intent(context, LoginActivity.class);
        startActivity(LoginActivity);
    }

    @Override
    public void onBackPressed() {
        if (materialSheetFab.isSheetVisible()) {
            materialSheetFab.hideSheet();
        } else {
            super.onBackPressed();
        }
    }
}
