package com.dramamore.shorts.yanqin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.activity.DramaPlayActivity;
import com.dramamore.shorts.yanqin.widget.RoundImageView;

import java.util.ArrayList;
import java.util.List;

public class FollowListAdapter extends RecyclerView.Adapter<FollowListAdapter.ViewHolder> {
    private final List<FollowItem> dataList = new ArrayList<>();

    public void setData(List<FollowItem> list) {
        this.dataList.clear();
        this.dataList.addAll(list);
        notifyDataSetChanged();
    }

    public void addData(List<FollowItem> list) {
        int startPos = this.dataList.size();
        this.dataList.addAll(list);
        notifyItemRangeInserted(startPos, list.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_follow_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        FollowItem followItem = dataList.get(position);
        ShortPlay item = followItem.shortPlay;

        holder.tvName.setText(item.title);
        holder.tvEpisode.setText(item.total + context.getResources().getString(R.string.s_eps));
        holder.tvDesc.setText(item.desc);
        holder.tvSawNum.setText(context.getString(R.string.s_saw_num) + followItem.playIndex + context.getString(R.string.s_eps));

        Glide.with(holder.icCover.getContext())
                .load(item.coverImage)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.icCover);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DramaPlayActivity.start(((AppCompatActivity) context), item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class FollowItem {
        public final ShortPlay shortPlay;
        public final int playIndex;

        public FollowItem(@NonNull ShortPlay shortPlay, int playIndex) {
            this.shortPlay = shortPlay;
            this.playIndex = Math.max(1, playIndex);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final RoundImageView icCover;
        final TextView tvSawNum;
        final TextView tvEpisode;
        final TextView tvName;
        final TextView tvDesc;
        final LinearLayout llTagLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icCover = itemView.findViewById(R.id.ic_cover);
            icCover.setRadius(10);
            tvEpisode = itemView.findViewById(R.id.tv_episode);
            tvName = itemView.findViewById(R.id.tv_name);
            llTagLayout = itemView.findViewById(R.id.ll_tag_layout);
            tvSawNum = itemView.findViewById(R.id.tv_saw_num);
            tvDesc = itemView.findViewById(R.id.tv_desc);
        }
    }
}
