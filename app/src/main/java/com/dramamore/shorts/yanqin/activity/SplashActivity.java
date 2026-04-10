package com.dramamore.shorts.yanqin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.AdError;
import com.anythink.splashad.api.ATSplashAd;
import com.anythink.splashad.api.ATSplashAdExtraInfo;
import com.anythink.splashad.api.ATSplashAdListener;
import com.dramamore.shorts.yanqin.App;
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.utils.Logs;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final long SPLASH_LOAD_GUARD_TIMEOUT_MS = 5000L;

    private ATSplashAd topOnSplashAd;
    private FrameLayout splashAdContainer;
    private boolean finished;
    private final Handler splashHandler = new Handler(Looper.getMainLooper());
    private final Runnable splashGuardRunnable = () -> launchMain("guard_timeout");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splashAdContainer = findViewById(R.id.fl_splash_ad_container);
        tryShowTopOnSplashAd();
    }

    private void tryShowTopOnSplashAd() {
        String splashPlacementId = App.getTopOnSplashPlacementId(this);
        if (TextUtils.isEmpty(splashPlacementId)) {
            Logs.i(TAG, "skip splash ad: placement id empty");
            launchMain("placement_empty");
            return;
        }
        if (TextUtils.isEmpty(App.TOPON_APP_ID) || TextUtils.isEmpty(App.getTopOnAppKey(this))) {
            Logs.i(TAG, "skip splash ad: TOPON app id/app key not ready");
            launchMain("sdk_not_ready");
            return;
        }

        splashAdContainer.setVisibility(View.GONE);
        splashAdContainer.removeAllViews();

        topOnSplashAd = new ATSplashAd(this, splashPlacementId, new ATSplashAdListener() {
            @Override
            public void onAdLoaded(boolean isTimeout) {
                Logs.i(TAG, "TopOn splash onAdLoaded, isTimeout=" + isTimeout);
                if (finished || isFinishing() || isDestroyed()) {
                    return;
                }
                if (topOnSplashAd != null && topOnSplashAd.isAdReady()) {
                    splashAdContainer.setVisibility(View.VISIBLE);
                    topOnSplashAd.show(SplashActivity.this, splashAdContainer);
                    return;
                }
                if (isTimeout) {
                    launchMain("ad_loaded_timeout");
                }
            }

            @Override
            public void onAdLoadTimeout() {
                Logs.i(TAG, "TopOn splash onAdLoadTimeout");
                launchMain("load_timeout");
            }

            @Override
            public void onNoAdError(AdError adError) {
                Logs.i(TAG, "TopOn splash onNoAdError: " + (adError == null ? "unknown" : adError.getFullErrorInfo()));
                launchMain("no_ad_error");
            }

            @Override
            public void onAdShow(ATAdInfo atAdInfo) {
                Logs.i(TAG, "TopOn splash onAdShow");
                splashHandler.removeCallbacks(splashGuardRunnable);
            }

            @Override
            public void onAdClick(ATAdInfo atAdInfo) {
                Logs.i(TAG, "TopOn splash onAdClick");
            }

            @Override
            public void onAdDismiss(ATAdInfo atAdInfo, ATSplashAdExtraInfo atSplashAdExtraInfo) {
                Logs.i(TAG, "TopOn splash onAdDismiss");
                launchMain("ad_dismiss");
            }
        });

        topOnSplashAd.loadAd();
        Logs.i(TAG, "TopOn splash load start, placementId=" + splashPlacementId);
        splashHandler.removeCallbacks(splashGuardRunnable);
        splashHandler.postDelayed(splashGuardRunnable, SPLASH_LOAD_GUARD_TIMEOUT_MS);
    }

    private void launchMain(String reason) {
        if (finished) {
            return;
        }
        finished = true;
        splashHandler.removeCallbacks(splashGuardRunnable);
        if (splashAdContainer != null) {
            splashAdContainer.removeAllViews();
            splashAdContainer.setVisibility(View.GONE);
        }
        Logs.i(TAG, "launchMain, reason=" + reason);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        splashHandler.removeCallbacksAndMessages(null);
        if (topOnSplashAd != null) {
            topOnSplashAd.onDestory();
            topOnSplashAd = null;
        }
        if (splashAdContainer != null) {
            splashAdContainer.removeAllViews();
        }
        super.onDestroy();
    }
}
