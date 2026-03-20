package com.example.dramasdk.feedlist;

import com.bytedance.sdk.shortplay.api.PSSDK;

public class HotListFragment extends AbsFeedListFragment {

    @Override
    protected void loadData(ShortplayListListener shortplayListListener) {
        PSSDK.requestPopularDrama(1, 100, shortplayListListener);
    }

    public String getTitle() {
        return "热门";
    }

}
