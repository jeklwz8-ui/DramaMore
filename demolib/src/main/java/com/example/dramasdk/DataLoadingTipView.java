package com.example.dramasdk;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DataLoadingTipView extends FrameLayout {

    private ProgressBar progressBar;
    private TextView retryView;
    private RetryClickListener retryClickListener;
    private boolean dataLoading;

    public DataLoadingTipView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DataLoadingTipView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        progressBar = new ProgressBar(getContext());
        LayoutParams barLP = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        barLP.gravity = Gravity.CENTER;
        addView(progressBar, barLP);

        retryView = new TextView(getContext());
        retryView.setText("Click to retry");
        retryView.setGravity(Gravity.CENTER);
        retryView.setTextColor(Color.BLACK);
        retryView.setTextSize(36);
        addView(retryView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        retryView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (retryClickListener != null) {
                    retryClickListener.onClickRetry();
                }
            }
        });
        retryView.setVisibility(GONE);
    }

    public void onStartLoading() {
        progressBar.setVisibility(VISIBLE);
        retryView.setVisibility(GONE);
        dataLoading = true;
    }

    public boolean isDataLoading() {
        return dataLoading;
    }

    public void onLoadingFinish(boolean loadSuccess) {
        dataLoading = false;
        progressBar.setVisibility(GONE);
        retryView.setVisibility(loadSuccess ? GONE : VISIBLE);
    }

    public void setRetryClickListener(RetryClickListener retryClickListener) {
        this.retryClickListener = retryClickListener;
    }

    public interface RetryClickListener {
        void onClickRetry();
    }
}
