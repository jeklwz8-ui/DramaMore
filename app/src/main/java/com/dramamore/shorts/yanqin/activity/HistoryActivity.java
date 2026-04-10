package com.dramamore.shorts.yanqin.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.adapter.HistoryAdapter;
import com.dramamore.shorts.yanqin.adapter.MoreGridSpacingItemDecoration;
import com.dramamore.shorts.yanqin.dao.HistoryDao;
import com.dramamore.shorts.yanqin.database.HistoryDatabase;
import com.dramamore.shorts.yanqin.entity.HistoryDaoEntity;
import com.dramamore.shorts.yanqin.utils.DpUtils;
import com.dramamore.shorts.yanqin.utils.Logs;
import com.dramamore.shorts.yanqin.utils.ScreenAdaptUtils;

import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    private static final int PAGE_SIZE = 20;

    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMore = false;
    private int gridSpanCount = 3;
    private boolean activityDestroyed = false;

    private HistoryAdapter adapter;
    private HistoryDao historyDao;

    public static void start(Context context) {
        context.startActivity(new Intent(context, HistoryActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        HistoryDatabase database = HistoryDatabase.getDatabase(getApplicationContext());
        historyDao = database.historyDao();
        initRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isLoading && currentPage == 1) {
            loadMoreData();
        }
    }

    @Override
    protected void onDestroy() {
        activityDestroyed = true;
        super.onDestroy();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_history);
        gridSpanCount = ScreenAdaptUtils.calcGridSpanCount(this, 120, 3, 6);
        recyclerView.addItemDecoration(new MoreGridSpacingItemDecoration(gridSpanCount, DpUtils.dp2px(this, 10), false));

        adapter = new HistoryAdapter();
        GridLayoutManager layoutManager = new GridLayoutManager(this, gridSpanCount);
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
                    Logs.i(TAG, "onScrolled-load more history");
                    loadMoreData();
                }
            }
        });
    }

    private void loadMoreData() {
        if (isLoading || historyDao == null) {
            return;
        }
        isLoading = true;
        final int requestPage = currentPage;
        HistoryDatabase.executor.execute(() -> {
            int offset = (requestPage - 1) * PAGE_SIZE;
            List<HistoryDaoEntity> pageData = historyDao.getPagedHistories(PAGE_SIZE, offset);
            if (pageData == null) {
                pageData = Collections.emptyList();
            }
            final List<HistoryDaoEntity> finalPageData = pageData;
            final boolean pageHasMore = finalPageData.size() == PAGE_SIZE;
            runOnUiThread(() -> {
                if (isUnavailable()) {
                    return;
                }
                isLoading = false;
                hasMore = pageHasMore;
                if (requestPage == 1) {
                    adapter.setData(finalPageData);
                } else if (!finalPageData.isEmpty()) {
                    adapter.addData(finalPageData);
                }
                currentPage = pageHasMore ? requestPage + 1 : requestPage;
                Logs.i(TAG, "history page loaded, page=" + requestPage + ", size=" + finalPageData.size() + ", hasMore=" + pageHasMore);
            });
        });
    }

    private boolean isUnavailable() {
        if (activityDestroyed || isFinishing()) {
            return true;
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed();
    }
}
