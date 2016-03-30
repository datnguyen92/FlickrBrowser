package com.android.sample.flikrbrowser;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

enum DownloadState {IDLE, PROCESSING, NOT_INITIALIZED, FAILED_OR_EMPTY, OK }
public class GetData {
    String LOG_TAG = GetData.class.getSimpleName();
    String mRawUrl;
    String mData;
    DownloadState mDownloadState;

    public GetData(String mRawUrl) {
        this.mRawUrl = mRawUrl;
        this.mDownloadState = DownloadState.IDLE;
    }

    public void reset() {
        this.mRawUrl = null;
        this.mData = null;
        this.mDownloadState = DownloadState.IDLE;
    }

    public String getmData() {
        return mData;
    }

    public DownloadState getmDownloadState() {
        return mDownloadState;
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String webData) {
            super.onPostExecute(webData);
            mData = webData;
            Log.d(LOG_TAG, "Data: " + webData);

            if (mData==null) {
                if (mRawUrl == null) {
                    mDownloadState = DownloadState.NOT_INITIALIZED;
                } else {
                    mDownloadState = DownloadState.FAILED_OR_EMPTY;
                }
            } else {
                mDownloadState = DownloadState.OK;
            }
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            if (params == null) return null;

            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream is = urlConnection.getInputStream();
                if (is == null) {
                    return null;
                }

                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(is));

                String line;

                while ((line=reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                return buffer.toString();

            } catch (IOException e) {
                Log.d(LOG_TAG, "Error", e);
                return null;
            } finally {
                if (urlConnection!=null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.d(LOG_TAG, "Error", e);
                    }
                }
            }
        }
    }
}
