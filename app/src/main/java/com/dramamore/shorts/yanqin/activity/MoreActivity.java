package com.dramamore.shorts.yanqin.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;

import java.util.Arrays;

import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.adapter.MoreGridSpacingItemDecoration;
import com.dramamore.shorts.yanqin.adapter.ShortPlayAdapter;
import com.dramamore.shorts.yanqin.utils.DpUtils;
import com.dramamore.shorts.yanqin.utils.Logs;

public class MoreActivity extends AppCompatActivity {

    private static final String TAG = "MoreActivity";
    private static final String EXTRA_TYPE = "extra_type";
    private static final String EXTRA_TITLE = "extra_title";
    private int currentPage = 1;
    private boolean hasMore = false;
    private boolean isLoading = false;
    private ShortPlayAdapter adapter;
    private TextView tvTitle;
    private int type;

    public static void start(Context context,int type,String title){
        Intent intent = new Intent(context, MoreActivity.class);
        intent.putExtra(EXTRA_TYPE, type);
        intent.putExtra(EXTRA_TITLE, title);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_more);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.more), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTitle = findViewById(R.id.tv_title);
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initRecyclerView();

        type = getIntent().getIntExtra(EXTRA_TYPE, 0);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        tvTitle.setText(title);
        Logs.i(TAG, "loadMoreData-type=" + type + ",title=" + title);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoading && currentPage==1) {
            loadMoreData();
        }
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_bottom);
        recyclerView.addItemDecoration(new MoreGridSpacingItemDecoration(3, DpUtils.dp2px(this, 10), false));

        adapter = new ShortPlayAdapter();

        // 1. 设置三列网格
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
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
        if (type == 1) {//热播短剧
            PSSDK.requestPopularDrama(currentPage, 20, Arrays.asList(5L), feedListLoadResult);
        } else if(type==2) {//收藏最多
            PSSDK.requestDramaByTag(4, currentPage, 20, feedListLoadResult);
        }else if(type==3){//动漫短剧
            PSSDK.requestFeedListByCategoryIds(Arrays.asList(1000701l), null, currentPage, 20,feedListLoadResult);
        }
    }

    private PSSDK.FeedListResultListener feedListLoadResult = new PSSDK.FeedListResultListener() {
        @Override
        public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> result) {
            Logs.i(TAG, "loadMoreData-onSuccess-feedListLoadResult-hasMore=" + result.hasMore + ",size=" + result.dataList.size());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isLoading = false;
                    if (result.dataList != null && !result.dataList.isEmpty()) {
                        if (currentPage == 1) {
                            adapter.setData(result.dataList);
                        } else {
                            adapter.addData(result.dataList);
                        }

                        hasMore = result.hasMore;//更多
                        if (result.hasMore) {
                            currentPage++;
                        }
                    }
                }
            });
        }

        @Override
        public void onFail(PSSDK.ErrorInfo errorInfo) {
            Logs.i(TAG, "loadMoreData-onFail-errorInfo=" + errorInfo);
            isLoading = false;
            // 处理错误提示
        }
    };


}