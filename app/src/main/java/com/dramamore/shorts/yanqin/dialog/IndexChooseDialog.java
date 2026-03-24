package com.dramamore.shorts.yanqin.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.compose.ui.unit.Dp;
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

/**
 * 切换视频分辨率、剧集等弹窗
 */
public class IndexChooseDialog extends Dialog implements IIndexChooseListener {
    private final ShortPlay shortPlay;
    private final int currentIndex;
    private final IIndexChooseListener indexChooseListener;

    public IndexChooseDialog(Context context, ShortPlay shortPlay, int currentIndex, IIndexChooseListener indexChooseListener) {
        super(context);
        this.shortPlay = shortPlay;
        this.currentIndex = currentIndex;
        this.indexChooseListener = indexChooseListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_choose_index);

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        getWindow().setBackgroundDrawable(null);
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.gravity = Gravity.BOTTOM;

        ImageView coverIV = findViewById(R.id.iv_cover);
        Glide.with(coverIV).load(shortPlay.coverImage).into(coverIV);

        TextView titleTV = findViewById(R.id.tv_shortplay_title);
        titleTV.setText(shortPlay.title);

        TextView descTV = findViewById(R.id.tv_shortplay_desc);
        descTV.setText(getContext().getString(R.string.s_finish)+" | " + shortPlay.total + " "+getContext().getString(R.string.s_eps));

        findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        TabLayout tabLayout = findViewById(R.id.tab_layout);

        ViewPager2 viewPager2 = findViewById(R.id.view_pager);

        ArrayList<IndexTabData> indexTabDatas = new ArrayList<>();
        int tabCount = shortPlay.total % 30 == 0 ? shortPlay.total / 30 : shortPlay.total / 30 + 1;
        for (int i = 0; i < tabCount; i++) {
            if (i == tabCount - 1) {
                indexTabDatas.add(new IndexTabData(i * 30 + 1, shortPlay.total));
            } else {
                indexTabDatas.add(new IndexTabData(i * 30 + 1, (i + 1) * 30));
            }
        }

        IndexTabListAdapter indexTabsAdapter = new IndexTabListAdapter(indexTabDatas, currentIndex, this);
        viewPager2.setAdapter(indexTabsAdapter);

        new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                TextView textView = new TextView(getContext());
                IndexTabData tabData = indexTabDatas.get(position);
                textView.setText(tabData.startIndex + "-" + tabData.endIndex);
                textView.setTextSize(14);
                textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                // 判断是否是第一个 Tab (默认选中项)
                if (position == 0) {
                    textView.setTextColor(Color.WHITE);
                } else {
                    textView.setTextColor(Color.parseColor("#FF8D8D8D"));
                }

                tab.setCustomView(textView);
            }
        }).attach();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView textView = (TextView) tab.getCustomView();
                textView.setTextColor(Color.WHITE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView textView = (TextView) tab.getCustomView();
                textView.setTextColor(Color.parseColor("#FF8D8D8D"));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onChooseIndex(int index) {
        dismiss();
        if (indexChooseListener != null) {
            indexChooseListener.onChooseIndex(index);
        }
    }

    static class IndexTabData {
        public int startIndex;
        public int endIndex;

        public IndexTabData(int startIndex, int endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }

    private static class IndexTabListAdapter extends RecyclerView.Adapter<IndexTabVH> {
        private final ArrayList<IndexTabData> indexTabDatas;
        private final int currentPlayingIndex;
        private final IIndexChooseListener chooseListener;

        public IndexTabListAdapter(ArrayList<IndexTabData> indexTabDatas, int currentPlayingIndex, IIndexChooseListener chooseListener) {
            this.indexTabDatas = indexTabDatas;
            this.currentPlayingIndex = currentPlayingIndex;
            this.chooseListener = chooseListener;
        }

        @NonNull
        @Override
        public IndexTabVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView recyclerView = new RecyclerView(parent.getContext());
            recyclerView.setLayoutManager(new GridLayoutManager(parent.getContext(), 4));
            recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return new IndexTabVH(recyclerView, chooseListener);
        }

        @Override
        public void onBindViewHolder(@NonNull IndexTabVH holder, int position) {
            holder.bindData(indexTabDatas.get(position), currentPlayingIndex);
        }

        @Override
        public int getItemCount() {
            return indexTabDatas.size();
        }
    }

    private static class IndexTabVH extends RecyclerView.ViewHolder {
        @NonNull
        private final RecyclerView recyclerView;
        private final IIndexChooseListener onItemClickListener;

        public IndexTabVH(@NonNull RecyclerView recyclerView, IIndexChooseListener onItemClickListener) {
            super(recyclerView);
            this.recyclerView = recyclerView;
            this.onItemClickListener = onItemClickListener;
        }

        public void bindData(IndexTabData tabData, int currentPlayingIndex) {
            recyclerView.setAdapter(new IndexListAdapter(tabData, currentPlayingIndex, onItemClickListener));
        }
    }

    private static class IndexListAdapter extends RecyclerView.Adapter<IndexItemVH> {

        private final IndexTabData indexTabData;
        private final int currentIndex;
        private final IIndexChooseListener onItemClickListener;

        public IndexListAdapter(IndexTabData indexTabData, int currentIndex, IIndexChooseListener onItemClickListener) {
            this.indexTabData = indexTabData;
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
            holder.bindData(indexTabData.startIndex + position, currentIndex);
        }

        @Override
        public int getItemCount() {
            return indexTabData.endIndex - indexTabData.startIndex + 1;
        }
    }

    private static class IndexItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView tvContent;
        private final IIndexChooseListener onItemClickListener;
        private final ImageView statusView;
        private int index;

        public IndexItemVH(@NonNull View itemView, IIndexChooseListener onItemClickListener) {
            super(itemView);
            tvContent = new TextView(itemView.getContext());
            tvContent.setGravity(Gravity.CENTER);
            tvContent.setTextColor(Color.WHITE);
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

        public void bindData(int index, int currentIndex) {
            this.index = index;
            tvContent.setText("" + index);
            tvContent.setSelected(index == currentIndex);
            statusView.setVisibility(index == currentIndex ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onChooseIndex(this.index);
        }
    }
}
