package com.dramamore.shorts.yanqin.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.dramamore.shorts.yanqin.utils.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.adapter.GridSpacingItemDecoration;
import com.dramamore.shorts.yanqin.adapter.HomeAdapter;
import com.dramamore.shorts.yanqin.utils.DpUtils;
import com.dramamore.shorts.yanqin.utils.Logs;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final long UI_REFRESH_DELAY_MS = 2000L;
    private static final long HEADER_REQUEST_DELAY_MS = 300L;
    private static final String CACHE_HOME_FEED = "cache_home_feed";
    private static final String CACHE_HOME_BANNER = "cache_home_banner";
    private static final String CACHE_HOME_HOT = "cache_home_hot";
    private static final String CACHE_HOME_MOST = "cache_home_most";
    private static final String CACHE_HOME_CARTOON = "cache_home_cartoon";
    private static final Type SHORT_PLAY_LIST_TYPE = new TypeToken<List<ShortPlay>>() {}.getType();
    private final Gson gson = new Gson();
   /* private ViewPager2 bannerPager;
    private LinearLayout indicatorLayout;
    private LinearLayout llHot;*/

    private int currentPage = 1;
    private boolean hasMore = false;
    private boolean isLoading = false;
    private HomeAdapter adapter;
    private boolean isInitBannerData, isInitHotData, isInitMostData, isInitCartoonData;
    private boolean isHeaderRequestScheduled = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private boolean postToUiIfAlive(@NonNull Runnable action) {
        FragmentActivity activity = getActivity();
        if (!isAdded() || activity == null) {
            return false;
        }
        mainHandler.postDelayed(() -> {
            if (isAdded()) {
                action.run();
            }
        }, UI_REFRESH_DELAY_MS);
        return true;
    }

    private void cacheShortPlayList(String key, List<ShortPlay> list) {
        if (getContext() == null || list == null) {
            return;
        }
        SPUtils.getInstance(getContext()).putString(key, gson.toJson(list));
    }

    @Nullable
    private List<ShortPlay> readCachedShortPlayList(String key) {
        if (getContext() == null) {
            return null;
        }
        String json = SPUtils.getInstance(getContext()).getString(key, "");
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(json, SHORT_PLAY_LIST_TYPE);
        } catch (Throwable ignore) {
            return null;
        }
    }

    private void applyHomeCacheIfNeeded() {
        if (adapter == null || adapter.getItemCount() > 0) {
            return;
        }
        List<ShortPlay> feedCache = readCachedShortPlayList(CACHE_HOME_FEED);
        List<ShortPlay> bannerCache = readCachedShortPlayList(CACHE_HOME_BANNER);
        List<ShortPlay> hotCache = readCachedShortPlayList(CACHE_HOME_HOT);
        List<ShortPlay> mostCache = readCachedShortPlayList(CACHE_HOME_MOST);
        List<ShortPlay> cartoonCache = readCachedShortPlayList(CACHE_HOME_CARTOON);

        boolean hasHeaderCache = (bannerCache != null && !bannerCache.isEmpty())
                || (hotCache != null && !hotCache.isEmpty())
                || (mostCache != null && !mostCache.isEmpty())
                || (cartoonCache != null && !cartoonCache.isEmpty());
        if ((feedCache == null || feedCache.isEmpty()) && !hasHeaderCache) {
            return;
        }

        adapter.setData(feedCache == null ? Collections.emptyList() : feedCache);
        if (bannerCache != null && !bannerCache.isEmpty()) {
            adapter.setHeaderBannerData(bannerCache);
        }
        if (hotCache != null && !hotCache.isEmpty()) {
            adapter.setHeaderHotData(hotCache);
        }
        if (mostCache != null && !mostCache.isEmpty()) {
            adapter.setHeaderMostData(mostCache);
        }
        if (cartoonCache != null && !cartoonCache.isEmpty()) {
            adapter.setHeaderCartoonData(cartoonCache);
        }
    }

    private void scheduleHeaderDataInitIfNeeded() {
        if (isHeaderRequestScheduled || (isInitBannerData && isInitHotData && isInitMostData && isInitCartoonData)) {
            return;
        }
        isHeaderRequestScheduled = true;
        mainHandler.postDelayed(() -> {
            isHeaderRequestScheduled = false;
            if (!isAdded()) {
                return;
            }
            if (!isInitBannerData) {
                initBannerData();
            }
            if (!isInitHotData) {
                initHotData();
            }
            if (!isInitMostData) {
                initMostData();
            }
            if (!isInitCartoonData) {
                initCartoonData();
            }
        }, HEADER_REQUEST_DELAY_MS);
    }

    private void initBannerData() {
        PSSDK.requestNewDrama(1, 5, new PSSDK.FeedListResultListener() {
            @Override
            public void onFail(PSSDK.ErrorInfo errorInfo) {
                Logs.i(TAG, "initNewData-onFail-errorInfo=" + errorInfo);
            }

            @Override
            public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> feedListLoadResult) {
                Logs.i(TAG, "initNewData-onSuccess-feedListLoadResult=" + feedListLoadResult.toString() + ",size=" + feedListLoadResult.dataList.size());
                isInitBannerData = true;
                postToUiIfAlive(() -> {
                    cacheShortPlayList(CACHE_HOME_BANNER, feedListLoadResult.dataList);
                    if (adapter != null) {
                        adapter.setHeaderBannerData(feedListLoadResult.dataList);
                    }
                });
            }
        });
    }

    private void initHotData() {
        PSSDK.requestPopularDrama(1, 3, Arrays.asList(5L), new PSSDK.FeedListResultListener() {
            @Override
            public void onFail(PSSDK.ErrorInfo errorInfo) {
                Logs.i(TAG, "initHotData-onFail-errorInfo=" + errorInfo);
            }

            @Override
            public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> feedListLoadResult) {
                Logs.i(TAG, "initHotData-onSuccess-feedListLoadResult=" + feedListLoadResult.toString());
                isInitHotData = true;
                postToUiIfAlive(() -> {
                    cacheShortPlayList(CACHE_HOME_HOT, feedListLoadResult.dataList);
                    if (adapter != null) {
                        adapter.setHeaderHotData(feedListLoadResult.dataList);
                    }
                });
            }
        });
    }

    //近期最多收藏
    private void initMostData() {
        PSSDK.requestDramaByTag(4, 1, 3, new PSSDK.FeedListResultListener() {
            @Override
            public void onFail(PSSDK.ErrorInfo errorInfo) {
                Logs.i(TAG, "initMostData-onFail-errorInfo=" + errorInfo);
            }

            @Override
            public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> feedListLoadResult) {
                Logs.i(TAG, "initMostData-onSuccess-feedListLoadResult=" + feedListLoadResult.toString());
                isInitMostData = true;
                postToUiIfAlive(() -> {
                    cacheShortPlayList(CACHE_HOME_MOST, feedListLoadResult.dataList);
                    if (adapter != null) {
                        adapter.setHeaderMostData(feedListLoadResult.dataList);
                    }
                });
            }
        });
    }

    //动漫剧
    private void initCartoonData() {
        PSSDK.requestFeedListByCategoryIds(Arrays.asList(1000701l), null, 1, 3, new PSSDK.FeedListResultListener() {
            @Override
            public void onFail(PSSDK.ErrorInfo errorInfo) {
                Logs.i(TAG, "initCartoonData-onFail-errorInfo=" + errorInfo);
            }

            @Override
            public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> feedListLoadResult) {
                Logs.i(TAG, "initCartoonData-onSuccess-feedListLoadResult=" + feedListLoadResult.toString());
                isInitCartoonData = true;
                postToUiIfAlive(() -> {
                    cacheShortPlayList(CACHE_HOME_CARTOON, feedListLoadResult.dataList);
                    if (adapter != null) {
                        adapter.setHeaderCartoonData(feedListLoadResult.dataList);
                    }
                });
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initRecyclerView(view);
        applyHomeCacheIfNeeded();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        applyHomeCacheIfNeeded();

        scheduleHeaderDataInitIfNeeded();

        if (!isLoading && currentPage == 1) {
            loadMoreData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainHandler.removeCallbacksAndMessages(null);
        isHeaderRequestScheduled = false;
        isLoading = false;
    }

    private void initRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rv_home);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, DpUtils.dp2px(getActivity(), 10), false));

        adapter = new HomeAdapter();

        // 1. 设置三列网格
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        // 2. 設置跨列邏輯
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // 判斷當前 position 是否為 Header
                // 假設你的 Adapter 中第 0 位是 Header
                if (adapter.getItemViewType(position) == HomeAdapter.TYPE_HEADER) {
                    return 3; // 返回 3，代表佔滿 3 列（橫跨全螢幕）
                } else {
                    return 1; // 返回 1，代表只佔 1 列（正常顯示）
                }
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // 2. 上拉加载监听
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { // 向下滑动
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && hasMore) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            Logs.i(TAG, "onScrolled-加载更多！");
                            loadMoreData();
                        }
                    }
                }
            }
        });
    }

    private void loadMoreData() {
        isLoading = true;
        List<Long> list = Arrays.asList(1l, 2l, 4l, 5l, 6l, 7l, 8l, 11l, 12l, 13l, 14l, 1701l, 1702l, 1704l, 1706l, 1709l, 1751l, 1851l, 1901l, 1902l, 1951l, 1952l, 1953l, 1954l);
//        PSSDK.requestFeedListByCategoryIds(list,null,currentPage,20,new PSSDK.FeedListResultListener() {
        PSSDK.requestFeedList(currentPage, 20, new PSSDK.FeedListResultListener() {
            @Override
            public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> result) {
                Logs.i(TAG, "loadMoreData-onSuccess-feedListLoadResult-hasMore=" + result.hasMore + ",size=" + result.dataList.size());

                boolean posted = postToUiIfAlive(() -> {
                    isLoading = false;
                    if (adapter != null && result.dataList != null && !result.dataList.isEmpty()) {
                        if (currentPage == 1) {
                            cacheShortPlayList(CACHE_HOME_FEED, result.dataList);
                        }
                        hasMore = result.hasMore;//更多
                        if (result.hasMore) {
                            currentPage++;
                        }
                        if (currentPage == 1) {
                            adapter.setData(result.dataList);
                        } else {
                            adapter.addData(result.dataList);
                        }
                    }
                });
                if (!posted) {
                    isLoading = false;
                }
            }

            @Override
            public void onFail(PSSDK.ErrorInfo errorInfo) {
                Logs.i(TAG, "loadMoreData-onFail-errorInfo=" + errorInfo);
                isLoading = false;
                // 处理错误提示
            }
        });
    }

}
