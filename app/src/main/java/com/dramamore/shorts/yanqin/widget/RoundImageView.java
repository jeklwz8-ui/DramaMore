package com.dramamore.shorts.yanqin.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class RoundImageView extends ImageView {
    public RoundImageView(Context context) {
        this(context, null);
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRadius(int radius) {
        post(new Runnable() {
            @Override
            public void run() {
                setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, android.graphics.Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius, getContext().getResources().getDisplayMetrics()));
                    }
                });
                setClipToOutline(true);
            }
        });
    }
}
