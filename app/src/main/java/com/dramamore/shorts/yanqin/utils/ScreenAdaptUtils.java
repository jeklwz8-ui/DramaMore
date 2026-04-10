package com.dramamore.shorts.yanqin.utils;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

public class ScreenAdaptUtils {

    private ScreenAdaptUtils() {
    }

    public static int calcGridSpanCount(@NonNull Context context, int minItemDp, int minSpan, int maxSpan) {
        Configuration configuration = context.getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp;
        if (screenWidthDp <= 0 || minItemDp <= 0) {
            return Math.max(minSpan, 1);
        }
        int span = screenWidthDp / minItemDp;
        if (span < minSpan) {
            span = minSpan;
        }
        if (span > maxSpan) {
            span = maxSpan;
        }
        return Math.max(span, 1);
    }
}
