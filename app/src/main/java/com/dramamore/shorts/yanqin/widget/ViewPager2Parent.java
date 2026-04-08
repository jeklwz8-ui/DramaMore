package com.dramamore.shorts.yanqin.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ViewPager2Parent extends FrameLayout {
    private final Path roundClipPath = new Path();
    private final RectF roundRect = new RectF();
    private final float cornerRadiusPx;

    public ViewPager2Parent(@NonNull Context context) {
        super(context);
        cornerRadiusPx = dpToPx(context, 12f);
        initRoundClip();
    }

    public ViewPager2Parent(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        cornerRadiusPx = dpToPx(context, 12f);
        initRoundClip();
    }

    public ViewPager2Parent(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        cornerRadiusPx = dpToPx(context, 12f);
        initRoundClip();
    }
    private OnWindowVisibilityChangeListener listener;

    public interface OnWindowVisibilityChangeListener {
        void onVisibilityChanged(int visibility);
    }

    public void setOnWindowVisibilityChangeListener(OnWindowVisibilityChangeListener listener) {
        this.listener = listener;
    }

    private void initRoundClip() {
        setWillNotDraw(false);
        setClipToPadding(false);
        setClipChildren(true);
    }

    private static float dpToPx(@NonNull Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    private void updateRoundPath(int width, int height) {
        roundRect.set(0f, 0f, width, height);
        roundClipPath.reset();
        roundClipPath.addRoundRect(roundRect, cornerRadiusPx, cornerRadiusPx, Path.Direction.CW);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateRoundPath(w, h);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (roundRect.isEmpty()) {
            super.dispatchDraw(canvas);
            return;
        }
        int saveCount = canvas.save();
        canvas.clipPath(roundClipPath);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (listener != null) {
            listener.onVisibilityChanged(visibility);
        }
    }
}
