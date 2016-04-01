package com.android.sample.flickrbrowser.utils;

import android.content.Context;

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
}