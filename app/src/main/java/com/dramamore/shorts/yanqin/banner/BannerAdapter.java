package com.dramamore.shorts.yanqin.banner;

import android.content.Context;
import android.graphics.Outline;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bytedance.sdk.shortplay.api.ShortPlay;

import java.util.List;

import com.dramamore.shorts.yanqin.activity.DramaPlayActivity;
import com.dramamore.shorts.yanqin.utils.Logs;
import com.dramamore.shorts.yanqin.utils.ShortUtils;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {

    private static final String TAG = "BannerAdapter";
    private List<ShortPlay> images;
    private Context context;

    public BannerAdapter(Context context, List<ShortPlay> images) {
        this.context = context;
        this.images = images;
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Logs.i(TAG,"onBindViewHolder-pos="+position+",size="+images.size());
        if(images.isEmpty())return;
        int realPosition = position % images.size();
        Glide.with(context)
                .load(images.get(realPosition).coverImage)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DramaPlayActivity.start(((AppCompatActivity) holder.itemView.getContext()), images.get(realPosition));
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ImageView imageView = new ImageView(context);
        imageView.post(() -> {
            imageView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    int radius = 20;
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
        });
        imageView.setClipToOutline(true);

        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        return new ViewHolder(imageView);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view;
        }
    }
}
