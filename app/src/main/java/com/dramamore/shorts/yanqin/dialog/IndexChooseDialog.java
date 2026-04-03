package com.dramamore.shorts.yanqin.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.listener.IIndexChooseListener;
import com.dramamore.shorts.yanqin.utils.DpUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class IndexChooseDialog extends Dialog implements IIndexChooseListener {
    private static final int PAGE_INTRO = 0;
    private static final int PAGE_EPISODE = 1;
    private static final int EPISODE_PAGE_SIZE = 18;

    private final ShortPlay shortPlay;
    private final int currentIndex;
    private final IIndexChooseListener indexChooseListener;

    public IndexChooseDialog(Context context, ShortPlay shortPlay, int currentIndex, IIndexChooseListener indexChooseListener) {
        super(context);
        this.shortPlay = shortPlay;
        this.currentIndex = currentIndex <= 0 ? 1 : currentIndex;
        this.indexChooseListener = indexChooseListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_choose_index);

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        getWindow().setBackgroundDrawable(null);
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(attributes);

        int totalEpisodes = shortPlay.total > 0
                ? shortPlay.total
                : (shortPlay.episodes == null ? 0 : shortPlay.episodes.size());

        ImageView coverIV = findViewById(R.id.iv_cover);
        Glide.with(coverIV).load(shortPlay.coverImage).into(coverIV);

        TextView titleTV = findViewById(R.id.tv_shortplay_title);
        titleTV.setText(shortPlay.title);

        TextView descTV = findViewById(R.id.tv_shortplay_desc);
        descTV.setText("\u5df2\u5b8c\u7ed3 \u5171" + totalEpisodes + "\u96c6");

        findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager2 = findViewById(R.id.view_pager);
        viewPager2.setOffscreenPageLimit(2);
        viewPager2.setAdapter(new ContentPagerAdapter(shortPlay, totalEpisodes, currentIndex, this));

        new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(position == PAGE_INTRO ? "\u7b80\u4ecb" : "\u9009\u96c6");
            }
        }).attach();
        viewPager2.setCurrentItem(PAGE_EPISODE, false);
    }

    @Override
    public void onChooseIndex(int index) {
        dismiss();
        if (indexChooseListener != null) {
            indexChooseListener.onChooseIndex(index);
        }
    }

    private static class ContentPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_INTRO = 1;
        private static final int TYPE_EPISODE = 2;

        private final ShortPlay shortPlay;
        private final int totalEpisodes;
        private final int currentPlayingIndex;
        private final IIndexChooseListener chooseListener;

        ContentPagerAdapter(ShortPlay shortPlay, int totalEpisodes, int currentPlayingIndex, IIndexChooseListener chooseListener) {
            this.shortPlay = shortPlay;
            this.totalEpisodes = totalEpisodes;
            this.currentPlayingIndex = currentPlayingIndex;
            this.chooseListener = chooseListener;
        }

        @Override
        public int getItemViewType(int position) {
            return position == PAGE_INTRO ? TYPE_INTRO : TYPE_EPISODE;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_INTRO) {
                View view = inflater.inflate(R.layout.item_choose_intro_page, parent, false);
                return new IntroPageVH(view);
            }
            View view = inflater.inflate(R.layout.item_choose_episode_page, parent, false);
            return new EpisodePageVH(view, totalEpisodes, currentPlayingIndex, chooseListener);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof IntroPageVH) {
                ((IntroPageVH) holder).bind(shortPlay.desc);
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    private static class IntroPageVH extends RecyclerView.ViewHolder {
        private final TextView introContentTV;

        IntroPageVH(@NonNull View itemView) {
            super(itemView);
            introContentTV = itemView.findViewById(R.id.tv_intro_content);
        }

        void bind(String content) {
            if (TextUtils.isEmpty(content)) {
                introContentTV.setText("\u6682\u65e0\u7b80\u4ecb");
            } else {
                introContentTV.setText(content);
            }
        }
    }

    private static class EpisodePageVH extends RecyclerView.ViewHolder {
        private final LinearLayout pageTabsContainer;
        private final EpisodeGridAdapter gridAdapter;
        private final List<EpisodeRange> episodeRanges;
        private int currentPageIndex;

        EpisodePageVH(@NonNull View itemView, int totalEpisodes, int currentPlayingIndex, IIndexChooseListener onItemClickListener) {
            super(itemView);
            pageTabsContainer = itemView.findViewById(R.id.ll_episode_page_tabs);
            RecyclerView recyclerView = itemView.findViewById(R.id.rv_episode);
            recyclerView.setLayoutManager(new GridLayoutManager(itemView.getContext(), 4));

            episodeRanges = buildEpisodeRanges(totalEpisodes);
            currentPageIndex = findPageIndexByEpisode(episodeRanges, currentPlayingIndex);
            EpisodeRange initialRange = episodeRanges.get(currentPageIndex);

            gridAdapter = new EpisodeGridAdapter(initialRange.start, initialRange.end, currentPlayingIndex, onItemClickListener);
            recyclerView.setAdapter(gridAdapter);

            renderPageTabs();
        }

        private void renderPageTabs() {
            pageTabsContainer.removeAllViews();
            Context context = itemView.getContext();
            for (int i = 0; i < episodeRanges.size(); i++) {
                final int pageIndex = i;
                EpisodeRange range = episodeRanges.get(i);
                TextView tabView = new TextView(context);
                tabView.setText(range.start + "-" + range.end);
                tabView.setTextSize(14f);
                tabView.setIncludeFontPadding(false);
                tabView.setTextColor(pageIndex == currentPageIndex ? Color.WHITE : Color.parseColor("#7FFFFFFF"));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                if (i > 0) {
                    lp.leftMargin = DpUtils.dp2px(context, 32);
                }
                tabView.setLayoutParams(lp);
                tabView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (currentPageIndex == pageIndex) {
                            return;
                        }
                        currentPageIndex = pageIndex;
                        EpisodeRange selectedRange = episodeRanges.get(currentPageIndex);
                        gridAdapter.updateRange(selectedRange.start, selectedRange.end);
                        updatePageTabStyles();
                    }
                });
                pageTabsContainer.addView(tabView);
            }
            updatePageTabStyles();
        }

        private void updatePageTabStyles() {
            for (int i = 0; i < pageTabsContainer.getChildCount(); i++) {
                View child = pageTabsContainer.getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setTextColor(i == currentPageIndex ? Color.WHITE : Color.parseColor("#7FFFFFFF"));
                }
            }
        }

        @NonNull
        private static List<EpisodeRange> buildEpisodeRanges(int totalEpisodes) {
            int safeTotal = Math.max(totalEpisodes, 1);
            List<EpisodeRange> ranges = new ArrayList<>();
            int start = 1;
            while (start <= safeTotal) {
                int end = Math.min(start + EPISODE_PAGE_SIZE - 1, safeTotal);
                ranges.add(new EpisodeRange(start, end));
                start = end + 1;
            }
            return ranges;
        }

        private static int findPageIndexByEpisode(@NonNull List<EpisodeRange> ranges, int episode) {
            int target = episode <= 0 ? 1 : episode;
            for (int i = 0; i < ranges.size(); i++) {
                EpisodeRange range = ranges.get(i);
                if (target >= range.start && target <= range.end) {
                    return i;
                }
            }
            return 0;
        }
    }

    private static class EpisodeRange {
        final int start;
        final int end;

        EpisodeRange(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private static class EpisodeGridAdapter extends RecyclerView.Adapter<IndexItemVH> {
        private int startEpisode;
        private int endEpisode;
        private final int currentIndex;
        private final IIndexChooseListener onItemClickListener;

        EpisodeGridAdapter(int startEpisode, int endEpisode, int currentIndex, IIndexChooseListener onItemClickListener) {
            this.startEpisode = startEpisode;
            this.endEpisode = endEpisode;
            this.currentIndex = currentIndex;
            this.onItemClickListener = onItemClickListener;
        }

        @NonNull
        @Override
        public IndexItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            FrameLayout layout = new FrameLayout(parent.getContext());
            return new IndexItemVH(layout, onItemClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull IndexItemVH holder, int position) {
            holder.bindData(startEpisode + position, currentIndex);
        }

        @Override
        public int getItemCount() {
            return Math.max(endEpisode - startEpisode + 1, 0);
        }

        void updateRange(int startEpisode, int endEpisode) {
            this.startEpisode = startEpisode;
            this.endEpisode = endEpisode;
            notifyDataSetChanged();
        }
    }

    private static class IndexItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tvContent;
        private final IIndexChooseListener onItemClickListener;
        private final ImageView statusView;
        private int index;

        IndexItemVH(@NonNull View itemView, IIndexChooseListener onItemClickListener) {
            super(itemView);
            tvContent = new TextView(itemView.getContext());
            tvContent.setGravity(Gravity.CENTER);
            tvContent.setTextColor(0xFFFFFFFF);
            tvContent.setPadding(0, 40, 0, 40);
            tvContent.setBackground(itemView.getResources().getDrawable(R.drawable.bg_index_round));
            FrameLayout.LayoutParams contentLP = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            contentLP.gravity = Gravity.CENTER;
            contentLP.leftMargin = contentLP.rightMargin = DpUtils.dp2px(itemView.getContext(), 4);
            contentLP.topMargin = contentLP.bottomMargin = DpUtils.dp2px(itemView.getContext(), 3);
            ((FrameLayout) itemView).addView(tvContent, contentLP);

            statusView = new ImageView(itemView.getContext());
            FrameLayout.LayoutParams statusLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            statusLP.gravity = Gravity.RIGHT | Gravity.TOP;
            statusLP.topMargin = statusLP.rightMargin = DpUtils.dp2px(itemView.getContext(), 8);
            statusView.setImageResource(R.drawable.ic_status);
            ((FrameLayout) itemView).addView(statusView, statusLP);

            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);
        }

        void bindData(int index, int currentIndex) {
            this.index = index;
            tvContent.setText(String.valueOf(index));
            tvContent.setSelected(index == currentIndex);
            statusView.setVisibility(index == currentIndex ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onChooseIndex(this.index);
            }
        }
    }
}
