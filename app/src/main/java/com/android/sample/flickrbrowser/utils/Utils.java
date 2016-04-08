package com.android.sample.flickrbrowser.utils;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by datnguyen on 30/3/16.
 */
public class Utils {
    public static void showErrorDialog(Context context, String title, String msg) {
        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(title.equals("")?"Error Message":title)
                .setContentText(msg)
                .show();
    }

    public static boolean HasAdroidL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static String timeAgoFrom(long millis) {
        // Calculate the different between target date time and current time in seconds
        long nowMillis = new Date().getTime();
        long diff = ((int)(nowMillis/1000) - millis);

        if (diff < 60) { // time ago in seconds
            return "Just now";
        } else if (diff < 60*60) { // time ago in minutes
            int min = (int) diff/(60);
            return String.valueOf(min) + (min==1?" minute":" minutes");
        } else if (diff < 60*60*24) { // time ago in hours
            int hour = (int) diff/(60*60);
            return String.valueOf(hour) + (hour==1?" hour":" hours");
        } else if (diff < 60*60*24*30) { // time ago in days
            int day = (int) diff/(60*60*24);
            return String.valueOf(day) + (day==1?" day":" days");
        } else if (diff < 60*60*24*365) { // time ago in months
            int month = (int) diff/(60*60*24);
            return String.valueOf(month) + (month==1?" month":" months");
        } else {
            int year = (int) diff/(60*60*24*365);
            return String.valueOf(year) + (year==1?" year":" years");
        }
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size=1024;
        try {
            byte[] bytes=new byte[buffer_size];
            for(;;) {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch(Exception e){
            Log.d("CopyStream", e.getMessage());
        }
    }

    public static String getFileNameFromPath(String path) {
        if (path.equals("")) return "";

        return path.substring(path.lastIndexOf("/")+1, path.lastIndexOf("."));
    }

    // Get Image path from provided Image Uri
    public static String getPathFromUri(Uri uri, Context context) {
        Uri filePathUri = uri;
        String path = "";
        try {
            if (uri.getScheme().compareTo("content")==0) {
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor.moveToFirst()) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
                    filePathUri = Uri.parse(cursor.getString(column_index));
                    path = filePathUri.getPath();
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.d("getPathFromUri", e.getMessage());
        }

        return path;
    }

    // Convert dp to pixels
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
