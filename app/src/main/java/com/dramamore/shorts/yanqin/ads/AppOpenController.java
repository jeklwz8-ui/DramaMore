package com.dramamore.shorts.yanqin.ads;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dramamore.shorts.yanqin.activity.DramaPlayActivity;
import com.dramamore.shorts.yanqin.activity.SplashActivity;
import com.dramamore.shorts.yanqin.utils.Logs;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class AppOpenController {
    public static final String SOURCE_COLD_START = "cold_start";
    public static final String SOURCE_FOREGROUND = "foreground";

    private static final String TAG = "AppOpenController";

    private final Context appContext;
    private final AdConsentGate adConsentGate;
    private final Set<AvailabilityListener> availabilityListeners = new CopyOnWriteArraySet<>();

    @Nullable
    private AppOpenAd appOpenAd;
    private boolean isLoadingAd;
    private boolean isShowingAd;
    private boolean coldStartPending;
    private long loadTime;
    @Nullable
    private String currentScreenTag;

    public AppOpenController(@NonNull Context appContext, @NonNull AdConsentGate adConsentGate) {
        this.appContext = appContext.getApplicationContext();
        this.adConsentGate = adConsentGate;
    }

    public synchronized void preloadIfNeeded(@NonNull Context context) {
        if (!adConsentGate.canRequestAds()) {
            Logs.i(TAG, "load skip reason=no_consent");
            return;
        }
        if (isLoadingAd) {
            Logs.i(TAG, "load skip reason=already_loading");
            return;
        }
        if (isAdAvailable()) {
            Logs.i(TAG, "load skip reason=ad_ready");
            return;
        }

        adConsentGate.beforeAdRequest(new Runnable() {
            @Override
            public void run() {
                loadAdInternal(context.getApplicationContext());
            }
        });
    }

    public synchronized boolean isAdAvailable() {
        return appOpenAd != null && (System.currentTimeMillis() - loadTime) < AdMobConfig.APP_OPEN_EXPIRATION_MS;
    }

    public synchronized boolean isShowingAd() {
        return isShowingAd;
    }

    public synchronized boolean isColdStartPending() {
        return coldStartPending;
    }

    public synchronized void markColdStartPending() {
        coldStartPending = true;
        Logs.i(TAG, "cold start state=pending");
    }

    public synchronized void markColdStartFinished() {
        coldStartPending = false;
        Logs.i(TAG, "cold start state=finished");
    }

    public synchronized void setCurrentScreen(@Nullable String screenTag) {
        currentScreenTag = screenTag;
    }

    public void addAvailabilityListener(@NonNull AvailabilityListener listener) {
        availabilityListeners.add(listener);
    }

    public void removeAvailabilityListener(@NonNull AvailabilityListener listener) {
        availabilityListeners.remove(listener);
    }

    public void showIfAvailable(
            @NonNull Activity activity,
            @NonNull String source,
            @Nullable ShowCompleteListener onComplete
    ) {
        final AppOpenAd adToShow;
        synchronized (this) {
            String skipReason = getSkipReasonLocked(activity, source);
            if (skipReason != null) {
                Logs.i(TAG, "show skip reason=" + skipReason + ", source=" + source);
                preloadIfNeeded(activity.getApplicationContext());
                notifyShowComplete(onComplete, false);
                return;
            }
            if (!isAdAvailable()) {
                Logs.i(TAG, "show skip reason=no_ready_ad, source=" + source);
                preloadIfNeeded(activity.getApplicationContext());
                notifyShowComplete(onComplete, false);
                return;
            }
            adToShow = appOpenAd;
            appOpenAd = null;
            isShowingAd = true;
        }

        if (adToShow == null) {
            synchronized (this) {
                isShowingAd = false;
            }
            Logs.i(TAG, "show skip reason=ad_reference_missing, source=" + source);
            preloadIfNeeded(activity.getApplicationContext());
            notifyShowComplete(onComplete, false);
            return;
        }

        Logs.i(TAG, "show start, source=" + source);
        adToShow.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                Logs.i(TAG, "show success, source=" + source);
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                synchronized (AppOpenController.this) {
                    isShowingAd = false;
                    loadTime = 0L;
                }
                Logs.i(TAG, "dismiss, source=" + source);
                preloadIfNeeded(appContext);
                notifyShowComplete(onComplete, true);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                synchronized (AppOpenController.this) {
                    isShowingAd = false;
                    loadTime = 0L;
                }
                Logs.i(TAG, "failToShow, source=" + source + ", error=" + adError.getMessage());
                preloadIfNeeded(appContext);
                notifyShowComplete(onComplete, false);
            }
        });

        try {
            adToShow.show(activity);
        } catch (Throwable throwable) {
            synchronized (this) {
                isShowingAd = false;
                loadTime = 0L;
            }
            Logs.i(TAG, "failToShow, source=" + source + ", error=" + throwable.getMessage());
            preloadIfNeeded(appContext);
            notifyShowComplete(onComplete, false);
        }
    }

    private synchronized void loadAdInternal(@NonNull Context context) {
        if (isLoadingAd) {
            return;
        }
        isLoadingAd = true;
        Logs.i(TAG, "load start");
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(
                context,
                AdMobConfig.getAppOpenAdUnitId(),
                request,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        synchronized (AppOpenController.this) {
                            appOpenAd = ad;
                            isLoadingAd = false;
                            loadTime = System.currentTimeMillis();
                        }
                        Logs.i(TAG, "load success");
                        notifyAvailabilityListeners();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        synchronized (AppOpenController.this) {
                            appOpenAd = null;
                            isLoadingAd = false;
                            loadTime = 0L;
                        }
                        Logs.i(TAG, "load fail, error=" + loadAdError.getMessage());
                    }
                }
        );
    }

    @Nullable
    private synchronized String getSkipReasonLocked(@NonNull Activity activity, @NonNull String source) {
        if (!adConsentGate.canRequestAds()) {
            return "no_consent";
        }
        if (isShowingAd) {
            return "already_showing";
        }
        if (SOURCE_FOREGROUND.equals(source) && coldStartPending) {
            return "cold_start_pending";
        }
        if (SOURCE_COLD_START.equals(source) && !coldStartPending) {
            return "cold_start_finished";
        }
        if (activity instanceof DramaPlayActivity
                || DramaPlayActivity.class.getSimpleName().equals(currentScreenTag)) {
            return "blocked_play_activity";
        }
        if (SOURCE_FOREGROUND.equals(source)
                && (activity instanceof SplashActivity
                || SplashActivity.class.getSimpleName().equals(currentScreenTag))) {
            return "splash_foreground";
        }
        return null;
    }

    private void notifyAvailabilityListeners() {
        for (AvailabilityListener listener : availabilityListeners) {
            listener.onAdAvailable();
        }
    }

    private void notifyShowComplete(@Nullable ShowCompleteListener listener, boolean shown) {
        if (listener != null) {
            listener.onComplete(shown);
        }
    }

    public interface AvailabilityListener {
        void onAdAvailable();
    }

    public interface ShowCompleteListener {
        void onComplete(boolean shown);
    }
}
