package com.example.dramasdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import com.bytedance.sdk.shortplay.api.ShortPlay;

import java.util.Locale;

public class DemoUtils {
    public static final String SP_FILE_NAME = "pssdk_demo";


    public static int dp2Px(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static String getDeviceLanguage() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        if (language.equalsIgnoreCase("zh")) {
            String script = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                script = locale.getScript();
            }

            if (!TextUtils.isEmpty(script)) {
                language = language + "_" + script;
            } else {
                language += "_hans";
            }
        }

        return language.toLowerCase(Locale.US);
    }
}
