package com.dramamore.shorts.yanqin.ads;

import androidx.annotation.NonNull;

public class NoOpConsentGate implements AdConsentGate {
    @Override
    public boolean canRequestAds() {
        return true;
    }

    @Override
    public void beforeAdRequest(@NonNull Runnable action) {
        action.run();
    }
}
