package com.dramamore.shorts.yanqin;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.dramamore.shorts.yanqin.ads.AppOpenController;
import com.dramamore.shorts.yanqin.ads.NoOpConsentGate;
import com.dramamore.shorts.yanqin.utils.Logs;
import com.bytedance.sdk.openadsdk.api.init.PAGConfig;
import com.bytedance.sdk.openadsdk.api.init.PAGSdk;
import com.bytedance.sdk.shortplay.api.PSSDK;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

public class App extends Application implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    private static final String TAG = "App";
    public static final String REWARDAD_ID = "";
    public static final String NATIVEAD_ID = "n69c9e5dfe5c90";
    public static final String BANNERAD_ID = "n69c9e5bebb98f";

    public static final String TOPON_APP_ID = "h69c9e50997ea1";
    public static final String GOOGLE_APP_ID = "ca-app-pub-1656134190869494~4054416417";
    private static volatile boolean isPangleInitStarted = false;
    private static volatile boolean isPangleInitDone = false;
    private static final List<Runnable> pendingPangleReadyCallbacks = new ArrayList<>();
    private AppOpenController appOpenController;
    @Nullable
    private Activity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        init();
    }

    private void init() {
        appOpenController = new AppOpenController(getApplicationContext(), new NoOpConsentGate());
        initAdMob();

        //release
        String appId = "215000708";
        String securityKey = "6932a7b7e3459677445f135b3d8fe222";
        PSSDK.Config.Builder builder = new PSSDK.Config.Builder();
        builder.appId(appId)
                .vodAppId("943688")
                .securityKey(securityKey)
                .licenseAssertPath("l-3887091217-ch-vod-a-943688.lic")
                .debug(BuildConfig.DEBUG);

        PSSDK.init(this, builder.build(), new PSSDK.PSSDKInitListener() {
            @Override
            public void onInitFinished(boolean success, PSSDK.ErrorInfo errorInfo) {
                Log.d(TAG, "onInitFinished() called with: success = [" + success + "], errorInfo = [" + errorInfo + "]");
            }
        });

        PSSDK.setEligibleAudience(true);

        preWarmPangleAdsSdk();
    }

    private void initAdMob() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MobileAds.initialize(getApplicationContext(), initializationStatus -> {
                    Logs.i(TAG, "admob initialized");
                    if (appOpenController != null) {
                        appOpenController.preloadIfNeeded(getApplicationContext());
                    }
                });
            }
        }, "admob-init").start();
    }

    public AppOpenController getAppOpenController() {
        return appOpenController;
    }

    private void preWarmPangleAdsSdk() {
        // Delay ad sdk init to avoid adding startup pressure on cold launch.
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                ensurePangleAdsSdkInit(getApplicationContext(), null), 800L);
    }

    public static void ensurePangleAdsSdkInit(Context context, Runnable onReady) {
        if (isPangleInitDone) {
            if (onReady != null) {
                onReady.run();
            }
            return;
        }
        synchronized (App.class) {
            if (onReady != null) {
                pendingPangleReadyCallbacks.add(onReady);
            }
            if (isPangleInitStarted) {
                return;
            }
            isPangleInitStarted = true;
        }

        PAGConfig.Builder builder = new PAGConfig.Builder();
        builder.appId("8799428");
        PAGSdk.init(context.getApplicationContext(), builder.build(), new PAGSdk.PAGInitCallback() {
            @Override
            public void success() {
                isPangleInitDone = true;
                Log.d(TAG, "pangle ads sdk init success");
                dispatchPangleReadyCallbacks();
            }

            @Override
            public void fail(int i, String s) {
                Log.d(TAG, "pangle ads sdk init fail, i=" + i + ", s=" + s);
                dispatchPangleReadyCallbacks();
            }
        });
    }

    private static void dispatchPangleReadyCallbacks() {
        List<Runnable> callbacks;
        synchronized (App.class) {
            callbacks = new ArrayList<>(pendingPangleReadyCallbacks);
            pendingPangleReadyCallbacks.clear();
        }
        for (Runnable callback : callbacks) {
            if (callback != null) {
                callback.run();
            }
        }
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        if (appOpenController == null) {
            return;
        }
        if (currentActivity == null) {
            appOpenController.preloadIfNeeded(getApplicationContext());
            return;
        }
        appOpenController.showIfAvailable(currentActivity, AppOpenController.SOURCE_FOREGROUND, null);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (appOpenController != null && !appOpenController.isShowingAd()) {
            currentActivity = activity;
            appOpenController.setCurrentScreen(activity.getClass().getSimpleName());
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (appOpenController != null && !appOpenController.isShowingAd()) {
            currentActivity = activity;
            appOpenController.setCurrentScreen(activity.getClass().getSimpleName());
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (currentActivity == activity) {
            currentActivity = null;
        }
    }
}
