package com.android.sample.flickrbrowser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.sample.flickrbrowser.utils.FlickrUtils;
import com.dd.processbutton.iml.ActionProcessButton;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.REST;
import com.googlecode.flickrjandroid.auth.Permission;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthInterface;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;

import java.net.URL;

import cn.pedant.SweetAlert.SweetAlertDialog;

/*
 * Steps need to be followed
 *        - Get the request token
 *        - Redirect to Flickr for user authentication
 *        - App callback to handle access token return from Flickr
 *        - Use the access token to retrieve private information such as user's photos
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    final String LOG_TAG = LoginActivity.class.getSimpleName();

    ActionProcessButton btnLogIn;
    Context context;
    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        activity = this;

        btnLogIn = (ActionProcessButton) findViewById(R.id.btnLogIn);
        if (btnLogIn!=null) {
            btnLogIn.setMode(ActionProcessButton.Mode.ENDLESS);
            btnLogIn.setOnClickListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * Handle callback after user successfully login to Flickr
         */
        Intent intent = activity.getIntent();
        String scheme = intent.getScheme();
        //The scheme should be same as the one in AndroidManifest file
        if (scheme != null && scheme.equals(FlickrUtils.APP_SCHEME)) {
            Toast.makeText(context, scheme, Toast.LENGTH_SHORT).show();
            Uri uri = intent.getData();
            new GetAccessToken().execute(uri);
        } else {
            completeLoading();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogIn:
                new GetRequestTokenTask().execute();
                break;
        }
    }

    /*
     * Function to stop button loading state
     */
    private void completeLoading() {
        btnLogIn.setProgress(0); //Stop button loading
        btnLogIn.setClickable(true);
    }

    /*
     * Function to start button loading state
     */
    private void startLoading() {
        btnLogIn.setProgress(1); //Stop button loading
        btnLogIn.setClickable(false);
    }

    /*
     * Save request token to shared preferences for later use
     */
    private void saveToken(OAuthToken oAuthToken, User user) {
        SharedPreferences sp = getSharedPreferences(FlickrUtils.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(FlickrUtils.KEY_OAUTH_TOKEN, oAuthToken.getOauthToken());
        editor.putString(FlickrUtils.KEY_TOKEN_SECRET, oAuthToken.getOauthTokenSecret());
        if (user!=null) {
            editor.putString(FlickrUtils.KEY_USER, new Gson().toJson(user));
        }
        editor.commit();
    }

    /*
     * Get token secret from shared preference
     */
    private String getTokenSecret() {
        SharedPreferences sp = getSharedPreferences(FlickrUtils.PREFS_NAME, Context.MODE_PRIVATE);

        return sp.getString(FlickrUtils.KEY_TOKEN_SECRET, null);
    }

    /*
     * Background task to get request token to redirect user to Flickr authentication page
     */
    private class GetRequestTokenTask extends AsyncTask<Void, Void, URL> {

        @Override
        protected void onPostExecute(URL url) {
            if (url != null) {
                //Redirect user to Flickr authentication page
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString())));
            } else {
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Login Error")
                        .setContentText("Failed to get request token. Please try again later")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                                completeLoading();
                            }
                        })
                        .show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startLoading();
        }

        @Override
        protected URL doInBackground(Void... params) {
            try {
                //Get access token and user from callback
                Flickr flickr = new Flickr(FlickrUtils.FLICKR_API_KEY, FlickrUtils.FLICKR_SECRET_KEY, new REST());
                //Get a request token from Flickr
                OAuthToken oAuthToken = flickr.getOAuthInterface().getRequestToken(FlickrUtils.OAUTH_CALLBACK_URI.toString());
                saveToken(oAuthToken, null);
                URL oauthURL = flickr.getOAuthInterface().buildAuthenticationUrl(Permission.WRITE, oAuthToken);

                return oauthURL;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            return null;
        }
    }

    /*
     * Background task to exchange request token for access token and token secret
     */
    private class GetAccessToken extends AsyncTask<Uri, Void, OAuth> {

        @Override
        protected void onPostExecute(OAuth oauth) {
            if (oauth != null) {
                completeLoading();
                saveToken(oauth.getToken(), oauth.getUser());
                activity.finish();
                Intent MainActivity = new Intent(context, MainActivity.class);
                startActivity(MainActivity);
            } else {
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Login Error")
                        .setContentText("Unable to get access token. Please try again later")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                                completeLoading();
                            }
                        })
                        .show();
            }
        }

        @Override
        protected OAuth doInBackground(Uri... params) {
            try {
                Uri uri = params[0];
                String query = uri.getQuery();
                String[] data = query.split("&");
                if (data != null && data.length == 2) {
                    String oauthToken = data[0].substring(data[0].indexOf("=") + 1);
                    String oauthVerifier = data[1].substring(data[1].indexOf("=") + 1);
                    String tokenSecret = getTokenSecret();
                    if (tokenSecret != null) {
                        Flickr flickr = new Flickr(FlickrUtils.FLICKR_API_KEY, FlickrUtils.FLICKR_SECRET_KEY, new REST());
                        OAuthInterface oAuthInterface = flickr.getOAuthInterface();
                        OAuth oAuth = oAuthInterface.getAccessToken(oauthToken, tokenSecret, oauthVerifier);
                        return oAuth;
                    }
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            return null;
        }
    }
}
