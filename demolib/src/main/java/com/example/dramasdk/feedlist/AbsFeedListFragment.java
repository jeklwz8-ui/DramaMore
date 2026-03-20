package com.example.dramasdk.feedlist;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.example.dramasdk.DramaListAdapter;
import com.example.dramasdk.DramaPlayActivity;
import com.example.dramasdk.R;

import java.util.List;

public abstract class AbsFeedListFragment extends Fragment {
    private static final String TAG = "AbsFeedListFragment";
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    boolean dataInit;
    private ProgressBar progressBar;
    private TextView retryView;
    private TextView dataSizeTV;
    private DramaListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shortplay_feed_list_normal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.loading_bar);
        retryView = view.findViewById(R.id.tv_retry);
        retryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });

        dataSizeTV = view.findViewById(R.id.tv_data_size);

        RecyclerView recyclerView = view.findViewById(R.id.rlv);
        GridLayoutManager layoutManager = new GridLayoutManager(view.getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DramaListAdapter(new DramaListAdapter.OnItemClickListener() {
            @Override
            public void onClickItem(int index) {
                ShortPlay shortPlay = adapter.getDataItem(index);
                showDetailPage(shortPlay);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void showDetailPage(ShortPlay shortPlay) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        DramaPlayActivity.start(activity, shortPlay);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!dataInit) {
            loadData();
        }
    }

    private void loadData() {
        dataInit = true;
        progressBar.setVisibility(View.VISIBLE);
        retryView.setVisibility(View.GONE);
        dataSizeTV.setVisibility(View.GONE);
        adapter.clearData();

        loadData(new ShortplayListListener());
    }

    protected abstract void loadData(ShortplayListListener shortplayListListener);

    public abstract String getTitle();

    public void clearData(boolean needReload) {
        if (adapter == null) {
            return;
        }
        adapter.clearData();
        dataInit = false;
        if (needReload) {
            loadData();
        }
    }

    protected void onDataLoaded(List<ShortPlay> dataList) {
    }

    protected class ShortplayListListener implements PSSDK.FeedListResultListener {

        @Override
        public void onFail(PSSDK.ErrorInfo errorInfo) {
            Log.d(TAG, "onFail() called with: errorInfo = [" + errorInfo + "]");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    retryView.setVisibility(View.VISIBLE);
                    dataSizeTV.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> result) {
            Log.d(TAG, "onSuccess() called with: result = [" + result + "]");
            // 短剧列表
            List<ShortPlay> dataList = result.dataList;
            // 分页请求，是否还有更多数据
            boolean hasMore = result.hasMore;

            onDataLoaded(dataList);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    adapter.addData(result.dataList);
                    dataSizeTV.setVisibility(View.VISIBLE);
                    dataSizeTV.setText(result.dataList.size() + "项数据");
                    progressBar.setVisibility(View.GONE);
                    if (result.dataList.isEmpty()) {
                        Toast.makeText(getContext(), "没有数据了", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
