package com.android.sample.flickrbrowser;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.sample.flickrbrowser.utils.FlickrUtils;
import com.android.sample.flickrbrowser.utils.Utils;
import com.dd.processbutton.iml.ActionProcessButton;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.uploader.UploadMetaData;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class UploadActivity extends BaseActivity {

    // variables
    String photoPath = "";
    Context context;
    boolean isUploading=false;
    final String LOG_TAG = UploadActivity.class.getSimpleName();

    // Activity's elements
    ImageView ivPhoto;
    MaterialEditText metTitle, metDescription, metTags;
    ActionProcessButton btnUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("UPLOAD PHOTO");

        Bundle extras = getIntent().getExtras();
        if (extras!=null) {
            photoPath = extras.getString("path");
        }

        context = this;

        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        metTitle = (MaterialEditText) findViewById(R.id.metTitle);
        metDescription = (MaterialEditText) findViewById(R.id.metDescription);
        metTags = (MaterialEditText) findViewById(R.id.metTags);
        btnUpload = (ActionProcessButton) findViewById(R.id.btnUpload);

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
                Toast.makeText(context, "Upload pressed", Toast.LENGTH_SHORT).show();
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
    protected int getLayoutResource() {
        return R.layout.activity_upload;
    }

    class UploadPhotoTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isUploading = true;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            isUploading = false;
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
