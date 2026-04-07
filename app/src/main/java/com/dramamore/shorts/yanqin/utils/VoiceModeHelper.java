package com.dramamore.shorts.yanqin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.bytedance.sdk.shortplay.api.ShortPlay;

import java.util.ArrayList;
import java.util.List;

public class VoiceModeHelper {
    public static final int MODE_ALL = 0;
    public static final int MODE_DUBBED = 1;
    public static final int MODE_ORIGINAL = 2;

    private static final String SP_NAME = "voice_mode_sp";
    private static final String KEY_MODE = "voice_mode";

    public static int getMode(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getInt(KEY_MODE, MODE_ALL);
    }

    public static void setMode(Context context, int mode) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putInt(KEY_MODE, mode).apply();
    }

    public static String getModeLabel(int mode) {
        switch (mode) {
            case MODE_DUBBED:
                return "配音";
            case MODE_ORIGINAL:
                return "原音";
            default:
                return "不限";
        }
    }

    public static boolean isOriginal(ShortPlay item) {
        if (item == null) return false;
        if (TextUtils.isEmpty(item.voiceLanguage) || TextUtils.isEmpty(item.originalLanguage)) {
            return false;
        }
        return item.voiceLanguage.equals(item.originalLanguage);
    }

    public static boolean isDubbed(ShortPlay item) {
        if (item == null) return false;
        if (TextUtils.isEmpty(item.voiceLanguage) || TextUtils.isEmpty(item.originalLanguage)) {
            return false;
        }
        return !item.voiceLanguage.equals(item.originalLanguage);
    }

    public static List<ShortPlay> filter(List<ShortPlay> source, int mode) {
        if (source == null || mode == MODE_ALL) {
            return source;
        }

        List<ShortPlay> result = new ArrayList<>();
        for (ShortPlay item : source) {
            if (item == null) continue;

            if (mode == MODE_ORIGINAL && isOriginal(item)) {
                result.add(item);
            } else if (mode == MODE_DUBBED && isDubbed(item)) {
                result.add(item);
            }
        }
        return result;
    }
}
