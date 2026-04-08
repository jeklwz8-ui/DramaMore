package com.dramamore.shorts.yanqin.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.bytedance.sdk.shortplay.api.PSSDK;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class ContentLanguageHelper {
    public static final String DEFAULT_LANGUAGE = "zh_hans";
    private static final String LANGUAGE_ID = "id";
    private static final String LANGUAGE_IN = "in";

    private ContentLanguageHelper() {
    }

    public static void ensureDefaultContentLanguage() {
        List<String> languages = PSSDK.getContentLanguages();
        if (languages == null || languages.isEmpty()) {
            PSSDK.setContentLanguages(Collections.singletonList(DEFAULT_LANGUAGE));
        }
    }

    @NonNull
    public static List<String> getSelectedContentLanguages() {
        String language = getSelectedContentLanguage();
        return Collections.singletonList(language);
    }

    @NonNull
    public static String getSelectedContentLanguage() {
        List<String> languages = PSSDK.getContentLanguages();
        if (languages != null) {
            for (String language : languages) {
                String normalized = normalizeContentLanguage(language);
                if (!TextUtils.isEmpty(normalized)) {
                    return normalized;
                }
            }
        }
        return DEFAULT_LANGUAGE;
    }

    @NonNull
    public static String normalizeContentLanguage(String language) {
        if (TextUtils.isEmpty(language)) {
            return DEFAULT_LANGUAGE;
        }
        String safeLanguage = language.trim().toLowerCase(Locale.US);
        if (LANGUAGE_ID.equals(safeLanguage)) {
            return LANGUAGE_IN;
        }
        return safeLanguage;
    }
}
