package com.example.dramasdk;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RatioFrameLayout extends FrameLayout {

    private float ratio;
    private Path mPath = new Path();
    private float radius;

    public RatioFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RatioFrameLayout);
        if (typedArray != null) {
            ratio = typedArray.getFloat(R.styleable.RatioFrameLayout_ratio, 0);
        }

        radius = DemoUtils.dp2Px(context, 8);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (ratio > 0 && MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
            int newHeight = (int) (measuredWidth / ratio);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mPath.reset();
        mPath.addRoundRect(new RectF(0, 0, w, h), radius, radius, Path.Direction.CW);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(mPath);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }
}
