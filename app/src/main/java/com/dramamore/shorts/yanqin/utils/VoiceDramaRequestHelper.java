package com.dramamore.shorts.yanqin.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class VoiceDramaRequestHelper {
    private static final String TAG = "VoiceDramaRequestHelper";
    private static final String LANGUAGE_ID = "id";
    private static final String LANGUAGE_IN = "in";
    private static final Set<String> SUPPORTED_VOICE_LANGUAGES = new LinkedHashSet<>(
            java.util.Arrays.asList("zh_hans", "zh_hant", "en", "vi", "id", "th", "ja", "ko", "pt", "es")
    );

    private VoiceDramaRequestHelper() {
    }

    public static boolean requestVoiceDramaBySelectedLanguages(
            int page,
            int pageSize,
            @NonNull PSSDK.FeedListResultListener listener
    ) {
        List<String> voiceLanguages = getSelectedVoiceLanguages();
        if (voiceLanguages.isEmpty()) {
            Logs.i(TAG, "requestVoiceDramaBySelectedLanguages-skip: no selected content language");
            return false;
        }
        Logs.i(TAG, "requestVoiceDramaBySelectedLanguages-languages=" + voiceLanguages);
        requestVoiceDramaInternal(voiceLanguages, page, pageSize, listener);
        return true;
    }

    @NonNull
    public static List<String> getSelectedVoiceLanguages() {
        List<String> selectedLanguages = ContentLanguageHelper.getSelectedContentLanguages();
        Set<String> normalizedLanguages = new LinkedHashSet<>();
        for (String language : selectedLanguages) {
            if (TextUtils.isEmpty(language)) {
                continue;
            }
            String safeLanguage = language.trim().toLowerCase(Locale.US);
            if (safeLanguage.isEmpty()) {
                continue;
            }
            String voiceLanguage = mapContentLanguageToVoiceLanguage(safeLanguage);
            if (!TextUtils.isEmpty(voiceLanguage) && SUPPORTED_VOICE_LANGUAGES.contains(voiceLanguage)) {
                normalizedLanguages.add(voiceLanguage);
            }
        }
        return new ArrayList<>(normalizedLanguages);
    }

    @NonNull
    public static String getSelectedVoiceLanguageCacheSuffix() {
        List<String> selectedLanguages = getSelectedVoiceLanguages();
        if (selectedLanguages.isEmpty()) {
            return "none";
        }
        List<String> cacheLanguages = new ArrayList<>(selectedLanguages);
        Collections.sort(cacheLanguages);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cacheLanguages.size(); i++) {
            if (i > 0) {
                builder.append('_');
            }
            builder.append(cacheLanguages.get(i));
        }
        return builder.toString();
    }

    private static void requestVoiceDramaInternal(
            @NonNull List<String> voiceLanguages,
            int page,
            int pageSize,
            @NonNull PSSDK.FeedListResultListener listener
    ) {
        PSSDK.setVoiceLanguages(voiceLanguages);
        PSSDK.requestFeedList(page, pageSize, new PSSDK.FeedListResultListener() {
            @Override
            public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> result) {
                PSSDK.setVoiceLanguages(Collections.emptyList());
                listener.onSuccess(result);
            }

            @Override
            public void onFail(PSSDK.ErrorInfo errorInfo) {
                PSSDK.setVoiceLanguages(Collections.emptyList());
                listener.onFail(errorInfo);
            }
        });
    }

    private static String mapContentLanguageToVoiceLanguage(@NonNull String contentLanguage) {
        if (LANGUAGE_IN.equals(contentLanguage)) {
            return LANGUAGE_ID;
        }
        return contentLanguage;
    }
}
