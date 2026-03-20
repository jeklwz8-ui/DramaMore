package com.example.dramasdk.feedlist;

import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NormalListFragment extends AbsFeedListFragment {

    @Override
    protected void loadData(ShortplayListListener shortplayListListener) {
        PSSDK.requestFeedList(1, 100, shortplayListListener);
    }

    public String getTitle() {
        return "普通";
    }

    @Override
    protected void onDataLoaded(List<ShortPlay> dataList) {
        // 手动排序，便于验证指定短剧是否下发
        Collections.sort(dataList, new Comparator<ShortPlay>() {
            @Override
            public int compare(ShortPlay o1, ShortPlay o2) {
                return Long.compare(o1.id, o2.id);
            }
        });
    }
}
