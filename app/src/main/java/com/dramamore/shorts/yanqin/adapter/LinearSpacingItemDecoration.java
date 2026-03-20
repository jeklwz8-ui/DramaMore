package com.dramamore.shorts.yanqin.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LinearSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;

    public LinearSpacingItemDecoration(int spacingDp, Context context) {
        // 將 dp 轉為 px
        this.spacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, spacingDp, context.getResources().getDisplayMetrics());
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int itemCount = state.getItemCount();

        // 這裡以垂直列表為例：
        outRect.bottom = spacing; // 每一項底部都加間距

        // 如果你希望第一項頂部也有間距：
        if (position == 0) {
            outRect.top = spacing;
        }
    }
}
