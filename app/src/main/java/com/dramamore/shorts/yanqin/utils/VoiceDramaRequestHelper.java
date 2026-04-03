package com.dramamore.shorts.yanqin.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoiceDramaRequestHelper {
    private static final String TAG = "VoiceDramaRequestHelper";
    private static final String IP_COUNTRY_URL = "https://ipapi.co/country_code/";
    private static final int CONNECT_TIMEOUT_MS = 1500;
    private static final int READ_TIMEOUT_MS = 1500;
    private static final String FALLBACK_LANGUAGE = "zh_hans";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static volatile List<String> cachedVoiceLanguages;

    private VoiceDramaRequestHelper() {
    }

    public static void requestVoiceDramaByIp(
            @NonNull Context context,
            int page,
            int pageSize,
            @NonNull PSSDK.FeedListResultListener listener
    ) {
        List<String> cachedLanguages = cachedVoiceLanguages;
        if (cachedLanguages != null && !cachedLanguages.isEmpty()) {
            requestVoiceDramaInternal(cachedLanguages, page, pageSize, true, listener);
            return;
        }

        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                List<String> voiceLanguages = resolveVoiceLanguagesByIp(context);
                cachedVoiceLanguages = voiceLanguages;
                MAIN_HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        requestVoiceDramaInternal(voiceLanguages, page, pageSize, true, listener);
                    }
                });
            }
        });
    }

    private static void requestVoiceDramaInternal(
            @NonNull List<String> voiceLanguages,
            int page,
            int pageSize,
            boolean allowFallbackToZhHans,
            @NonNull PSSDK.FeedListResultListener listener
    ) {
        PSSDK.setVoiceLanguages(voiceLanguages);
        PSSDK.requestFeedList(page, pageSize, new PSSDK.FeedListResultListener() {
            @Override
            public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> result) {
                if (shouldFallbackToZhHans(result, page, voiceLanguages, allowFallbackToZhHans)) {
                    Logs.i(TAG, "requestVoiceDramaInternal-empty primary voice result, fallback to zh_hans");
                    requestVoiceDramaInternal(
                            Collections.singletonList(FALLBACK_LANGUAGE),
                            page,
                            pageSize,
                            false,
                            listener
                    );
                    return;
                }
                PSSDK.setVoiceLanguages(Collections.emptyList());
                listener.onSuccess(result);
            }

            @Override
            public void onFail(PSSDK.ErrorInfo errorInfo) {
                if (page == 1 && allowFallbackToZhHans && !voiceLanguages.contains(FALLBACK_LANGUAGE)) {
                    Logs.i(TAG, "requestVoiceDramaInternal-primary voice request failed, fallback to zh_hans, errorInfo=" + errorInfo);
                    requestVoiceDramaInternal(
                            Collections.singletonList(FALLBACK_LANGUAGE),
                            page,
                            pageSize,
                            false,
                            listener
                    );
                    return;
                }
                PSSDK.setVoiceLanguages(Collections.emptyList());
                listener.onFail(errorInfo);
            }
        });
    }

    @NonNull
    private static List<String> resolveVoiceLanguagesByIp(@NonNull Context context) {
        String countryCode = fetchCountryCodeByIp();
        if (countryCode == null || countryCode.isEmpty()) {
            countryCode = Locale.getDefault().getCountry();
        }
        List<String> languages = mapCountryCodeToVoiceLanguages(countryCode);
        Logs.i(TAG, "resolveVoiceLanguagesByIp-countryCode=" + countryCode + ", languages=" + languages);
        return languages;
    }

    private static String fetchCountryCodeByIp() {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            connection = (HttpURLConnection) new URL(IP_COUNTRY_URL).openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "";
            }
            InputStream inputStream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String countryCode = reader.readLine();
            return countryCode == null ? "" : countryCode.trim().toUpperCase(Locale.US);
        } catch (Exception e) {
            Logs.i(TAG, "fetchCountryCodeByIp-error=" + e.getMessage());
            return "";
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception ignore) {
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @NonNull
    private static List<String> mapCountryCodeToVoiceLanguages(String countryCode) {
        List<String> languages = new ArrayList<>();
        String safeCountryCode = countryCode == null ? "" : countryCode.trim().toUpperCase(Locale.US);
        if ("JP".equals(safeCountryCode)) {
            languages.add("ja");
        } else if ("KR".equals(safeCountryCode)) {
            languages.add("ko");
        } else if ("TH".equals(safeCountryCode)) {
            languages.add("th");
        } else if ("VN".equals(safeCountryCode)) {
            languages.add("vi");
        } else if ("ID".equals(safeCountryCode)) {
            languages.add("id");
        } else if ("PT".equals(safeCountryCode) || "BR".equals(safeCountryCode)) {
            languages.add("pt");
        } else if ("ES".equals(safeCountryCode)
                || "MX".equals(safeCountryCode)
                || "AR".equals(safeCountryCode)
                || "CO".equals(safeCountryCode)
                || "CL".equals(safeCountryCode)
                || "PE".equals(safeCountryCode)) {
            languages.add("es");
        } else if ("SA".equals(safeCountryCode)
                || "AE".equals(safeCountryCode)
                || "EG".equals(safeCountryCode)) {
            languages.add("ar");
        } else if ("TW".equals(safeCountryCode)
                || "HK".equals(safeCountryCode)
                || "MO".equals(safeCountryCode)) {
            languages.add("zh_hant");
        } else if ("CN".equals(safeCountryCode) || "SG".equals(safeCountryCode) || "MY".equals(safeCountryCode)) {
            languages.add("zh_hans");
        } else {
            languages.add("en");
        }
        return languages;
    }

    private static boolean shouldFallbackToZhHans(
            PSSDK.FeedListLoadResult<ShortPlay> result,
            int page,
            @NonNull List<String> voiceLanguages,
            boolean allowFallbackToZhHans
    ) {
        return page == 1
                && allowFallbackToZhHans
                && !voiceLanguages.contains(FALLBACK_LANGUAGE)
                && (result == null || result.dataList == null || result.dataList.isEmpty());
    }
}
