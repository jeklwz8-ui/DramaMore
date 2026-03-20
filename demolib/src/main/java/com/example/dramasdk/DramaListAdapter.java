package com.example.dramasdk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bytedance.sdk.shortplay.api.ShortPlay;

import java.util.ArrayList;
import java.util.List;

public class DramaListAdapter extends RecyclerView.Adapter<DramaListAdapter.FeedListVH> {
    private final List<ShortPlay> dataList = new ArrayList<>();
    private final OnItemClickListener itemClickListener;

    public DramaListAdapter(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public FeedListVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FeedListVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shortplay, parent, false), itemClickListener);
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
        this.dataList.addAll(newData);
        notifyDataSetChanged();
    }

    public ShortPlay getDataItem(int index) {
        if (index < 0 || index >= dataList.size()) {
            return null;
        }
        return dataList.get(index);
    }

    public void clearData() {
        int size = this.dataList.size();
        this.dataList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public interface OnItemClickListener {
        void onClickItem(int index);
    }

    public static class FeedListVH extends RecyclerView.ViewHolder {

        private final ImageView coverIV;
        private final TextView titleTV;
        private final TextView countTV;
        private final TextView descTV;
        private final TextView idTV;

        public FeedListVH(@NonNull View itemView, OnItemClickListener clickListener) {
            super(itemView);
            coverIV = itemView.findViewById(R.id.iv_cover);
            titleTV = itemView.findViewById(R.id.tv_title);
            countTV = itemView.findViewById(R.id.tv_count);
            descTV = itemView.findViewById(R.id.tv_desc);
            idTV = itemView.findViewById(R.id.tv_id);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onClickItem(getAdapterPosition());
                }
            });
        }

        public void bindData(ShortPlay shortPlay) {
            titleTV.setText(shortPlay.title);
            countTV.setText("共" + shortPlay.total + "集");
            descTV.setText(shortPlay.desc);
            idTV.setText("" + shortPlay.id);
            String coverImage = shortPlay.coverImage;
            Glide.with(itemView.getContext()).load(coverImage).into(coverIV);
        }
    }
}
