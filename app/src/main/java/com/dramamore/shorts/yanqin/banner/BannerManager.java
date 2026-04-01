package com.dramamore.shorts.yanqin.banner;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

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
    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable runnable;
    private List<ShortPlay> images = new ArrayList<>();
    BannerAdapter adapter;
    private boolean isAttached = false; // 记录状态
    public BannerManager(ViewPager2 pager, LinearLayout indicator, List<ShortPlay> list) {
        viewPager = pager;
        indicatorLayout = indicator;
        images.clear();
        images.addAll(list);

        adapter = new BannerAdapter(pager.getContext(), images);
        viewPager.setAdapter(adapter);

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

        for (int i = 0; i < images.size(); i++) {

            ImageView dot = new ImageView(viewPager.getContext());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(18, 18);
            params.setMargins(10, 0, 10, 0);
            dot.setLayoutParams(params);
            dot.setImageResource(R.drawable.indicator_inactive);

            indicatorLayout.addView(dot);
        }

        updateIndicators(0);
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

    private void startAutoScroll() {

        runnable = new Runnable() {
            @Override
            public void run() {
                Logs.i(TAG, "startAutoScroll-WindowVisibility=" + viewPager.getWindowVisibility());
                if (viewPager.getWindowVisibility() == View.VISIBLE) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
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
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 3000);
    }
}
