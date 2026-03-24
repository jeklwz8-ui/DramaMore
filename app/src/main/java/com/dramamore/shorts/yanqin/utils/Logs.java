package com.dramamore.shorts.yanqin.utils;

import android.util.Log;

import com.dramamore.shorts.yanqin.BuildConfig;

public class Logs {
    public final static boolean DEBUG = BuildConfig.DEBUG;

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
    }
}
