package com.example.dramasdk.feedlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.example.dramasdk.AbsTabFragment;
import com.example.dramasdk.DataLoadingTipView;
import com.example.dramasdk.DramaPlayActivity;
import com.example.dramasdk.PlayHistoryHelper;
import com.example.dramasdk.R;
import com.example.dramasdk.SearchActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class HomeTabFragment extends AbsTabFragment implements DataLoadingTipView.RetryClickListener {
    private static final String TAG = "HomeTabFragment";
    private boolean inited;
    private CategoryFeedListAdapter listAdapter;
    private DataLoadingTipView dataLoadingTipView;
    private View lastWatchTipView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.search_entry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SearchActivity.class));
            }
        });

        ViewPager2 viewPager2 = view.findViewById(R.id.home_view_pager);
        listAdapter = new CategoryFeedListAdapter(this);
        viewPager2.setAdapter(listAdapter);
        TabLayout tabLayout = view.findViewById(R.id.home_tab_layout);

        new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                String itemTitle = listAdapter.getItemTitle(position);
                TextView customView = new TextView(tab.view.getContext());
                customView.setTextSize(13);
                customView.setTextColor(Color.parseColor("#192734"));
                customView.setText(itemTitle);
                customView.setAlpha(0.6f);
                tab.setCustomView(customView);
            }
        }).attach();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView textView = (TextView) tab.getCustomView();
                textView.setTypeface(null, Typeface.BOLD);
                textView.setAlpha(0.9f);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView textView = (TextView) tab.getCustomView();
                textView.setTypeface(null, Typeface.NORMAL);
                textView.setAlpha(0.6f);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        dataLoadingTipView = view.findViewById(R.id.data_loading_tip_view);
        dataLoadingTipView.setRetryClickListener(this);

        lastWatchTipView = view.findViewById(R.id.last_watch_layout);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!inited) {
            inited = true;
            loadData();
        }

        if (lastWatchTipView.isEnabled()) {
            PlayHistoryHelper.PlayHistory lastWatchShortPlay = PlayHistoryHelper.getLastWatchShortPlay();
            if (lastWatchShortPlay != null) {
                bindLastWatchTipView(lastWatchShortPlay);
            }
        }
    }

    private void loadData() {
        if (dataLoadingTipView.isDataLoading()) {
            return;
        }

        dataLoadingTipView.onStartLoading();
        String lang = "";
        List<String> contentLanguages = PSSDK.getContentLanguages();
        if (contentLanguages != null && !contentLanguages.isEmpty()) {
            lang = contentLanguages.get(0);
        }
        PSSDK.requestCategoryList(lang, new PSSDK.CategoryListResultListener() {
            @Override
            public void onFail(PSSDK.ErrorInfo errorInfo) {
                Log.d(TAG, "onFail() called with: errorInfo = [" + errorInfo + "]");
                runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        toast("加载失败，" + errorInfo.code + ", " + errorInfo.msg);
                        dataLoadingTipView.onLoadingFinish(false);
                    }
                });
            }

            @Override
            public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay.ShortPlayCategory> result) {
                Log.d(TAG, "onSuccess() called with: result = [" + result + "]");
                runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        // 手动补几个聚合Tab
                        result.dataList.add(0, new ShortPlay.ShortPlayCategory(-2, "Recommend"));
                        result.dataList.add(1, new ShortPlay.ShortPlayCategory(-1, "New"));
                        listAdapter.setCategoryList(result.dataList);
                        dataLoadingTipView.onLoadingFinish(true);
                    }
                });
            }
        });
        // 精选

        // 上新
    }

    private void bindLastWatchTipView(PlayHistoryHelper.PlayHistory playHistory) {
        lastWatchTipView.setVisibility(View.VISIBLE);
        ImageView coverIV = lastWatchTipView.findViewById(R.id.tv_last_watch_cover);
        Glide.with(this).load(playHistory.shortPlay.coverImage).into(coverIV);
        TextView titleTV = lastWatchTipView.findViewById(R.id.tv_last_watch_title);
        titleTV.setText(playHistory.shortPlay.title);
        lastWatchTipView.findViewById(R.id.iv_close_last_watch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastWatchTipView.setVisibility(View.GONE);
                lastWatchTipView.setEnabled(false);
            }
        });
        lastWatchTipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DramaPlayActivity.start(getActivity(), playHistory.shortPlay, playHistory.index, playHistory.seconds);
            }
        });
    }

    @Override
    public String getTitle(Context context) {
        return "Theater";
    }

    @Override
    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(R.drawable.tab_theater_selector);
    }

    @Override
    public void onClickRetry() {
        loadData();
    }

    private static class CategoryFeedListAdapter extends FragmentStateAdapter {

        private final List<ShortPlay.ShortPlayCategory> categories = new ArrayList<>();

        public CategoryFeedListAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            CategoryFeedListFragment categoryFeedListFragment = new CategoryFeedListFragment();
            Bundle args = new Bundle();
            args.putLong("category_id", categories.get(position).id);
            categoryFeedListFragment.setArguments(args);
            return categoryFeedListFragment;
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }


        public String getItemTitle(int position) {
            return categories.get(position).name;
        }

        public void setCategoryList(List<ShortPlay.ShortPlayCategory> dataList) {
            for (ShortPlay.ShortPlayCategory shortPlayCategory : dataList) {
                if (shortPlayCategory.id < 0 || shortPlayCategory.count > 0 && !TextUtils.isEmpty(shortPlayCategory.name)) {
                    this.categories.add(shortPlayCategory);
                }
            }
            notifyDataSetChanged();
        }
    }
}
