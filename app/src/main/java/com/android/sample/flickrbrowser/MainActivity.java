package com.android.sample.flickrbrowser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.android.sample.flickrbrowser.adapters.PhotoListAdapter;
import com.android.sample.flickrbrowser.models.Photo;
import com.android.sample.flickrbrowser.utils.FlickrUtils;
import com.android.sample.flickrbrowser.utils.Utils;
import com.android.sample.flickrbrowser.views.Fab;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.Parameter;
import com.googlecode.flickrjandroid.oauth.OAuthUtils;
import com.googlecode.flickrjandroid.people.User;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends BaseActivity
        implements SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener,
        RecyclerView.OnItemTouchListener {
    final String LOG_TAG = MainActivity.class.getSimpleName();
    final int REQUEST_TAKE_PHOTO = 101;
    final int REQUEST_PICK_PHOTO = 102;

    User user = null;
    Context context;
    Activity activity;
    MaterialSheetFab materialSheetFab;
    SwipeRefreshLayout swipeRefreshLayout;
    DilatingDotsProgressBar progress;
    RecyclerView photoListView;
    PhotoListAdapter mAdapter;
    ArrayList<Photo> photoList;
    CardView btnCamera, btnGallery;

    Fab mFab;
    String oauthToken="", mCurrentPhotoPath="";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        activity = this;

        //Set activity title
        setTitle("PHOTO COLLECTION");

        /*
        * Init activity elements
        *    - Floating Action Button - mFab
        *    - RecyclerView - photoListView
        *    - User profile button
        *    - Progress bar
        *    - Refresh layout
        *    - Action button sheets
         */
        // Initialize material sheet FAB
        mFab = (Fab) findViewById(R.id.custom_fab);
        View sheetView = findViewById(R.id.fab_sheet);
        View overlay = findViewById(R.id.overlay);
        int sheetColor = getResources().getColor(R.color.colorPrimary);
        int fabColor = getResources().getColor(R.color.colorPrimary);
        materialSheetFab = new MaterialSheetFab<>(mFab, sheetView, overlay,
                sheetColor, fabColor);

        // Init Photo list view and adapter
        photoListView = (RecyclerView) findViewById(R.id.photoList);
        photoListView.setLayoutManager(new LinearLayoutManager(context));
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        photoListView.setItemAnimator(itemAnimator);
        photoListView.addOnScrollListener(scrollListener);
        photoList = new ArrayList<>();

        //SwipeRefreshLayout set up
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        // progress indicator bar
        progress = (DilatingDotsProgressBar) findViewById(R.id.progress);
        progress.showNow();

        // Action button sheets
        btnCamera = (CardView) findViewById(R.id.btnCamera);
        btnGallery = (CardView) findViewById(R.id.btnGallery);
        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
    }

    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            // Hide floating action button when user scroll down and show when scrolling up
            if (dy > 0 && mFab.isShown())
                mFab.hide();
            else if (dy < 0 && !mFab.isShown())
                mFab.show();
        }
    };

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    public void onBackPressed() {
        if (materialSheetFab.isSheetVisible()) {
            materialSheetFab.hideSheet();
        } else {
            super.onBackPressed();
        }
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

        if (id == R.id.action_profile) {
            Toast.makeText(context, user.getUsername(), Toast.LENGTH_SHORT).show();
            SharedPreferences sp = getSharedPreferences(FlickrUtils.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(FlickrUtils.KEY_USER, "");
            editor.apply();
            finish();
            startActivity(new Intent(context, LoginActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                switch (resultCode) {
                    case RESULT_OK:
                        /*Bundle extras = data.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");*/
                        galleryAddPic();
                        Intent UploadActivity = new Intent(context, UploadActivity.class);
                        UploadActivity.putExtra("path", mCurrentPhotoPath);
                        startActivity(UploadActivity);
                        break;
                    case RESULT_CANCELED:
                        Toast.makeText(context, "Cancel photo capture", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;
            case REQUEST_PICK_PHOTO:
                switch (resultCode) {
                    case RESULT_OK:
                        Uri fileUri = data.getData();
                        mCurrentPhotoPath = Utils.getPathFromUri(fileUri, context);
                        Intent UploadActivity = new Intent(context, UploadActivity.class);
                        UploadActivity.putExtra("path", mCurrentPhotoPath);
                        startActivity(UploadActivity);
                        break;
                    case RESULT_CANCELED:
                        Toast.makeText(context, "No photo is selected", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Get current login user from shared preference
        SharedPreferences sp = getSharedPreferences(FlickrUtils.PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(FlickrUtils.KEY_USER, null);
        oauthToken = sp.getString(FlickrUtils.KEY_OAUTH_TOKEN, "");
        if (json!=null && !json.equals("")){
            user = gson.fromJson(json, User.class);
        }

        // If no user logged in, redirect to login activity
        if (user == null) {
            finish();
            Intent LoginActivity = new Intent(context, LoginActivity.class);
            startActivity(LoginActivity);
            return;
        }

        // User already logged in, get user's photos
        //Toast.makeText(context, "Welcome, " + user.getUsername() + " - " + user.getId(), Toast.LENGTH_SHORT).show();
        this.onRefresh();
    }

    @Override
    public void onRefresh() {
        new GetUserPhotoTask(oauthToken, "1").execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCamera:
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    Toast.makeText(context, "Camera is inaccessible", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }

                    materialSheetFab.hideSheetThenFab();
                }
                break;

            case R.id.btnGallery:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                photoPickerIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivityForResult(photoPickerIntent, REQUEST_PICK_PHOTO);
                break;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        // Indicate item position
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        int position = rv.getChildAdapterPosition(child);

        // Open photo viewer
        Gson gson = new Gson();
        String photoJson = gson.toJson(photoList.get(position));
        Intent PhotoViewActivity = new Intent(context, PhotoViewActivity.class);
        PhotoViewActivity.putExtra("photo", photoJson);
        startActivity(PhotoViewActivity);

        return false;
    }

    // Handle user's tap on List item
    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    /*
     * Create temporary image file
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "FLICKR_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    class GetUserPhotoTask extends AsyncTask<Void, Void, String> {
        String oauth_token, page;

        GetUserPhotoTask(String oauth_token, String page) {
            this.oauth_token = oauth_token;
            this.page = page;
            if (page.equals("1")) {
                photoList = new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(String signature) {
            super.onPostExecute(signature);

            if (signature==null || signature.equals("")) {
                return;
            }

            // Build url string to get photo from logged in user
            Uri requestUri = Uri.parse(FlickrUtils.FLICKR_BASE_URL).buildUpon()
                    .appendQueryParameter("oauth_consumer_key", FlickrUtils.FLICKR_API_KEY)
                    .appendQueryParameter("auth_token", oauth_token)
                    .appendQueryParameter("oauth_signature", signature)
                    .appendQueryParameter("user_id", user.getId())
                    .appendQueryParameter("method", "flickr.people.getPhotos")
                    .appendQueryParameter("per_page", "10")
                    .appendQueryParameter("page", "1")
                    .appendQueryParameter("nojsoncallback", page)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("privacy_filter", "5")
                    .appendQueryParameter("extras", "owner_name, description, date_upload, date_taken, original_format, tags, machine_tags, views, url_m, url_l, url_s, url_n, url_z, url_c, url_o")
                    .build();

            JsonRequest request = new JsonObjectRequest(Request.Method.GET, requestUri.toString(), null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // Display all the photos received from the api
                    displayPhoto(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Error Response", error.getMessage());
                }
            });

            // Create a request queue and add the request to it
            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(request);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                // Retrieve oauth signature from Flickr service
                ArrayList parameters = new ArrayList();
                parameters.add(new Parameter("oauth_callback", FlickrUtils.OAUTH_CALLBACK_URI));
                parameters.add(new Parameter("oauth_consumer_key", FlickrUtils.FLICKR_API_KEY));
                String signature = OAuthUtils.getSignature("GET",
                        "http://www.flickr.com/services/oauth/request_token",
                        parameters, FlickrUtils.FLICKR_SECRET_KEY, null);
                return signature;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            return null;
        }

        void displayPhoto(JSONObject jsonObject) {
            try {
                JSONObject json = (JSONObject) jsonObject.get("photos");
                JSONArray photos = json.getJSONArray("photo");
                for (int i = 0; i<photos.length();i++) {
                    JSONObject photoJson = (JSONObject) photos.get(i);
                    Photo photo = new Photo(photoJson);
                    photoList.add(photo);
                }

                mAdapter = new PhotoListAdapter(photoList, context);
                photoListView.setAdapter(mAdapter);

                swipeRefreshLayout.setRefreshing(false);
                progress.hideNow();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }
}
