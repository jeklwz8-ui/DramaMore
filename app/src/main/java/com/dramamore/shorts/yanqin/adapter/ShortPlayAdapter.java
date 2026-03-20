package com.dramamore.shorts.yanqin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.activity.DramaPlayActivity;
import com.dramamore.shorts.yanqin.utils.ShortUtils;
import com.dramamore.shorts.yanqin.widget.RoundImageView;

import java.util.ArrayList;
import java.util.List;

public class ShortPlayAdapter extends RecyclerView.Adapter<ShortPlayAdapter.ViewHolder> {
    private List<ShortPlay> dataList = new ArrayList<>();

    public void setData(List<ShortPlay> list) {
        this.dataList.clear();
        this.dataList.addAll(list);
        notifyDataSetChanged();
    }

    public void addData(List<ShortPlay> list) {
        int startPos = this.dataList.size();
        this.dataList.addAll(list);
        notifyItemRangeInserted(startPos, list.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShortPlay item = dataList.get(position);
        holder.tvName.setText(item.title); // 假设字段名为 playName
        holder.tvEpisode.setText(item.total + holder.itemView.getContext().getResources().getString(R.string.s_eps));

        // 使用你之前的转换方法显示热度
        holder.tvHotValue.setText(ShortUtils.convertToK(item.totalCollectCount));

        // 使用 Glide 或其他框架加载封面
        Glide.with(holder.icCover.getContext())
                .load(item.coverImage)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.icCover);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DramaPlayActivity.start((AppCompatActivity)v.getContext(),item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RoundImageView icCover;
        TextView tvHotValue, tvEpisode, tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icCover = itemView.findViewById(R.id.ic_cover);
            icCover.setRadius(10);
            tvHotValue = itemView.findViewById(R.id.tv_hot_value);
            tvEpisode = itemView.findViewById(R.id.tv_episode);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }
}

