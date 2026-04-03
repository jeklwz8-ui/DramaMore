package com.dramamore.shorts.yanqin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.adapter.FollowListAdapter;
import com.dramamore.shorts.yanqin.adapter.LinearSpacingItemDecoration;
import com.dramamore.shorts.yanqin.dao.HistoryDao;
import com.dramamore.shorts.yanqin.database.HistoryDatabase;
import com.dramamore.shorts.yanqin.entity.HistoryDaoEntity;
import com.dramamore.shorts.yanqin.utils.Logs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FollowFragment extends Fragment {
    private static final String TAG = "FollowFragment";
    private static final int PAGE_SIZE = 20;
    private static final int MIN_FIRST_SCREEN_ITEMS = 6;

    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMore = true;

    private FollowListAdapter adapter;
    private HistoryDao historyDao;
    private View loadingLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follow, container, false);
        loadingLayout = view.findViewById(R.id.ll_follow_loading);
        initRecyclerView(view);
        HistoryDatabase historyDb = HistoryDatabase.getDatabase(requireContext());
        historyDao = historyDb.historyDao();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            refreshData();
        }
    }

    private void refreshData() {
        if (isLoading) {
            return;
        }
        currentPage = 1;
        hasMore = true;
        if (adapter != null) {
            adapter.setData(Collections.emptyList());
        }
        setLoadingVisible(true);
        loadMoreData(true, true);
    }

    private void initRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rv_follow);
        recyclerView.addItemDecoration(new LinearSpacingItemDecoration(10, getContext()));

        adapter = new FollowListAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0 || isLoading || !hasMore) {
                    return;
                }
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                    loadMoreData(true, false);
                }
            }
        });
    }

    private void loadMoreData(boolean skipEmptyPages, boolean showPageLoading) {
        if (isLoading || !hasMore) {
            return;
        }
        isLoading = true;
        final int requestPage = currentPage;
        final boolean keepPageLoading = showPageLoading;
        PSSDK.requestFeedList(requestPage, PAGE_SIZE, new PSSDK.FeedListResultListener() {
            @Override
            public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> result) {
                final boolean resultHasMore = result != null && result.hasMore;
                final List<ShortPlay> source = result == null || result.dataList == null ? Collections.emptyList() : result.dataList;
                HistoryDatabase.executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<FollowListAdapter.FollowItem> collectedItems = buildCollectedItems(source);
                        if (!isAdded() || getActivity() == null) {
                            isLoading = false;
                            return;
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isLoading = false;
                                hasMore = resultHasMore;
                                if (resultHasMore) {
                                    currentPage = requestPage + 1;
                                }

                                if (requestPage == 1) {
                                    adapter.setData(collectedItems);
                                } else if (!collectedItems.isEmpty()) {
                                    adapter.addData(collectedItems);
                                }

                                boolean needContinueEmptyPage = skipEmptyPages && collectedItems.isEmpty() && hasMore;
                                boolean needContinueFillFirstScreen = keepPageLoading
                                        && adapter.getItemCount() < MIN_FIRST_SCREEN_ITEMS
                                        && hasMore;
                                if (needContinueEmptyPage || needContinueFillFirstScreen) {
                                    loadMoreData(true, keepPageLoading);
                                    return;
                                }
                                if (keepPageLoading) {
                                    setLoadingVisible(false);
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onFail(PSSDK.ErrorInfo errorInfo) {
                Logs.i(TAG, "loadMoreData-onFail-errorInfo=" + errorInfo);
                isLoading = false;
                if (keepPageLoading) {
                    setLoadingVisible(false);
                }
            }
        });
    }

    @NonNull
    private List<FollowListAdapter.FollowItem> buildCollectedItems(@NonNull List<ShortPlay> source) {
        List<FollowListAdapter.FollowItem> result = new ArrayList<>();
        for (ShortPlay shortPlay : source) {
            if (shortPlay == null || !shortPlay.isCollected) {
                continue;
            }
            int playIndex = 1;
            if (historyDao != null) {
                HistoryDaoEntity history = historyDao.getEntityByShortId(shortPlay.id);
                if (history != null && history.play_index > 0) {
                    playIndex = history.play_index;
                }
            }
            result.add(new FollowListAdapter.FollowItem(shortPlay, playIndex));
        }
        return result;
    }

    private void setLoadingVisible(boolean visible) {
        if (loadingLayout == null) {
            return;
        }
        loadingLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
