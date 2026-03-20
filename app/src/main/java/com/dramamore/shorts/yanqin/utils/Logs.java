package com.dramamore.shorts.yanqin.utils;

import android.util.Log;

public class Logs {
    public final static boolean DEBUG = true;

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
    }
}
