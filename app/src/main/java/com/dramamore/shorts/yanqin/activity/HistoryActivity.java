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
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.adapter.HistoryAdapter;
import com.dramamore.shorts.yanqin.adapter.MoreGridSpacingItemDecoration;
import com.dramamore.shorts.yanqin.adapter.ShortPlayAdapter;
import com.dramamore.shorts.yanqin.dao.HistoryDao;
import com.dramamore.shorts.yanqin.database.FollowDatabase;
import com.dramamore.shorts.yanqin.database.HistoryDatabase;
import com.dramamore.shorts.yanqin.entity.FollowDaoEntity;
import com.dramamore.shorts.yanqin.entity.HistoryDaoEntity;
import com.dramamore.shorts.yanqin.utils.DpUtils;
import com.dramamore.shorts.yanqin.utils.Logs;

import java.util.Arrays;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "MoreActivity";
    private int currentPage = 1;
    private boolean isLoading = false, hasMore = false;
    private final int PAGE_SIZE = 20; // 每页数量
    private HistoryAdapter adapter;
    private HistoryDao historyDao;

    public static void start(Context context){
        context.startActivity(new Intent(context,HistoryActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 建议在 onCreate 顶部调用
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
// 设置状态栏颜色为透明
        getWindow().setStatusBarColor(Color.TRANSPARENT);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        HistoryDatabase dp=HistoryDatabase.getDatabase(getApplicationContext());
        historyDao=dp.historyDao();
        initRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoading && currentPage==1) {
            loadMoreData();
        }
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_history);
        recyclerView.addItemDecoration(new MoreGridSpacingItemDecoration(3, DpUtils.dp2px(this, 10), false));

        adapter = new HistoryAdapter();

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
        // 在子线程中执行数据库操作
        HistoryDatabase.executor.execute(new Runnable() {
            @Override
            public void run() {
                // 1. 获取当前页数据
                int offset = (currentPage - 1) * PAGE_SIZE;
                List<HistoryDaoEntity> newData =historyDao.getPagedHistories(PAGE_SIZE, (currentPage - 1) * offset);
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

    private void updateUI(List<HistoryDaoEntity> newData) {
        Logs.i(TAG, "updateUI-newData.size=" + newData.size());
        runOnUiThread(new Runnable() {
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