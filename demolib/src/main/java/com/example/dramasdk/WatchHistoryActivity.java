package com.example.dramasdk;

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

import java.util.ArrayList;

public class WatchHistoryActivity extends AppFragmentActivity implements DramaListAdapter.OnItemClickListener {

    private HistoryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_watch_history);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new HistoryAdapter(PlayHistoryHelper.getPlayHistory(), this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClickItem(int index) {
        PlayHistoryHelper.PlayHistory history = adapter.getItem(index);
        DramaPlayActivity.start(this, history.shortPlay, history.index);
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryVH> {
        private final ArrayList<PlayHistoryHelper.PlayHistory> playHistories = new ArrayList<>();
        private final DramaListAdapter.OnItemClickListener itemClickListener;

        public HistoryAdapter(ArrayList<PlayHistoryHelper.PlayHistory> playHistory, DramaListAdapter.OnItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
            this.playHistories.addAll(playHistory);
        }

        @NonNull
        @Override
        public HistoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new HistoryVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false), itemClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryVH holder, int position) {
            holder.bind(playHistories.get(position));
        }

        @Override
        public int getItemCount() {
            return playHistories.size();
        }

        public PlayHistoryHelper.PlayHistory getItem(int index) {
            return playHistories.get(index);
        }
    }

    private static class HistoryVH extends RecyclerView.ViewHolder {

        private final ImageView cover;
        private final TextView titleView;
        private final TextView descView;

        public HistoryVH(@NonNull View itemView, DramaListAdapter.OnItemClickListener itemClickListener) {
            super(itemView);

            cover = itemView.findViewById(R.id.iv_cover);
            titleView = itemView.findViewById(R.id.tv_title);
            descView = itemView.findViewById(R.id.tv_count);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickListener.onClickItem(getBindingAdapterPosition());
                }
            });
        }

        public void bind(PlayHistoryHelper.PlayHistory playHistory) {
            Glide.with(cover.getContext()).load(playHistory.shortPlay.coverImage).into(cover);
            titleView.setText(playHistory.shortPlay.title);
            descView.setText("第" + playHistory.index + "集/共" + playHistory.shortPlay.total + "集");
        }
    }
}
