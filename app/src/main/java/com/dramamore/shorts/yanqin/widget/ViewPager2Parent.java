package com.dramamore.shorts.yanqin.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ViewPager2Parent extends FrameLayout {
    public ViewPager2Parent(@NonNull Context context) {
        super(context);
    }

    public ViewPager2Parent(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewPager2Parent(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    private OnWindowVisibilityChangeListener listener;

    public interface OnWindowVisibilityChangeListener {
        void onVisibilityChanged(int visibility);
    }

    public void setOnWindowVisibilityChangeListener(OnWindowVisibilityChangeListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (listener != null) {
            listener.onVisibilityChanged(visibility);
        }
    }
}
