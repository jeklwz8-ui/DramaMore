package com.dramamore.shorts.yanqin.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;

import java.util.Arrays;

import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.adapter.GridSpacingItemDecoration;
import com.dramamore.shorts.yanqin.adapter.LinearSpacingItemDecoration;
import com.dramamore.shorts.yanqin.adapter.SearchListAdapter;
import com.dramamore.shorts.yanqin.utils.DpUtils;
import com.dramamore.shorts.yanqin.utils.Logs;
import com.dramamore.shorts.yanqin.utils.SPUtils;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private int currentPage = 1;
    private boolean isLoading = false,hasMore=false;
    private SearchListAdapter adapter;
    private TextView tvSearch;
    private EditText etSearch;
    private static final String HISTORY_KEY="history_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvSearch = findViewById(R.id.tv_search);
        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage = 1;
                loadMoreData();
            }
        });
        etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                currentPage = 1;
                loadMoreData();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {

                currentPage = 1;
                loadMoreData();
                return true;
            }
            return false;
        });

        /*LinearLayout llSearchHis = findViewById(R.id.ll_search_his);
        String hisStr = SPUtils.getInstance(this).getString(HISTORY_KEY, "");
        String[] hisTags = hisStr.split("_", 5);
        for(int i=0;i<hisTags.length-1;i++){
            TextView textView=new TextView(this);
            textView.setText(hisTags[i]);
            textView.setTextSize(14);
            textView.setTextColor(Color.WHITE);
            llSearchHis.addView(textView);
        }*/

        initRecyclerView();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_bottom);
        recyclerView.addItemDecoration(new LinearSpacingItemDecoration(10,this));

        adapter = new SearchListAdapter();

        // 1. 设置三列网格
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
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
        /**
         * 搜索短剧
         *
         * @param keyword   关键词，不能为空
         * @param isFuzzy   是否模糊搜索
         * @param pageIndex 分页索引，从1开始
         * @param pageCount 分页大小
         */
        PSSDK.searchDrama(etSearch.getText().toString().trim(), true, currentPage, 20, feedListLoadResult);
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

                        hasMore=result.hasMore;
                        if (result.hasMore) {
                            currentPage++; // 只有还有更多数据时才增加页码
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
            if (errorInfo.code == 10013 && adapter != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setData(Arrays.asList());
                    }
                });
            }
        }
    };


}