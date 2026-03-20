package com.example.dramasdk;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.bytedance.sdk.openadsdk.api.init.PAGConfig;
import com.bytedance.sdk.openadsdk.api.init.PAGSdk;
import com.bytedance.sdk.shortplay.api.PSSDK;

public class DemoApp extends Application {
    private static final String TAG = "PSSDK.DemoApp";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        String appId = "8309530";
        String securityKey = "82dc912698c9e4e7cd8176da48906fde";
        PSSDK.Config.Builder builder = new PSSDK.Config.Builder();
        builder.appId(appId)
                .vodAppId("568708")
                .securityKey(securityKey)
                .licenseAssertPath("vod_player.lic")
                .debug(true);
        PSSDK.init(this, builder.build(), new PSSDK.PSSDKInitListener() {
            @Override
            public void onInitFinished(boolean success, PSSDK.ErrorInfo errorInfo) {
                Log.d(TAG, "onInitFinished() called with: success = [" + success + "], errorInfo = [" + errorInfo + "]");
            }
        });

        PSSDK.setEligibleAudience(true);

        initPangleAdsSDK();
    }

    private void initPangleAdsSDK() {
        PAGConfig.Builder builder = new PAGConfig.Builder();
        builder.appId("8025677");
        PAGSdk.init(this, builder.build(), new PAGSdk.PAGInitCallback() {
            @Override
            public void success() {
                Log.d(TAG, "pangle ads sdk init success");
            }

            @Override
            public void fail(int i, String s) {
                Log.d(TAG, "pangle ads sdk init fail, i=" + i + ", s=" + s);
            }
        });
    }
}
