package com.dramamore.shorts.yanqin.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.activity.MoreActivity;
import com.dramamore.shorts.yanqin.activity.DramaPlayActivity;
import com.dramamore.shorts.yanqin.activity.SearchActivity;
import com.dramamore.shorts.yanqin.banner.BannerManager;
import com.dramamore.shorts.yanqin.utils.ShortUtils;
import com.dramamore.shorts.yanqin.widget.RoundImageView;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_HEADER = 1;
    public final int TYPE_LIST = 2;
    private List<ShortPlay> dataList = new ArrayList<>();
    private List<ShortPlay> bannerDataList = new ArrayList<>();
    private List<ShortPlay> hotDataList = new ArrayList<>();
    private List<ShortPlay> mostDataList = new ArrayList<>();
    private List<ShortPlay> cartoonDataList = new ArrayList<>();
    private BannerManager bannerManager;

    public void setData(List<ShortPlay> list) {
        this.dataList.clear();
        this.dataList.add(new ShortPlay(0, "", 0, 0, "", "", ""));
        this.dataList.addAll(list);
        notifyDataSetChanged();
    }

    public void addData(List<ShortPlay> list) {
        int startPos = this.dataList.size();
        this.dataList.addAll(list);
        notifyItemRangeInserted(startPos, list.size());
    }

    public void setHeaderBannerData(List<ShortPlay> newBanners) {
        if (newBanners != null) {
            this.bannerDataList.clear();
            bannerDataList.addAll(newBanners);
            notifyItemChanged(0);
        }
    }

    public void setHeaderHotData(List<ShortPlay> newHots) {
        if (newHots != null) {
            this.hotDataList.clear();
            hotDataList.addAll(newHots);
            notifyItemChanged(0);
        }
    }

    public void setHeaderMostData(List<ShortPlay> shortPlayList) {
        if (shortPlayList != null) {
            this.mostDataList.clear();
            mostDataList.addAll(shortPlayList);
            notifyItemChanged(0);
        }
    }

    public void setHeaderCartoonData(List<ShortPlay> shortPlayList) {
        if (shortPlayList != null) {
            this.cartoonDataList.clear();
            cartoonDataList.addAll(shortPlayList);
            notifyItemChanged(0);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_LIST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header_home, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_layout, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.flSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.itemView.getContext().startActivity(new Intent(holder.itemView.getContext(), SearchActivity.class));
                }
            });
            headerViewHolder.tvHotMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MoreActivity.start(holder.itemView.getContext(), 1, holder.itemView.getContext().getString(R.string.s_hot_short));
                }
            });
            headerViewHolder.tvMostMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MoreActivity.start(holder.itemView.getContext(), 2, holder.itemView.getContext().getString(R.string.s_most));
                }
            });
            headerViewHolder.tvCartoonMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MoreActivity.start(holder.itemView.getContext(), 3, holder.itemView.getContext().getString(R.string.s_cartoon));
                }
            });
            if (bannerDataList != null) {
                headerViewHolder.bannerManager.updateData(bannerDataList);
            }
            if (hotDataList != null) {
                for (int i = 0; i < hotDataList.size(); i++) {
                    ShortPlay shortPlay = hotDataList.get(i);
                    View childAt = headerViewHolder.llHot.getChildAt(i);
                    if (childAt != null) {
                        childAt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DramaPlayActivity.start((AppCompatActivity) holder.itemView.getContext(), shortPlay);
                            }
                        });
                        RoundImageView imageView = childAt.findViewById(R.id.ic_cover);
                        imageView.setRadius(10);
                        Glide.with(holder.itemView.getContext())
                                .load(shortPlay.coverImage)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(imageView);
                        TextView tvHotValue = childAt.findViewById(R.id.tv_hot_value);
                        tvHotValue.setText(ShortUtils.convertToK(shortPlay.totalCollectCount));
                        TextView tvEpisode = childAt.findViewById(R.id.tv_episode);
                        tvEpisode.setText(shortPlay.total + holder.itemView.getContext().getString(R.string.s_eps));
                        TextView tvName = childAt.findViewById(R.id.tv_name);
                        tvName.setText(shortPlay.title);
                    }
                }
            }
            if (mostDataList != null) {
                for (int i = 0; i < mostDataList.size(); i++) {
                    ShortPlay shortPlay = mostDataList.get(i);
                    View childAt = headerViewHolder.llMost.getChildAt(i);
                    if (childAt != null) {
                        childAt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DramaPlayActivity.start((AppCompatActivity) holder.itemView.getContext(), shortPlay);
                            }
                        });
                        RoundImageView imageView = childAt.findViewById(R.id.ic_cover);
                        imageView.setRadius(10);
                        Glide.with(holder.itemView.getContext())
                                .load(shortPlay.coverImage)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(imageView);
                        TextView tvHotValue = childAt.findViewById(R.id.tv_hot_value);
                        tvHotValue.setText(ShortUtils.convertToK(shortPlay.totalCollectCount));
                        TextView tvEpisode = childAt.findViewById(R.id.tv_episode);
                        tvEpisode.setText(shortPlay.total + holder.itemView.getContext().getString(R.string.s_eps));
                        TextView tvName = childAt.findViewById(R.id.tv_name);
                        tvName.setText(shortPlay.title);
                    }
                }
            }
            if (cartoonDataList != null) {
                for (int i = 0; i < cartoonDataList.size(); i++) {
                    ShortPlay shortPlay = cartoonDataList.get(i);
                    View childAt = headerViewHolder.llCartoon.getChildAt(i);
                    if (childAt != null) {
                        childAt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DramaPlayActivity.start((AppCompatActivity) holder.itemView.getContext(), shortPlay);
                            }
                        });
                        RoundImageView imageView = childAt.findViewById(R.id.ic_cover);
                        imageView.setRadius(10);
                        Glide.with(holder.itemView.getContext())
                                .load(shortPlay.coverImage)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(imageView);
                        TextView tvHotValue = childAt.findViewById(R.id.tv_hot_value);
                        tvHotValue.setText(ShortUtils.convertToK(shortPlay.totalCollectCount));
                        TextView tvEpisode = childAt.findViewById(R.id.tv_episode);
                        tvEpisode.setText(shortPlay.total + holder.itemView.getContext().getString(R.string.s_eps));
                        TextView tvName = childAt.findViewById(R.id.tv_name);
                        tvName.setText(shortPlay.title);
                    }
                }
            }
        } else {
            ViewHolder viewHolder = ((ViewHolder) holder);
            ShortPlay item = dataList.get(position);
            viewHolder.tvName.setText(item.title); // 假设字段名为 playName
            viewHolder.tvEpisode.setText(item.total + holder.itemView.getContext().getResources().getString(R.string.s_eps));

            // 使用你之前的转换方法显示热度
            viewHolder.tvHotValue.setText(ShortUtils.convertToK(item.totalCollectCount));

            // 使用 Glide 或其他框架加载封面
            Glide.with(viewHolder.icCover.getContext())
                    .load(item.coverImage)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(viewHolder.icCover);

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DramaPlayActivity.start((AppCompatActivity) v.getContext(), item);
                }
            });
        }
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

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        ViewPager2 bannerPager;
        LinearLayout indicatorLayout;
        LinearLayout llHot;
        LinearLayout llMost;
        LinearLayout llCartoon;
        TextView tvHotMore;
        TextView tvMostMore;
        TextView tvCartoonMore;
        TextView tvBannerTitle;
        FrameLayout flSearch;
        BannerManager bannerManager;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerPager = itemView.findViewById(R.id.bannerPager);
            indicatorLayout = itemView.findViewById(R.id.indicatorLayout);
            tvBannerTitle = itemView.findViewById(R.id.tv_banner_title);
            List<ShortPlay> bannerDatas = new ArrayList<>();
            bannerManager = new BannerManager(bannerPager, indicatorLayout, tvBannerTitle, bannerDatas);

            flSearch = itemView.findViewById(R.id.fl_search);
            llHot = itemView.findViewById(R.id.ll_hot);
            tvHotMore = itemView.findViewById(R.id.tv_hot_more);

            llMost = itemView.findViewById(R.id.ll_most);
            tvMostMore = itemView.findViewById(R.id.tv_most_more);

            llCartoon = itemView.findViewById(R.id.ll_cartoon);
            tvCartoonMore = itemView.findViewById(R.id.tv_cartoon_more);
        }
    }
}

