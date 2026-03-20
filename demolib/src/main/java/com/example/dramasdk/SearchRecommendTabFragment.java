package com.example.dramasdk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;

import java.util.ArrayList;
import java.util.List;

public class SearchRecommendTabFragment extends AppFragment implements PSSDK.FeedListResultListener, DataLoadingTipView.RetryClickListener, DramaListAdapter.OnItemClickListener {
    public static final int TYPE_HOT = 1;
    public static final int TYPE_NEW = 2;
    private boolean init;
    private int contentType;
    private DataLoadingTipView dataLoadingTipView;
    private DramaListAdapter adapter;

    public static SearchRecommendTabFragment newInstance(int type) {
        SearchRecommendTabFragment fragment = new SearchRecommendTabFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            contentType = arguments.getInt("type");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_recommend_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (contentType == 0) {
            return;
        }
        TextView titleTV = view.findViewById(R.id.title_tv);
        titleTV.setText(contentType == TYPE_HOT ? "Hot" : "New");
        view.setBackgroundResource(contentType == TYPE_HOT ? R.drawable.bg_hot_card : R.drawable.bg_new_card);
        ViewGroup.LayoutParams rootLP = view.getLayoutParams();
        if (rootLP instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) rootLP).rightMargin = DemoUtils.dp2Px(view.getContext(), 14);
        }

        dataLoadingTipView = view.findViewById(R.id.data_loading_tip_view);
        dataLoadingTipView.setRetryClickListener(this);

        RecyclerView recyclerView = view.findViewById(R.id.rlv);
        adapter = new DramaListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(adapter);
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
        dataLoadingTipView.onStartLoading();
        PSSDK.requestPopularDrama(1, 10, this);
    }

    @Override
    public void onFail(PSSDK.ErrorInfo errorInfo) {
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                toast("加载失败，" + errorInfo.code + ", " + errorInfo.msg);
                dataLoadingTipView.onLoadingFinish(false);
            }
        });
    }

    @Override
    public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> result) {
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                adapter.setData(result.dataList);
                dataLoadingTipView.onLoadingFinish(true);
            }
        });
    }

    @Override
    public void onClickRetry() {
        loadData();
    }

    @Override
    public void onClickItem(int index) {
        ShortPlay item = adapter.getItem(index);
        DramaPlayActivity.start(getActivity(), item);
    }

    private static class DramaListAdapter extends RecyclerView.Adapter<FeedListVH> implements com.example.dramasdk.DramaListAdapter.OnItemClickListener {

        private final List<ShortPlay> dataList = new ArrayList<>();
        private final com.example.dramasdk.DramaListAdapter.OnItemClickListener itemClickListener;

        public DramaListAdapter(com.example.dramasdk.DramaListAdapter.OnItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @NonNull
        @Override
        public FeedListVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FeedListVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_recommend_list, parent, false), itemClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull FeedListVH holder, int position) {
            holder.bind(dataList.get(position));
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        public void setData(List<ShortPlay> dataList) {
            this.dataList.clear();
            this.dataList.addAll(dataList);
            notifyDataSetChanged();
        }

        @Override
        public void onClickItem(int index) {

        }

        public ShortPlay getItem(int index) {
            return dataList.get(index);
        }
    }

    private static class FeedListVH extends RecyclerView.ViewHolder {

        private final ImageView coverIV;
        private final TextView descTV;
        private final TextView titleTV;

        public FeedListVH(@NonNull View itemView, com.example.dramasdk.DramaListAdapter.OnItemClickListener clickListener) {
            super(itemView);

            coverIV = itemView.findViewById(R.id.iv_cover);
            descTV = itemView.findViewById(R.id.tv_desc);
            titleTV = itemView.findViewById(R.id.tv_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onClickItem(getBindingAdapterPosition());
                    }
                }
            });
        }

        public void bind(ShortPlay shortPlay) {
            String coverImage = shortPlay.coverImage;
            Glide.with(itemView.getContext()).load(coverImage).into(coverIV);

            descTV.setText(shortPlay.total + " Eps");
            titleTV.setText(shortPlay.title);
        }
    }
}
