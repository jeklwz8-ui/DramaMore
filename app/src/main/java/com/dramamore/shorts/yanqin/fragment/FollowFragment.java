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

import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.adapter.FollowListAdapter;
import com.dramamore.shorts.yanqin.adapter.LinearSpacingItemDecoration;
import com.dramamore.shorts.yanqin.dao.FollowDao;
import com.dramamore.shorts.yanqin.database.FollowDatabase;
import com.dramamore.shorts.yanqin.entity.FollowDaoEntity;
import com.dramamore.shorts.yanqin.utils.Logs;

import java.util.List;

public class FollowFragment extends Fragment {
    private static final String TAG = "FollowFragment";
    private int currentPage = 1;
    private boolean isLoading = false, hasMore = false;
    private final int PAGE_SIZE = 20; // 每页数量
    private FollowListAdapter adapter;
    private FollowDao followDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 建议创建对应的 layout 文件：fragment_home.xml
        View view = inflater.inflate(R.layout.fragment_follow, container, false);
        initRecyclerView(view);
        FollowDatabase db = FollowDatabase.getDatabase(getActivity());
        followDao = db.followDao();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Logs.i(TAG, "onResume---");
        if (!isLoading) {
            currentPage = 1;
            loadMoreData();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Logs.i(TAG, "onHiddenChanged---hidden=" + hidden);
        if (!hidden) {
            // Fragment 从隐藏状态变为显示状态
            if (!isLoading) {
                currentPage = 1;
                loadMoreData();
            }
        }
    }

    private void initRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rv_follow);
        recyclerView.addItemDecoration(new LinearSpacingItemDecoration(10, getContext()));

        adapter = new FollowListAdapter();

        // 1. 设置三列网格
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
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

                    if (!isLoading) {
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
        // 在子线程中执行数据库操作
        FollowDatabase.executor.execute(new Runnable() {
            @Override
            public void run() {
                // 1. 获取当前页数据
                int offset = (currentPage - 1) * PAGE_SIZE;
                List<FollowDaoEntity> newData = followDao.getPagedFollows(PAGE_SIZE, (currentPage - 1) * offset);
                // 2. 判断是否还有更多
                hasMore = newData.size() == PAGE_SIZE;
                if (!newData.isEmpty()) {
                    // 3. 更新偏移量，为下一页做准备
                    // 4. 将数据回调给 UI 层 (比如通过 LiveData 或 Handler)
                    updateUI(newData);
                }
            }
        });
    }

    private void updateUI(List<FollowDaoEntity> newData) {
        Logs.i(TAG, "updateUI-newData.size=" + newData.size());
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isLoading=false;
                if (currentPage == 1) {
                    adapter.setData(newData);
                } else {
                    adapter.addData(newData);
                }
            }
        });
    }
}

