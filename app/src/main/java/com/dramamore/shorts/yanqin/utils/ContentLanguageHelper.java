package com.dramamore.shorts.yanqin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.sdk.shortplay.api.PSSDK;
import com.dramamore.shorts.yanqin.App;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class ContentLanguageHelper {
    public static final String DEFAULT_LANGUAGE = "zh_hans";
    private static final String LANGUAGE_ID = "id";
    private static final String LANGUAGE_IN = "in";
    private static final String SP_NAME = "content_language_pref";
    private static final String KEY_SELECTED_CONTENT_LANGUAGE = "selected_content_language";

    private ContentLanguageHelper() {
    }

    public static void ensureDefaultContentLanguage() {
        String selectedLanguage = resolveSelectedLanguageFromSdk();
        if (TextUtils.isEmpty(selectedLanguage)) {
            selectedLanguage = readSelectedLanguageFromLocal();
            if (TextUtils.isEmpty(selectedLanguage)) {
                selectedLanguage = DEFAULT_LANGUAGE;
            }
            PSSDK.setContentLanguages(Collections.singletonList(selectedLanguage));
        }
        persistSelectedLanguageToLocal(selectedLanguage);
    }

    @NonNull
    public static List<String> getSelectedContentLanguages() {
        String language = getSelectedContentLanguage();
        return Collections.singletonList(language);
    }

    @NonNull
    public static String getSelectedContentLanguage() {
        String selectedLanguage = resolveSelectedLanguageFromSdk();
        if (!TextUtils.isEmpty(selectedLanguage)) {
            persistSelectedLanguageToLocal(selectedLanguage);
            return selectedLanguage;
        }
        String localLanguage = readSelectedLanguageFromLocal();
        if (!TextUtils.isEmpty(localLanguage)) {
            return localLanguage;
        }
        return DEFAULT_LANGUAGE;
    }

    public static void setSelectedContentLanguages(@Nullable List<String> languages) {
        String targetLanguage = DEFAULT_LANGUAGE;
        if (languages != null) {
            for (String language : languages) {
                String normalized = normalizeContentLanguage(language);
                if (!TextUtils.isEmpty(normalized)) {
                    targetLanguage = normalized;
                    break;
                }
            }
        }
        PSSDK.setContentLanguages(Collections.singletonList(targetLanguage));
        persistSelectedLanguageToLocal(targetLanguage);
    }

    @Nullable
    private static String resolveSelectedLanguageFromSdk() {
        List<String> languages = PSSDK.getContentLanguages();
        if (languages == null) {
            return null;
        }
        for (String language : languages) {
            String normalized = normalizeContentLanguage(language);
            if (!TextUtils.isEmpty(normalized)) {
                return normalized;
            }
        }
        return null;
    }

    @Nullable
    private static String readSelectedLanguageFromLocal() {
        Context context = App.getAppContext();
        if (context == null) {
            return null;
        }
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String language = sp.getString(KEY_SELECTED_CONTENT_LANGUAGE, null);
        if (TextUtils.isEmpty(language)) {
            return null;
        }
        return normalizeContentLanguage(language);
    }

    private static void persistSelectedLanguageToLocal(@Nullable String language) {
        String normalized = normalizeContentLanguage(language);
        Context context = App.getAppContext();
        if (context == null || TextUtils.isEmpty(normalized)) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_SELECTED_CONTENT_LANGUAGE, normalized).apply();
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
