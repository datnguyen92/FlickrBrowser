package com.android.sample.flickrbrowser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.sample.flickrbrowser.utils.FlickrUtils;
import com.android.sample.flickrbrowser.utils.NetworkUtils;
import com.android.sample.flickrbrowser.utils.Utils;
import com.dd.processbutton.iml.ActionProcessButton;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.uploader.UploadMetaData;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import butterknife.Bind;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class UploadActivity extends BaseActivity {

    // variables
    String photoPath = "";
    Context context;
    boolean isUploading=false;
    final String LOG_TAG = UploadActivity.class.getSimpleName();

    // Activity's elements
    @Bind(R.id.ivPhoto) ImageView ivPhoto;
    @Bind(R.id.metTitle) MaterialEditText metTitle;
    @Bind(R.id.metDescription) MaterialEditText metDescription;
    @Bind(R.id.metTags) MaterialEditText metTags;
    @Bind(R.id.btnUpload) ActionProcessButton btnUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up action bar
        setTitle("UPLOAD PHOTO");
        displayBackButton();

        Bundle extras = getIntent().getExtras();
        if (extras!=null) {
            photoPath = extras.getString("path");
        }

        context = this;

        // This is the photo path
        // either come from the camera or the gallery
        if (!photoPath.equals("")) {
            ivPhoto.setImageURI(Uri.parse(photoPath));
        }

        metTitle.setText(Utils.getFileNameFromPath(photoPath));
        // Button to handle upload photo to Flickr
        // Call AsyncTask to void UI freeze
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check network availability before upload image
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    showNetworkNotice();
                    return;
                }
                // Dismiss action due to empty photo path
                if (photoPath.equals("")) {
                    return;
                }
                new UploadPhotoTask().execute(photoPath,
                        metTitle.getText().toString(),
                        metDescription.getText().toString(),
                        metTags.getText().toString());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                // go to previous activity
                onBackPressed();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_upload;
    }

    @Override
    protected void onConnectedTask() {

    }

    @Override
    protected void onDisconnectedTask() {

    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("result", "0");
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }

    void toggleInputs() {
        metTitle.setEnabled(!isUploading);
        metDescription.setEnabled(!isUploading);
        metTags.setEnabled(!isUploading);
        btnUpload.setClickable(!isUploading);
    }

    class UploadPhotoTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isUploading = true;
            btnUpload.setProgress(1);
            toggleInputs();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String result =s;
            btnUpload.setProgress(0);
            isUploading = false;
            toggleInputs();

            if (result==null || result.equals("")) {
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Upload Error")
                        .setContentText("Failed to upload photo. Please try again later")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        })
                        .show();
                return;
            }

            Toast.makeText(context, "Photo is successfully uploaded", Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("result", "1");
            setResult(RESULT_OK, resultIntent);
            finish();
        }

        @Override
        protected String doInBackground(String... params) {
            String path = params[0],
            title = params[1],
            description = params[2],
            tags = params[3];

            path = path.substring(path.indexOf("/"), path.length());

            try {
                SharedPreferences sp = getSharedPreferences(FlickrUtils.PREFS_NAME, MODE_PRIVATE);
                String oauth_token = sp.getString(FlickrUtils.KEY_OAUTH_TOKEN, "");
                String token_secret = sp.getString(FlickrUtils.KEY_TOKEN_SECRET, "");

                Flickr f = FlickrUtils.getFlickrAuthed(oauth_token, token_secret);

                // Photo's metadata: title, description and tags
                UploadMetaData uploadMetaData = new UploadMetaData();
                uploadMetaData.setTitle(title);
                uploadMetaData.setDescription(description);
                if (!tags.equals("")) {
                    ArrayList<String> tagList = new ArrayList<>();
                    for (String tag:tags.split(" ")) {
                        tagList.add(tag);
                    }
                    uploadMetaData.setTags(tagList);
                }

                // Get file from path
                File file = new File(path);

                return f.getUploader().upload(file.getName(),
                        new FileInputStream(file), uploadMetaData);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }

            return null;
        }
    }

}
