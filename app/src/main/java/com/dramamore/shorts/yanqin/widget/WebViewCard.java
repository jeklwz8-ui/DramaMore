package com.dramamore.shorts.yanqin.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dramamore.shorts.yanqin.utils.Logs;

import java.io.File;

public class WebViewCard extends WebView {
    private String TAG = "WebViewCard";
    public static final String HOME_PAGE_KEY = "home_page_key";
    public static final String OSS_ENABLE_KEY = "oss_enable_key";
    public static final String UPDATE_TIMELAG_KEY = "update_timelag_key";
    public static final String DEFAULT_HTML = "file:///android_asset/default.html";
    private String homePage = "https://nos6.nyanwn.com";
    private boolean mIsLoaded, mDidError;

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

    private String mCachedPagePath;

    private void init() {
        initWebViewSettings();
        setFocusable(true);

        loadUrl(homePage);
    }

    private boolean mIsIntercept;

    /**
     * 设置触摸事件是否拦截
     *
     * @param isIntercept
     */
    public void requestIntercept(boolean isIntercept) {
        setEnabled(!isIntercept);
        mIsIntercept = isIntercept;
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

        setWebChromeClient(new WebChromeClient());
        setWebViewClient(new WebViewClient() {

            // 兼容 API 24 以上
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Logs.i(TAG, "shouldOverrideUrlLoading-url=" + url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // 1. 过滤掉无意义的初始空白页
                if (url == null || url.equals("about:blank")) {
                    return;
                }
                Logs.i(TAG, "onPageFinished-url=" + url + ",mIsLoaded=" + mIsLoaded + ",mDidError=" + mDidError);
                if (!mDidError) {
                    mIsLoaded = true;
                    if (url.equals(homePage)) {
                        view.saveWebArchive(mCachedPagePath);
                    } else if (url.equals(DEFAULT_HTML)) {
                        mIsLoaded = false;
                    }
                } else {
                    // 重置错误标记，方便下次尝试
                    mDidError = false;
                    mIsLoaded = false;
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Logs.i(TAG, "onReceivedError-url=" + view.getUrl() + ",request.url=" + request.getUrl() + " ,ErrorCode=" + error.getErrorCode() + " ,Descript=" + error.getDescription());
                int errorCode = error.getErrorCode();
                if(errorCode ==-2){
                    Logs.i(TAG, "onReceivedError-loadDefaultHtml-request.url=" + request.getUrl());
                    loadDefaultHtml();
                    mDidError = true;
                }

            }
        });
    }

    private void loadDefaultHtml() {
        loadUrl(DEFAULT_HTML);
    }

    // Android 6.0 及以上版本调用
    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        return super.startActionMode(new MyActionModeCallback(), type);
    }

    private class MyActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // 返回 false 直接不创建菜单，或者在此处 menu.clear()
            Logs.i(TAG, "onCreateActionMode-mIsIntercept=" + mIsIntercept);
            return !mIsIntercept;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    }

    @Override
    public void reload() {
        loadUrl(homePage);//都是加载homepage
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Logs.i(TAG, "onScrollChanged-l=" + l + ",t=" + t + ",oldl=" + oldl + ",oldt=" + oldt);
        if (l != 0) {
            scrollTo(0, t); // 强制横向位置永远为 0
        }
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

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.VISIBLE) {
            if (!mIsLoaded && !TextUtils.isEmpty(homePage)) {
                loadUrl(homePage);
                Logs.i(TAG, "onWindowVisibilityChanged-visibility=" + visibility + ",mIsLoaded=" + mIsLoaded + ",homePage=" + homePage);
            }
            // 恢复实例的 JS 执行
            onResume();
        } else {
            // 暂停实例的渲染和 JS
            onPause();
        }
    }
}
