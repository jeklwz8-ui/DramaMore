package com.dramamore.shorts.yanqin.ads;

import com.dramamore.shorts.yanqin.BuildConfig;

public final class AdMobConfig {
    public static final long APP_OPEN_EXPIRATION_MS = 4L * 60L * 60L * 1000L;
    public static final long COLD_START_TIMEOUT_MS = BuildConfig.APP_OPEN_COLD_START_TIMEOUT_MS;
    public static final String APP_OPEN_PRODUCTION_AD_UNIT_ID = "";

    private AdMobConfig() {
    }

    public static String getAppOpenAdUnitId() {
        return BuildConfig.APP_OPEN_AD_UNIT_ID;
    }
}
