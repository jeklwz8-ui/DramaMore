package com.dramamore.shorts.yanqin.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dramamore.shorts.yanqin.App;
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.ads.AdMobConfig;
import com.dramamore.shorts.yanqin.ads.AppOpenController;
import com.dramamore.shorts.yanqin.utils.Logs;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AppOpenController appOpenController;
    private boolean hasCommittedAdFlow;
    private boolean hasNavigatedToMain;

    private final Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            Logs.i(TAG, "cold start timeout reached");
            navigateToMain("timeout");
        }
    };

    private final AppOpenController.AvailabilityListener availabilityListener = new AppOpenController.AvailabilityListener() {
        @Override
        public void onAdAvailable() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    attemptShowColdStartAd();
                }
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            Logs.i(TAG, "skip duplicate launcher instance");
            finish();
            return;
        }
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        App app = (App) getApplication();
        appOpenController = app.getAppOpenController();
        if (appOpenController == null) {
            navigateToMain("controller_missing");
            return;
        }

        appOpenController.markColdStartPending();
        appOpenController.addAvailabilityListener(availabilityListener);
        appOpenController.preloadIfNeeded(getApplicationContext());
        mainHandler.postDelayed(timeoutRunnable, AdMobConfig.COLD_START_TIMEOUT_MS);
        attemptShowColdStartAd();
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        if (appOpenController != null) {
            appOpenController.removeAvailabilityListener(availabilityListener);
        }
        super.onDestroy();
    }

    private void attemptShowColdStartAd() {
        if (hasNavigatedToMain || hasCommittedAdFlow || isFinishing()) {
            return;
        }
        if (appOpenController == null || !appOpenController.isAdAvailable()) {
            return;
        }

        hasCommittedAdFlow = true;
        mainHandler.removeCallbacks(timeoutRunnable);
        appOpenController.showIfAvailable(this, AppOpenController.SOURCE_COLD_START, new AppOpenController.ShowCompleteListener() {
            @Override
            public void onComplete(boolean shown) {
                navigateToMain(shown ? "ad_complete" : "ad_failed");
            }
        });
    }

    private void navigateToMain(String reason) {
        if (hasNavigatedToMain) {
            return;
        }
        hasNavigatedToMain = true;
        Logs.i(TAG, "navigateToMain reason=" + reason);
        mainHandler.removeCallbacks(timeoutRunnable);
        if (appOpenController != null) {
            appOpenController.markColdStartFinished();
            appOpenController.removeAvailabilityListener(availabilityListener);
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
