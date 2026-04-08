package com.dramamore.shorts.yanqin.banner;

import android.os.Handler;
import android.os.Looper;
import android.os.Build;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bytedance.sdk.shortplay.api.ShortPlay;

import java.util.ArrayList;
import java.util.List;

import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.utils.Logs;
import com.dramamore.shorts.yanqin.widget.ViewPager2Parent;

public class BannerManager {

    private static final String TAG = "BannerManager";
    private ViewPager2 viewPager;
    private LinearLayout indicatorLayout;
    private TextView titleView;
    private ImageView backgroundView;
    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable runnable;
    private List<ShortPlay> images = new ArrayList<>();
    BannerAdapter adapter;
    @Nullable
    private CustomTarget<Bitmap> backgroundBitmapTarget;
    private boolean isAttached = false; // 记录状态
    public BannerManager(ViewPager2 pager, LinearLayout indicator, List<ShortPlay> list) {
        this(pager, indicator, null, null, list);
    }

    public BannerManager(ViewPager2 pager, LinearLayout indicator, TextView title, List<ShortPlay> list) {
        this(pager, indicator, title, null, list);
    }

    public BannerManager(ViewPager2 pager, LinearLayout indicator, TextView title, ImageView background, List<ShortPlay> list) {
        viewPager = pager;
        indicatorLayout = indicator;
        titleView = title;
        backgroundView = background;
        images.clear();
        images.addAll(list);
        applyBlurEffectIfNeeded();

        adapter = new BannerAdapter(pager.getContext(), images);
        viewPager.setAdapter(adapter);
        viewPager.setBackgroundColor(Color.TRANSPARENT);
        View recyclerChild = viewPager.getChildAt(0);
        if (recyclerChild instanceof RecyclerView) {
            recyclerChild.setBackgroundColor(Color.TRANSPARENT);
        }
        // Prevent ViewPager2 page changes from stealing focus and causing parent RecyclerView to scroll.
        viewPager.setFocusable(false);
        viewPager.setFocusableInTouchMode(false);
        viewPager.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        int start = Integer.MAX_VALUE / 2;
        if (!images.isEmpty()) {
            start = start - start % images.size();
        }

        viewPager.setCurrentItem(start);

        createIndicators();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {

                if (!images.isEmpty()) {
                    int real = position % images.size();
                    updateIndicators(real);
                    updateTitle(real);
                    updateBackground(real);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    stop(); // 手指触摸时停止自动轮播
                }

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    start(); // 滑动结束恢复轮播
                }
            }
        });

        ((ViewPager2Parent) viewPager.getParent()).setOnWindowVisibilityChangeListener(new ViewPager2Parent.OnWindowVisibilityChangeListener() {
            @Override
            public void onVisibilityChanged(int visibility) {
                Logs.i(TAG, "onVisibilityChanged-visibility=" + visibility);
                if(visibility==View.VISIBLE){
                    start();
                }else{
                    stop();
                }
            }
        });

        setPageTransformer();
        startAutoScroll();
    }

    public void updateData(List<ShortPlay> newList) {
        if (adapter != null && !newList.isEmpty()) {
            images.clear();
            images.addAll(newList);
            adapter.notifyDataSetChanged();

            if (indicatorLayout != null) {
                indicatorLayout.removeAllViews();
                createIndicators();
            }
            updateTitle(0);
            updateBackground(0);
            start();
        }
    }

    private void setPageTransformer() {

        viewPager.setPageTransformer((page, position) -> {

            /*float scale = 0.85f + (1 - Math.abs(position)) * 0.15f;
            page.setScaleY(scale);
            page.setAlpha(scale);*/
        });
    }

    private void createIndicators() {
        int dotSize = dpToPx(6);
        int dotHorizontalMargin = dpToPx(3);

        for (int i = 0; i < images.size(); i++) {

            ImageView dot = new ImageView(viewPager.getContext());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
            params.setMargins(dotHorizontalMargin, 0, dotHorizontalMargin, 0);
            dot.setLayoutParams(params);
            dot.setImageResource(R.drawable.indicator_inactive);

            indicatorLayout.addView(dot);
        }

        updateIndicators(0);
        updateTitle(0);
        updateBackground(0);
    }

    private int dpToPx(int dp) {
        float density = viewPager.getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void updateIndicators(int position) {

        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            ImageView dot = (ImageView) indicatorLayout.getChildAt(i);
            if (i == position) {
                dot.setImageResource(R.drawable.indicator_active);
            } else {
                dot.setImageResource(R.drawable.indicator_inactive);
            }
        }
    }

    private void updateTitle(int position) {
        if (titleView == null) {
            return;
        }
        if (images.isEmpty() || position < 0 || position >= images.size()) {
            titleView.setText("");
            return;
        }
        ShortPlay shortPlay = images.get(position);
        titleView.setText(shortPlay != null ? shortPlay.title : "");
    }

    private void updateBackground(int position) {
        if (backgroundView == null) {
            return;
        }
        if (images.isEmpty() || position < 0 || position >= images.size()) {
            clearBackgroundTarget();
            backgroundView.setImageDrawable(null);
            return;
        }
        ShortPlay shortPlay = images.get(position);
        if (shortPlay == null) {
            clearBackgroundTarget();
            backgroundView.setImageDrawable(null);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            clearBackgroundTarget();
            Glide.with(backgroundView)
                    .load(shortPlay.coverImage)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(backgroundView);
        } else {
            clearBackgroundTarget();
            backgroundBitmapTarget = new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    backgroundView.setImageBitmap(createBlurLikeBitmap(resource));
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    backgroundView.setImageDrawable(placeholder);
                }
            };
            Glide.with(backgroundView)
                    .asBitmap()
                    .load(shortPlay.coverImage)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(backgroundBitmapTarget);
        }
    }

    private void clearBackgroundTarget() {
        if (backgroundView != null && backgroundBitmapTarget != null) {
            Glide.with(backgroundView).clear(backgroundBitmapTarget);
        }
        backgroundBitmapTarget = null;
    }

    private Bitmap createBlurLikeBitmap(@NonNull Bitmap source) {
        // 增加模糊程度：减小缩放因子以进一步降低分辨率，从而增加模糊效果
        int scaledWidth = Math.max(1, Math.round(source.getWidth() * 0.05f));
        int scaledHeight = Math.max(1, Math.round(source.getHeight() * 0.05f));
        Bitmap lowRes = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true);
        return Bitmap.createScaledBitmap(lowRes, source.getWidth(), source.getHeight(), true);
    }

    private void applyBlurEffectIfNeeded() {
        if (backgroundView == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 增加 RenderEffect 的模糊半径
            backgroundView.setRenderEffect(RenderEffect.createBlurEffect(100f, 100f, Shader.TileMode.CLAMP));
        }
    }

    private void startAutoScroll() {

        runnable = new Runnable() {
            @Override
            public void run() {
                Logs.i(TAG, "startAutoScroll-WindowVisibility=" + viewPager.getWindowVisibility());
                if (isPagerActuallyVisible() && images.size() > 1) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, false);
                }
                handler.postDelayed(this, 3000);
            }
        };

        handler.postDelayed(runnable, 3000);
    }

    public void stop() {
        handler.removeCallbacks(runnable);
    }

    public void start() {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(runnable, 3000);
    }

    private boolean isPagerActuallyVisible() {
        if (viewPager == null || !viewPager.isShown() || viewPager.getWindowVisibility() != View.VISIBLE) {
            return false;
        }
        Rect rect = new Rect();
        if (!viewPager.getGlobalVisibleRect(rect) || rect.width() <= 0 || rect.height() <= 0) {
            return false;
        }
        int pagerHeight = viewPager.getHeight();
        if (pagerHeight <= 0) {
            return false;
        }
        float visibleHeightRatio = rect.height() * 1f / pagerHeight;
        // Keep autoplay active when banner is meaningfully visible.
        return visibleHeightRatio >= 0.3f;
    }
}
