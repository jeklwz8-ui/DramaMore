package com.example.dramasdk;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppFragmentActivity implements PSSDK.FeedListResultListener, DramaListAdapter.OnItemClickListener {

    private static final String SP_SEARCH_HISTORY = "search_history";
    private EditText inputET;
    private View searchBtn;
    private SearchResultAdapter adapter;
    private View searchHistory;
    private View recommendLayout;
    private DataLoadingTipView loadingTipView;
    private SharedPreferences localSP;
    private Set<String> searchHistoryKeywords;
    private String lastSearchKeyword;
    private RecyclerView searchResultRV;
    private int searchDataPageIndex = 1;
    private boolean hasMoreData;
    private View noContentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, SearchRecommendTabFragment.newInstance(SearchRecommendTabFragment.TYPE_HOT)).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, SearchRecommendTabFragment.newInstance(SearchRecommendTabFragment.TYPE_NEW)).commit();

        searchBtn = findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
        searchBtn.setEnabled(false);
        searchBtn.setAlpha(0.5f);

        inputET = findViewById(R.id.et_keyword);
        inputET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                    return true;
                }
                return false;
            }
        });
        inputET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean hasContent = !TextUtils.isEmpty(s.toString());
                searchBtn.setEnabled(hasContent);
                searchBtn.setAlpha(hasContent ? 1f : 0.5f);
            }
        });

        searchHistory = findViewById(R.id.search_history_layout);
        recommendLayout = findViewById(R.id.recommend_layout);

        searchResultRV = findViewById(R.id.search_result_list);
        searchResultRV.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchResultAdapter(this);
        searchResultRV.setAdapter(adapter);
        searchResultRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (hasMoreData && recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange()) {
                    loadMore();
                }
            }
        });

        loadingTipView = findViewById(R.id.loading_view);

        noContentView = findViewById(R.id.no_content_view);

        localSP = getSharedPreferences(DemoUtils.SP_FILE_NAME, MODE_PRIVATE);
        searchHistoryKeywords = localSP.getStringSet(SP_SEARCH_HISTORY, null);
        if (searchHistoryKeywords == null || searchHistoryKeywords.isEmpty()) {
            searchHistory.setVisibility(View.GONE);
        } else {
            searchHistory.setVisibility(View.VISIBLE);
            LinearLayout searchHistoryLayout = findViewById(R.id.search_history_container);
            for (String s : searchHistoryKeywords) {
                TextView tv = new TextView(this);
                tv.setText(s);
                tv.setTextColor(Color.BLACK);
                tv.setBackground(getResources().getDrawable(R.drawable.bg_round_white_cirle));
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inputET.setText(s);
                        search();
                    }
                });
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, DemoUtils.dp2Px(this, 24));
                params.leftMargin = DemoUtils.dp2Px(this, 10);
                searchHistoryLayout.addView(tv, params);
            }
        }

        findViewById(R.id.btn_clean_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localSP.edit().remove(SP_SEARCH_HISTORY).commit();
                searchHistory.setVisibility(View.GONE);
                searchHistoryKeywords.clear();
            }
        });
    }

    private void loadMore() {
        PSSDK.searchDrama(lastSearchKeyword, true, searchDataPageIndex + 1, 10, this);
    }

    private void search() {
        String keyword = inputET.getText().toString();
        if (TextUtils.isEmpty(keyword)) {
            return;
        }
        hasMoreData = false;
        searchDataPageIndex = 1;
        lastSearchKeyword = keyword;
        loadingTipView.onStartLoading();
        loadingTipView.setVisibility(View.VISIBLE);
        recommendLayout.setVisibility(View.GONE);
        searchResultRV.setVisibility(View.GONE);
        noContentView.setVisibility(View.GONE);
        hideSoftKeyboard();
        adapter.clearData();
        PSSDK.searchDrama(keyword, true, searchDataPageIndex, 20, this);
    }

    public void hideSoftKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onFail(PSSDK.ErrorInfo errorInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errorInfo.code == 10013) {
                    hasMoreData = true;
                    if (adapter.getItemCount() == 0) {
                        loadingTipView.onLoadingFinish(true);
                        noContentView.setVisibility(View.VISIBLE);
                    }
                } else {
                    toast("搜索失败, " + errorInfo.code + ", " + errorInfo.msg);
                    loadingTipView.onLoadingFinish(false);
                }
            }
        });
    }

    @Override
    public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> result) {
        hasMoreData = result.hasMore;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                searchDataPageIndex++;
                if (searchHistoryKeywords == null) {
                    searchHistoryKeywords = new HashSet<>();
                }
                if (adapter.dataList.isEmpty()) {
                    searchHistoryKeywords.add(lastSearchKeyword);
                    localSP.edit().putStringSet(SP_SEARCH_HISTORY, searchHistoryKeywords).commit();
                }

                adapter.appendData(result.dataList);
                if (!result.hasMore) {
                    adapter.appendData(new SearchResultEndItem());
                }
                loadingTipView.onLoadingFinish(true);
                searchResultRV.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClickItem(int index) {
        AbsSearchResultItem searchResultItem = adapter.getItem(index);
        if (searchResultItem instanceof SearchResultDataItem) {
            DramaPlayActivity.start(this, ((SearchResultDataItem) searchResultItem).shortPlay);
        }

    }

    private static class SearchResultAdapter extends RecyclerView.Adapter<SearchResultRV> {
        private final List<AbsSearchResultItem> dataList = new ArrayList<>();
        private final DramaListAdapter.OnItemClickListener clickListener;

        public SearchResultAdapter(com.example.dramasdk.DramaListAdapter.OnItemClickListener clickListener) {
            this.clickListener = clickListener;
        }

        @NonNull
        @Override
        public SearchResultRV onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == AbsSearchResultItem.TYPE_DATA) {
                return new SearchResultDataVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false), clickListener);
            } else {
                FrameLayout frameLayout = new FrameLayout(parent.getContext());
                frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                return new SearchResultEndVH(frameLayout);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull SearchResultRV holder, int position) {
            holder.bind(dataList.get(position));
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return this.dataList.get(position).getType();
        }

        public void appendData(AbsSearchResultItem searchResultItem) {
            int oldSize = this.dataList.size();
            this.dataList.add(searchResultItem);
            notifyItemRangeInserted(oldSize, 1);
        }

        public void appendData(List<ShortPlay> dataList) {
            int oldSize = this.dataList.size();
            for (ShortPlay shortPlay : dataList) {
                this.dataList.add(new SearchResultDataItem(shortPlay));
            }
            notifyItemRangeInserted(oldSize, dataList.size());
        }

        public AbsSearchResultItem getItem(int index) {
            return dataList.get(index);
        }

        public void clearData() {
            int size = dataList.size();
            this.dataList.clear();
            notifyItemRangeRemoved(0, size);
        }
    }


    private static class SearchResultEndVH extends SearchResultRV<SearchResultEndItem> {

        public SearchResultEndVH(@NonNull View itemView) {
            super(itemView);
            FrameLayout rootView = (FrameLayout) itemView;
            ImageView iv = new ImageView(itemView.getContext());
            iv.setImageResource(R.drawable.image_item_end);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            rootView.addView(iv, lp);
        }

        @Override
        public void bind(SearchResultEndItem dataItem) {

        }
    }

    private static class SearchResultDataVH extends SearchResultRV<SearchResultDataItem> {

        private final ImageView coverIV;
        private final TextView countTV;
        private final TextView titleTV;
        private final TextView descTV;

        public SearchResultDataVH(@NonNull View itemView, com.example.dramasdk.DramaListAdapter.OnItemClickListener clickListener) {
            super(itemView);

            coverIV = itemView.findViewById(R.id.iv_cover);
            countTV = itemView.findViewById(R.id.tv_count);
            titleTV = itemView.findViewById(R.id.tv_title);
            descTV = itemView.findViewById(R.id.tv_desc);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onClickItem(getBindingAdapterPosition());
                    }
                }
            });
        }

        public void bind(SearchResultDataItem resultItem) {
            String coverImage = resultItem.shortPlay.coverImage;
            Glide.with(itemView.getContext()).load(coverImage).into(coverIV);

            countTV.setText(resultItem.shortPlay.total + " Eps");
            titleTV.setText(resultItem.shortPlay.title);
            descTV.setText(resultItem.shortPlay.desc);
        }
    }

    private static abstract class SearchResultRV<T extends AbsSearchResultItem> extends RecyclerView.ViewHolder {

        public SearchResultRV(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void bind(T dataItem);
    }

    private static abstract class AbsSearchResultItem {
        public static final int TYPE_DATA = 0;
        public static final int TYPE_END = 1;

        abstract int getType();
    }

    private static class SearchResultEndItem extends AbsSearchResultItem {

        @Override
        int getType() {
            return TYPE_END;
        }
    }

    private static class SearchResultDataItem extends AbsSearchResultItem {

        public final ShortPlay shortPlay;

        public SearchResultDataItem(ShortPlay shortPlay) {
            this.shortPlay = shortPlay;
        }

        @Override
        int getType() {
            return TYPE_DATA;
        }
    }
}
