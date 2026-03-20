package com.dramamore.shorts.yanqin.adapter;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount; // 列数
    private int spacing;   // 间隔大小（px）
    private boolean includeEdge; // 是否包含边缘间距

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) return;

        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter != null && adapter.getItemViewType(position) == 0) { // TYPE_HEADER
            outRect.set(0, 0, 0, 0);
            return;
        }

        GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        int spanIndex = params.getSpanIndex(); // 當前在哪一列 (0, 1, 2)
        int spanCount = ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();

        if (includeEdge) {
            outRect.left = spacing - spanIndex * spacing / spanCount;
            outRect.right = (spanIndex + 1) * spacing / spanCount;
            outRect.bottom = spacing;
            if (position <= spanCount) outRect.top = spacing; // 第一行數據頂部
        } else {
            outRect.left = spanIndex * spacing / spanCount;
            outRect.right = spacing - (spanIndex + 1) * spacing / spanCount;
            if (position > spanCount) outRect.top = spacing; // 非第一行數據頂部
        }
    }
}

