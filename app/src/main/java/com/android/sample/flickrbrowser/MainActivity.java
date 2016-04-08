package com.android.sample.flickrbrowser;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.sample.flickrbrowser.adapters.PhotoListAdapter;
import com.android.sample.flickrbrowser.models.Gallery;
import com.android.sample.flickrbrowser.models.Photo;
import com.android.sample.flickrbrowser.ui.Fab;
import com.android.sample.flickrbrowser.utils.DrawableHolder;
import com.android.sample.flickrbrowser.utils.FlickrUtils;
import com.android.sample.flickrbrowser.utils.NetworkUtils;
import com.android.sample.flickrbrowser.utils.Utils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.dd.processbutton.iml.ActionProcessButton;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.Parameter;
import com.googlecode.flickrjandroid.oauth.OAuthUtils;
import com.googlecode.flickrjandroid.people.User;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;

public class MainActivity extends BaseActivity
        implements SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener {

    Context context;
    Activity activity;

    // Constants
    final String LOG_TAG = MainActivity.class.getSimpleName();
    final int REQUEST_TAKE_PHOTO = 101;
    final int REQUEST_PICK_PHOTO = 102;
    final int REQUEST_UPLOAD = 103;
    // Number of items remaining before reach the end of list
    // Trigger load more items before these items are shown
    final int VISIBLE_THRESHOLD = 3;

    //Layout reference
    @Bind(R.id.custom_fab) Fab mFab;
    @Bind(R.id.fab_sheet) View sheetView;
    @Bind(R.id.overlay) View overlay;
    @Bind(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.progress) DilatingDotsProgressBar progress;
    @Bind(R.id.photoList) RecyclerView photoListView;
    @Bind(R.id.btnCamera) CardView btnCamera;
    @Bind(R.id.btnGallery) CardView btnGallery;
    @Bind(R.id.btnRefresh) ActionProcessButton btnRefresh;
    @Bind(R.id.btnUpload) ActionProcessButton btnUpload;
    @Bind(R.id.lnEmptyView) LinearLayout lnEmptyView;

    // Variables
    MaterialSheetFab materialSheetFab;
    MenuItem mUserProfileIcon;
    User user = null;
    PhotoListAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    Gallery gallery;
    String oauthToken="", mCurrentPhotoPath="";
    int currPage = 1;
    boolean pendingIntroAnimation, isDataInit=false;
    boolean isFetchingData = false;
    int lastVisibleItem, visibleItemCount, totalItemCount;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prevent this trigger on screen orientation change
        if (savedInstanceState == null) {
            pendingIntroAnimation = true;
        }

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
        *    - Empty view
         */
        // Initialize material sheet FAB
        int sheetColor = getResources().getColor(R.color.colorPrimary);
        int fabColor = getResources().getColor(R.color.colorPrimary);
        materialSheetFab = new MaterialSheetFab<>(mFab, sheetView, overlay,
                sheetColor, fabColor);

        // Init Photo list view and adapter
        gallery = new Gallery();
        mLayoutManager = new LinearLayoutManager(context);
        photoListView.setLayoutManager(mLayoutManager);
        gallery = new Gallery();
        mAdapter = new PhotoListAdapter(gallery.getPhotoList(), context);
        photoListView.setAdapter(mAdapter);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        photoListView.setItemAnimator(itemAnimator);
        photoListView.addOnScrollListener(scrollListener);
        photoListView.addOnItemTouchListener(new RecyclerTouchListener(context, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Bundle options = null;
                if (Utils.HasAdroidL()) {
                    // Access view's ViewHolder
                    PhotoListAdapter.CustomViewHolder viewHolder = (PhotoListAdapter.CustomViewHolder) photoListView.getChildViewHolder(view);
                    Drawable drawable = viewHolder.ivPhoto.getDrawable();
                    // Save selected photo's drawable
                    DrawableHolder.set(drawable);
                    options = ActivityOptions.makeSceneTransitionAnimation(activity, viewHolder.ivPhoto, getString(R.string.photo_transition)).toBundle();
                }

                Photo photo = gallery.getPhotoList().get(position);

                Intent PhotoViewActivity = new Intent(context, PhotoViewActivity.class);
                Gson gson = new Gson();
                PhotoViewActivity.putExtra("photo", gson.toJson(photo));
                PhotoViewActivity.putExtra("gallery", gson.toJson(gallery));
                ActivityCompat.startActivity(activity, PhotoViewActivity, options);
            }
        }));

        //SwipeRefreshLayout set up
        swipeRefreshLayout.setOnRefreshListener(this);

        // progress indicator bar
        progress.showNow();

        // Action button sheets
        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);

        // Empty view
        btnRefresh.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (isDataInit) return;
        //Get current login user from shared preference
        SharedPreferences sp = getSharedPreferences(FlickrUtils.PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString(FlickrUtils.KEY_USER, null);
        oauthToken = sp.getString(FlickrUtils.KEY_OAUTH_TOKEN, "");
        if (json != null && !json.equals("")) {
            user = gson.fromJson(json, User.class);
        }

        // If no user logged in, redirect to login activity
        if (user == null) {
            activity.finish();
            startActivity(new Intent(context, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            return;
        }

        // User already logged in, get user's photos
        //Toast.makeText(context, "Welcome, " + user.getUsername() + " - " + user.getId(), Toast.LENGTH_SHORT).show();
        onRefresh();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    public void onBackPressed() {
        if (materialSheetFab.isSheetVisible()) {
            // Hide the float action sheet on back pressed
            materialSheetFab.hideSheet();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mUserProfileIcon = menu.findItem(R.id.action_profile);

        if (pendingIntroAnimation) {
            pendingIntroAnimation = false;
            startIntroAnimation();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_profile:
                showUserProfile();
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
                        galleryAddPhoto();
                        Intent UploadActivity = new Intent(context, UploadActivity.class);
                        UploadActivity.putExtra("path", mCurrentPhotoPath);
                        startActivityForResult(UploadActivity, REQUEST_UPLOAD);
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
                        startActivityForResult(UploadActivity, REQUEST_UPLOAD);
                        break;
                    case RESULT_CANCELED:
                        Toast.makeText(context, "No photo is selected", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_UPLOAD:
                switch (resultCode) {
                    case RESULT_OK:
                        String result=data.getStringExtra("result");
                        if (result.equals("1")) {
                            onRefresh();
                        } else if (result.equals("0")) {

                        }
                        break;
                }
        }

    }


    @Override
    protected void onConnectedTask() {
        //onRefresh();
    }

    @Override
    protected void onDisconnectedTask() {
        // If data is not loaded and network is not available, show up empty view
        toggleEmpty(!NetworkUtils.isNetworkAvailable(context) && !isDataInit);
    }

    @Override
    public void onRefresh() {
        toggleEmpty(!NetworkUtils.isNetworkAvailable(context) && !isDataInit);
        if (NetworkUtils.isNetworkAvailable(context)) {
            new GetUserPhotoTask(oauthToken, 1).execute();
        } else {
            showNetworkNotice();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // User choose capture new photo from built-in camera
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
            // User choose photos from system gallery
            case R.id.btnGallery:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                photoPickerIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivityForResult(photoPickerIntent, REQUEST_PICK_PHOTO);
                break;
            case R.id.btnRefresh:
                onRefresh();
                break;
            case R.id.btnUpload:
                materialSheetFab.showSheet();
                break;
        }
    }

    /*
     * Animate toolbar when being showed up
     */
    private void startIntroAnimation() {
        mFab.hideNow();
        int actionbarSize = Utils.dpToPx(56);
        getToolbar().setTranslationY(-actionbarSize);
        getToolbar().animate()
                .translationY(0)
                .setDuration(300)
                .setStartDelay(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startContentAnimation();
                    }
                })
                .start();
    }


    private void startContentAnimation() {
        mFab.show();
    }

    /*
     * Create temporary image file for saving image
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

    /*
     * Add captured photo into system Gallery library
     */
    private void galleryAddPhoto() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    /*
     * Show empty message if no data is available
     */
    void toggleEmpty(boolean showed) {
        if (showed) {
            //Set up empty gallery
            lnEmptyView.setVisibility(View.VISIBLE);
            photoListView.setVisibility(View.GONE);
            progress.hide();
        } else {
            lnEmptyView.setVisibility(View.GONE);
            photoListView.setVisibility(View.VISIBLE);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    /*
     * Show user profile dialog, which contain a logout button
     */
    void showUserProfile() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .btnStackedGravity(GravityEnum.CENTER)
                .forceStacking(true)
                .customView(R.layout.custom_dialog_view, true)
                .positiveText("Sign Out")
                .negativeText("Dismiss")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        SharedPreferences sp = getSharedPreferences(FlickrUtils.PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(FlickrUtils.KEY_USER, "");
                        editor.apply();
                        startActivity(new Intent(context, LoginActivity.class));
                        activity.finish();
                    }
                })
                .build();
        View customView = dialog.getCustomView();

        TextView tvRealName = (TextView) customView.findViewById(R.id.tvRealName);
        TextView tvUsername = (TextView) customView.findViewById(R.id.tvUsername);
        TextView tvEmail = (TextView) customView.findViewById(R.id.tvEmail);
        ImageView ivProfilePhoto = (ImageView) customView.findViewById(R.id.ivProfilePhoto);

        if (user!=null) {
            tvRealName.setText(user.getRealName());
            tvUsername.setText(user.getUsername());
            tvEmail.setText("");
        }

        dialog.show();
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

            if(dy > 0) { //check if user is scrolling down
                if (!isFetchingData) {
                    visibleItemCount = photoListView.getChildCount();
                    totalItemCount = mAdapter.getItemCount();
                    lastVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                    if ( (visibleItemCount + lastVisibleItem + VISIBLE_THRESHOLD) >= totalItemCount) {
                        new GetUserPhotoTask(oauthToken, currPage+1).execute();
                    }
                }
            }
        }
    };

    /*
     * Listener to detect user tap on list item
     */
    public interface ClickListener {
        void onClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private MainActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final MainActivity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    /*
     * AsyncTask to get current user's photos from Flickr
     * via predefined Flickr API
     */
    class GetUserPhotoTask extends AsyncTask<Void, Void, String> {
        String oauth_token;
        int page;

        GetUserPhotoTask(String oauth_token, int page) {
            this.oauth_token = oauth_token;
            this.page = page;
            currPage = page;
            if (page == 1) {
                gallery = new Gallery();
            } else {
                if (page <= gallery.getPageCount()) {
                    currPage = page;
                }
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
                    .appendQueryParameter("per_page", "3")
                    .appendQueryParameter("page", String.valueOf(page))
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("privacy_filter", "5")
                    .appendQueryParameter("extras", "owner_name, description, date_upload, date_taken, original_format, tags, machine_tags, views, url_m, url_l, url_s, url_n, url_z, url_c, url_o")
                    .build();

            // Retrieve json response from api server which contain all user's photo information
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
            if (!isDataInit) swipeRefreshLayout.setRefreshing(true);
            isFetchingData = true;
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

        /*
         * Display photos in RecyclerView with custom Adapter
         */
        void displayPhoto(JSONObject jsonObject) {
            try {
                if (!isDataInit) {
                    photoListView.setVisibility(View.VISIBLE);
                    photoListView.setTranslationY(photoListView.getHeight());
                }
                if (gallery.getPageCount() == 0) {
                    gallery = new Gallery(jsonObject);
                } else {
                    gallery.join(jsonObject);
                }
                mAdapter.setData(gallery.getPhotoList());
                mAdapter.notifyDataSetChanged();

                swipeRefreshLayout.setRefreshing(false);
                progress.hide();

                // Animate how the list view appear - from bottom to top
                if (!isDataInit)
                    photoListView.animate()
                        .translationY(0)
                        .setDuration(1000)
                        .setStartDelay(600)
                        .start();

                isDataInit=true;
                isFetchingData = false;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }
}
