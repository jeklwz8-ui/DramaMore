package com.example.dramasdk.feedlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.example.dramasdk.AppFragment;
import com.example.dramasdk.DataLoadingTipView;
import com.example.dramasdk.DramaListAdapter;
import com.example.dramasdk.DramaPlayActivity;
import com.example.dramasdk.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CategoryFeedListFragment extends AppFragment implements DramaListAdapter.OnItemClickListener, DataLoadingTipView.RetryClickListener {
    private static final int pageCount = 10;
    private static final String TAG = "CategoryFeedListFragmen";
    private boolean init;
    private long categoryId;
    private int pageIndex = 1;
    private DramaListAdapter listAdapter;
    private DataLoadingTipView dataLoadingTipView;
    private boolean moreDataLoading;
    private boolean hasMoreData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            categoryId = arguments.getLong("category_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_feed_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.rlv);
        GridLayoutManager layoutManager = new GridLayoutManager(view.getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        listAdapter = new DramaListAdapter(this);
        recyclerView.setAdapter(listAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (hasMoreData && recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange()) {
                    loadMore();
                }
            }
        });

        dataLoadingTipView = view.findViewById(R.id.data_loading_tip_view);
        dataLoadingTipView.setRetryClickListener(this);
    }

    private void loadMore() {
        if (moreDataLoading) {
            return;
        }
        moreDataLoading = true;
        PSSDK.FeedListResultListener resultListener = new PSSDK.FeedListResultListener() {
            @Override
            public void onFail(PSSDK.ErrorInfo errorInfo) {
                moreDataLoading = false;
                toast("加载失败, " + errorInfo.code + ", " + errorInfo.msg);
            }

            @Override
            public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> result) {
                moreDataLoading = false;
                hasMoreData = result.hasMore;
                runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        pageIndex++;
                        listAdapter.addData(result.dataList);
                    }
                });
            }
        };
        if (categoryId == -2) {
            PSSDK.requestFeedList(pageIndex + 1, pageCount, resultListener);
        } else if (categoryId == -1) {
            PSSDK.requestNewDrama(pageIndex + 1, pageCount, resultListener);
        } else {
            ArrayList<Long> categoryIds = new ArrayList<>();
            categoryIds.add(categoryId);
            PSSDK.requestFeedListByCategoryIds(categoryIds, null, pageIndex + 1, pageCount, resultListener);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (!init) {
            init = true;
            loadData();
        }
    }

    private void loadData() {
        PSSDK.FeedListResultListener resultListener = new PSSDK.FeedListResultListener() {
            @Override
            public void onFail(PSSDK.ErrorInfo errorInfo) {
                runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        dataLoadingTipView.onLoadingFinish(false);
                        toast("加载失败, " + errorInfo.code + ", " + errorInfo.msg);
                    }
                });
            }

            @Override
            public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> result) {
                hasMoreData = result.hasMore;
                runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.addData(result.dataList);
                        dataLoadingTipView.onLoadingFinish(true);
                    }
                });
            }
        };
        if (categoryId == -2) {
            PSSDK.requestFeedList(pageIndex, pageCount, resultListener);
        } else if (categoryId == -1) {
            PSSDK.requestNewDrama(pageIndex, pageCount, resultListener);
        } else {
            ArrayList<Long> categoryIds = new ArrayList<>();
            categoryIds.add(categoryId);
            dataLoadingTipView.onStartLoading();

            PSSDK.requestFeedListByCategoryIds(categoryIds, null, pageIndex, pageCount, resultListener);
        }

    }

    @Override
    public void onClickItem(int index) {
        DramaPlayActivity.start(getActivity(), listAdapter.getDataItem(index));
    }

    @Override
    public void onClickRetry() {
        loadData();
    }

    public static class DramaListAdapter extends RecyclerView.Adapter<FeedListVH> {
        private final List<ShortPlayInfo> dataList = new ArrayList<>();
        private final com.example.dramasdk.DramaListAdapter.OnItemClickListener itemClickListener;

        public DramaListAdapter(com.example.dramasdk.DramaListAdapter.OnItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @NonNull
        @Override
        public FeedListVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FeedListVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shortplay2, parent, false), itemClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull FeedListVH holder, int position) {
            holder.bindData(dataList.get(position));
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        public void addData(List<ShortPlay> newData) {
            int oldSize = this.dataList.size();
            for (ShortPlay shortPlay : newData) {
                this.dataList.add(new ShortPlayInfo(shortPlay));
            }
            notifyItemRangeInserted(oldSize, newData.size());
        }

        public ShortPlay getDataItem(int index) {
            if (index < 0 || index >= dataList.size()) {
                return null;
            }
            return dataList.get(index).shortPlay;
        }
    }

    public static class FeedListVH extends RecyclerView.ViewHolder {

        private final ImageView coverIV;
        private final TextView titleTV;
        private final TextView countTV;
        private final TextView hotValueTV;
        private final TextView tagView;
        private final TextView categoryTV;

        public FeedListVH(@NonNull View itemView, com.example.dramasdk.DramaListAdapter.OnItemClickListener clickListener) {
            super(itemView);
            coverIV = itemView.findViewById(R.id.iv_cover);
            titleTV = itemView.findViewById(R.id.tv_title);
            countTV = itemView.findViewById(R.id.tv_count);
            hotValueTV = itemView.findViewById(R.id.tv_hot_value);
            tagView = itemView.findViewById(R.id.tag_view);
            categoryTV = itemView.findViewById(R.id.tv_category);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onClickItem(getAdapterPosition());
                }
            });
        }

        public void bindData(ShortPlayInfo shortPlayInfo) {
            titleTV.setText(shortPlayInfo.shortPlay.title);
            countTV.setText(shortPlayInfo.shortPlay.total + " Episodes");
            String coverImage = shortPlayInfo.shortPlay.coverImage;
            Glide.with(itemView.getContext()).load(coverImage).into(coverIV);
            // 分类
            ArrayList<ShortPlay.ShortPlayCategory> categories = shortPlayInfo.shortPlay.categories;
            if (categories != null && !categories.isEmpty()) {
                categoryTV.setText(categories.get(0).name);
            }
            // 热度值，随机生成
            hotValueTV.setText(shortPlayInfo.hotValue + "k");
            // 标签
            ArrayList<ShortPlay.Tag> tags = shortPlayInfo.shortPlay.tags;
            tagView.setVisibility(View.INVISIBLE);
            if (tags != null && !tags.isEmpty()) {
                ShortPlay.Tag tag = tags.get(tags.size() - 1);
                if (tag.id == 1L) {// hot
                    tagView.setBackgroundResource(R.drawable.bg_tag_hot);
                    tagView.setText("Hot");
                    tagView.setVisibility(View.VISIBLE);
                } else if (tag.id == 2L) {// New
                    tagView.setBackgroundResource(R.drawable.bg_tag_hot);
                    tagView.setText("New");
                    tagView.setVisibility(View.VISIBLE);
                } else if (tag.id == 4L) {// Collect
                    tagView.setBackgroundResource(R.drawable.bg_tag_hot);
                    tagView.setText("7dCollect");
                    tagView.setVisibility(View.VISIBLE);
                }
            } else if (shortPlayInfo.hotValue > 50) {
                // 对没有下发标签的，随机补一个
                tagView.setBackgroundResource(R.drawable.bg_tag_recommend);
                tagView.setText("Recommend");
                tagView.setVisibility(View.VISIBLE);
            }
        }
    }

    private static class ShortPlayInfo {
        private final ShortPlay shortPlay;
        public int hotValue;

        public ShortPlayInfo(ShortPlay shortPlay) {
            this.shortPlay = shortPlay;
            this.hotValue = new Random().nextInt(100);
        }
    }
}
