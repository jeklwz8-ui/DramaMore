package com.dramamore.shorts.yanqin.ads;

import androidx.annotation.NonNull;

public interface AdConsentGate {
    boolean canRequestAds();

    void beforeAdRequest(@NonNull Runnable action);
}
