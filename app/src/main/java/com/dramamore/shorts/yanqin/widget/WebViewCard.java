package com.dramamore.shorts.yanqin.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dramamore.shorts.yanqin.utils.AppUtils;
import com.dramamore.shorts.yanqin.utils.Logs;

import java.io.File;

public class WebViewCard extends WebView {
    private String TAG = "WebViewCard";
    public static final String DEFAULT_HTML = "file:///android_asset/protocol.html";

    public WebViewCard(@NonNull Context context) {
        super(context);
        init();
    }

    public WebViewCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WebViewCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initWebViewSettings();
        setFocusable(true);

        loadUrl(DEFAULT_HTML);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebViewSettings() {

        WebSettings settings = getSettings();
        // --- 核心设置 ---
        settings.setJavaScriptEnabled(true);        // 启用 JS (大多数网页需要)
        settings.setDomStorageEnabled(true);        // 启用 DOM 存储 (处理一些缓存和本地存储)
        settings.setDatabaseEnabled(true);          // 启用数据库

        // --- 适配设置 (让网页能自动适配卡片大小) ---
        settings.setUseWideViewPort(true);          // 将图片调整到适合 webview 的大小
        settings.setLoadWithOverviewMode(true);     // 自适应屏幕

        // --- 缓存与加载 ---
        settings.setCacheMode(WebSettings.LOAD_DEFAULT); // 默认缓存模式
        settings.setAllowFileAccess(true);               // 允许访问文件 (如果加载 assets 网页)
        settings.setAllowContentAccess(true);

        // --- Android 9 (API 28) 特有：允许混合内容 ---
        // 如果你加载的页面是 HTTPS，但图片是 HTTP，不设置这个会导致图片不显示
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // 禁用缩放按钮（桌面卡片通常不需要缩放，会影响体验）
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);

        // 允许通过 file url 加载的 Javascript 读取其他的本地文件 (视需求而定)
        // 允许加载本地文件资源
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        // 提高性能：硬件加速
        // 注意：如果卡片滑动时白屏或闪烁，尝试关闭硬件加速
        this.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // 禁用滚动条（让卡片看起来更像原生 UI）
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Logs.i(TAG, "dispatchKeyEvent-keyCode=" + event.getKeyCode());
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (canGoBack()) {
                goBack();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /*@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Logs.i(TAG, "dispatchTouchEvent");
        return gestureDetector.onTouchEvent(ev);
    }*/

    GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            Logs.i(TAG, "onDown");
            return true;
        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            Logs.i(TAG, "onSingleTapUp");
//            AppUtils.openUrlInExternalBrowser(getContext(), mUrl);
            return true;
        }

    });
}
