package com.example.dramasdk.feedlist;

import com.bytedance.sdk.shortplay.api.PSSDK;

public class LatestListFragment extends AbsFeedListFragment {
    @Override
    protected void loadData(ShortplayListListener shortplayListListener) {
        PSSDK.requestNewDrama(1, 100, shortplayListListener);
    }

    public String getTitle() {
        return "最新";
    }
}
