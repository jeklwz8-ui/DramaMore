package com.dramamore.shorts.yanqin.adapter;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class MoreGridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount; // 列数
    private int spacing;   // 间隔大小（px）
    private boolean includeEdge; // 是否包含边缘间距

    public MoreGridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position
        int column = position % spanCount; // item column

        if (includeEdge) {
            // 边缘也留白
            outRect.left = spacing - column * spacing / spanCount;
            outRect.right = (column + 1) * spacing / spanCount;

            if (position < spanCount) { // 第一行顶部
                outRect.top = spacing;
            }
            outRect.bottom = spacing; // 底部间距
        } else {
            // 边缘不留白，仅中间留白
            outRect.left = column * spacing / spanCount;
            outRect.right = spacing - (column + 1) * spacing / spanCount;
            if (position >= spanCount) {
                outRect.top = spacing; // 只有从第二行开始才有顶部间距
            }
        }
    }
}

