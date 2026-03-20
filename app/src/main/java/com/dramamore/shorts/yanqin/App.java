package com.dramamore.shorts.yanqin;

import android.app.Application;
import android.util.Log;

import com.bytedance.sdk.shortplay.api.PSSDK;

public class App extends Application {
    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();

        init();
    }

    private void init() {
        //release
        String appId = "215000708";
        String securityKey = "6932a7b7e3459677445f135b3d8fe222";
        PSSDK.Config.Builder builder = new PSSDK.Config.Builder();
        builder.appId(appId)
                .vodAppId("943688")
                .securityKey(securityKey)
                .licenseAssertPath("l-3887091217-ch-vod-a-943688.lic")
                .debug(true);

        PSSDK.init(this, builder.build(), new PSSDK.PSSDKInitListener() {
            @Override
            public void onInitFinished(boolean success, PSSDK.ErrorInfo errorInfo) {
                Log.d(TAG, "onInitFinished() called with: success = [" + success + "], errorInfo = [" + errorInfo + "]");
            }
        });

        PSSDK.setEligibleAudience(true);
    }
}
