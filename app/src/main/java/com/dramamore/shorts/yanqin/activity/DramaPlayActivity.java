package com.dramamore.shorts.yanqin.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.compose.ui.unit.Dp;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAd;
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAdLoadListener;
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerRequest;
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerSize;
import com.bytedance.sdk.openadsdk.api.model.PAGErrorModel;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGImageItem;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGMediaView;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAd;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAdData;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAdLoadListener;
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeRequest;
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardItem;
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAd;
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAdInteractionListener;
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAdLoadCallback;
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedRequest;
import com.bytedance.sdk.shortplay.api.EpisodeData;
import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.bytedance.sdk.shortplay.api.ShortPlayFragment;
import com.dramamore.shorts.yanqin.App;
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.dao.HistoryDao;
import com.dramamore.shorts.yanqin.database.HistoryDatabase;
import com.dramamore.shorts.yanqin.entity.HistoryDaoEntity;
import com.dramamore.shorts.yanqin.listener.IIndexChooseListener;
import com.dramamore.shorts.yanqin.utils.DpUtils;
import com.dramamore.shorts.yanqin.utils.Logs;
import com.dramamore.shorts.yanqin.utils.PlayHistoryHelper;
import com.dramamore.shorts.yanqin.utils.ShortUtils;
import com.google.gson.Gson;
import com.ss.ttvideoengine.Resolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DramaPlayActivity extends AppFragmentActivity implements IIndexChooseListener {
    private static final String TAG = "DramaPlayActivity";
    private static final String EXTRA_SHORT_PLAY = "short_play";
    private static final String EXTRA_SHORT_PLAY_INDEX = "short_play_index";
    private static final String EXTRA_SHORT_PLAY_FROM_SECONDS = "seconds";
    private static final int REQUEST_CODE_CHOOSE_RESOLUTION = 1;
    private static final int REQUEST_CODE_CHOOSE_INDEX = 2;
    private final List<PAGNativeAd> feedAds = new ArrayList<>();
    /**
     * 已解锁的剧集
     */
    private final SparseIntArray unlockedIndexes = new SparseIntArray();
    private final PlayHistoryHelper.PlayHistory playHistory = new PlayHistoryHelper.PlayHistory();
    private ShortPlayFragment detailFragment;
    private PSSDK.VideoPlayInfo currentVideoPlayInfo;
    private View bannerView;
    private ShortPlay shortPlay;
    //private int startFromIndex;
    //private int startFromSeconds;
    private PAGRewardedAd rewardedAd;
    private View bottomDefaultView;
    private Resolution[] resolutions;
    private Resolution currentResolution;
    private boolean hasShowRetainDialog;
    private boolean hasShowUnlockMoreDialog;
    private Runnable taskWhenResume;

    public static void start(Context context, ShortPlay shortPlay) {
        start(context, shortPlay, 1, 0);
    }

    private static void start(Context context, ShortPlay shortPlay, int index) {
        start(context, shortPlay, index, 0);
    }

    private static void start(Context context, ShortPlay shortPlay, int index, int fromSeconds) {
        Intent intent = new Intent(context, DramaPlayActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EXTRA_SHORT_PLAY, shortPlay);
        intent.putExtra(EXTRA_SHORT_PLAY_INDEX, index);
        intent.putExtra(EXTRA_SHORT_PLAY_FROM_SECONDS, fromSeconds);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 建议在 onCreate 顶部调用
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        // 设置状态栏颜色为透明
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        EdgeToEdge.enable(this);

        Intent intent = getIntent();
        Parcelable parcelableShortPlay = intent.getParcelableExtra(EXTRA_SHORT_PLAY);
        if (parcelableShortPlay == null) {
            finish();
            return;
        }

        shortPlay = (ShortPlay) parcelableShortPlay;

        // 测试gson序列化的兼容性
        Gson gson = new Gson();
        String json = gson.toJson(shortPlay);
        shortPlay = gson.fromJson(json, ShortPlay.class);
        //startFromIndex = intent.getIntExtra(EXTRA_SHORT_PLAY_INDEX, 1);
        //startFromSeconds = intent.getIntExtra(EXTRA_SHORT_PLAY_FROM_SECONDS, 0);
        if (shortPlay.episodes == null || shortPlay.episodes.isEmpty()) {
            toast("episodes is empty");
        }

        setContentView(R.layout.act_play);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.play), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 预加载信息流广告
        loadPangleFeedAd();
        loadRewardAd(null);

        HistoryDatabase.executor.execute(new Runnable() {
            @Override
            public void run() {
                HistoryDatabase dp = HistoryDatabase.getDatabase(DramaPlayActivity.this);
                HistoryDao historyDao = dp.historyDao();
                HistoryDaoEntity entity = historyDao.getEntityByShortId(shortPlay.id);
                int playIndex = entity == null ? 0 : entity.play_index;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showDetailFragment(shortPlay, playIndex, 0);
                    }
                });
            }
        });
    }

    @NonNull
    private View getBottomDefaultView() {
        if (bottomDefaultView == null) {
            bottomDefaultView = LayoutInflater.from(this).inflate(R.layout.player_bottom_default, null);
        }
        return bottomDefaultView;
    }

    private void showDetailFragment(ShortPlay shortPlay, int startFromIndex, int startFromSeconds) {
        // 默认前5集解锁
        for (int i = 1; i <= 5; i++) {
            unlockedIndexes.put(i, 1);
        }

        PSSDK.DetailPageConfig.Builder builder = new PSSDK.DetailPageConfig.Builder();
        builder.displayTextVisibility(PSSDK.DetailPageConfig.TEXT_POS_BOTTOM_DESC, false);
        builder.displayTextVisibility(PSSDK.DetailPageConfig.TEXT_POS_BOTTOM_TITLE, false);
        builder.displayProgressBar(false);
        builder.startPlayIndex(startFromIndex);
        builder.enableImmersiveMode(10000) // 【可选】播放页无操作xxxms后隐藏文字进入沉浸式模式，默认不启用此功能，启用时可指定时间
                .playSingleItem(false); // 【可选】只播放一集模式，用于在开发者用多个播放页Fragment对象构造滑动切剧场景时，默认false
        // 开启自动播放下一集
        builder.enableAutoPlayNext(true);
        builder.startPlayAtTimeSeconds(startFromSeconds);
        builder.hideLeftTopCloseAndTitle(false, new PSSDK.ShortPlayDetailPageCloseListener() {
            @Override
            public boolean onCloseClicked() {
                onBackPressed();
                return true;
            }
        });

        // 配置广告策略
        builder.adCustomProvider(new PSSDK.AdCustomProvider() {
            @Override
            public List<Integer> getDetailDrawAdPositions() {
                ArrayList<Integer> integers = new ArrayList<>();
                // 在第1集、第3集、第50集后面插入广告
                        /*integers.add(1);
                        integers.add(3);
                        integers.add(50);*/
                return integers;
            }

            @Override
            public PSSDK.DrawAdProvider getDrawAdProvider() {
                return new PSSDK.DrawAdProvider() {
                    @Override
                    public void onPrepareAd() {
                        // 快划到广告插入位置时调用，可以在这里请求广告
                        loadPangleFeedAd();
                    }

                    @Override
                    public View onObtainAdView(int position, int index) {
                        // 返回广告View，如没有可用广告则返回null
                        //return createFeedAdView();
                        return null;
                    }

                    @Override
                    public void onDestroy() {
                        // 播放页退出时调用，可在这里释放广告资源
                    }
                };
            }
        });
        detailFragment = PSSDK.createDetailFragment(shortPlay, builder.build(), new PSSDK.ShortPlayDetailPageListener() {
            ProgressChangeListener progressChangeListener;
            ResolutionChangeListener resolutionChangeListener;

            @Override
            public void onOverScroll(int direction) {
                String dir = direction == PSSDK.DIRECTION_UP ? "UP" : "DOWN";
                Logs.i(TAG, "onOverScroll() called with: direction = [" + direction + "], dir=" + dir);
            }

            @Override
            public void onProgressChange(ShortPlay shortPlay, int index, int currentPlayTimeInSeconds, int durationInSeconds) {
                Logs.i(TAG, "onProgressChange:index=" + index + ",进度：" + currentPlayTimeInSeconds + "/" + durationInSeconds);

                playHistory.index = index;
                playHistory.seconds = currentPlayTimeInSeconds;

                if (progressChangeListener != null) {
                    progressChangeListener.onProgressChanged(currentPlayTimeInSeconds, durationInSeconds);
                }
            }

            @Override
            public boolean onPlayFailed(PSSDK.ErrorInfo errorInfo) {
                // 视频播放失败
                Logs.i(TAG, "onPlayFailed() called with: errorInfo = [" + errorInfo + "]");
                if (errorInfo.code == PSSDK.ErrorInfo.ERROR_CODE_CURRENT_COUNTRY_NOT_SUPPORT) {
                    // 当前地区不支持播放，SDK会Toast提示，开发者也可以在此时显示弹窗等更友好的提示
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DramaPlayActivity.this);
                    dialogBuilder.setMessage("当前地区不支持播放");
                    dialogBuilder.create().show();
                    // return true表示替换掉SDK内的Toast提示
                    return true;
                }
                return false;
            }

            @Override
            public void onShortPlayPlayed(ShortPlay shortPlay, int index, EpisodeData episodeData) {
                // 每一集开始播放时回调，可用来记录播放历史
                Logs.i(TAG, "onShortPlayPlayed() called with: shortPlay = [" + shortPlay + "], index = [" + index + "]");

                if (shortPlay.isCollected) {//已收藏则更新哪一集
                    ShortUtils.followInsertOrDelete(DramaPlayActivity.this, true, shortPlay, index);
                }
                ShortUtils.historyInsert(DramaPlayActivity.this, shortPlay, index);
                playHistory.shortPlay = shortPlay;
                playHistory.index = index;
                PlayHistoryHelper.savePlayHistory(playHistory);
            }

            @Override
            public void onItemSelected(int position, ItemType type, int index) {
                Logs.i(TAG, "onItemSelected() called with: position = [" + position + "], type = [" + type + "], index = [" + index + "]");

                resolutions = null;
                currentResolution = null;

                if (type == ItemType.AD) {
                    // 列表里是广告时，底部就不同时显示banner了，影响体验，换成普通view
                    View view = getBottomDefaultView();
                    detailFragment.setBottomExtraViewContent(view, ShortPlayFragment.BottomViewType.OTHER);
                } else {
                    // 列表里是视频，可以显示底部banner
                    if (bannerView != null) {
                        detailFragment.setBottomExtraViewContent(bannerView, ShortPlayFragment.BottomViewType.AD);
                    } else {
                        loadPangleBannerAd();
                    }
                }
            }

            @Override
            public void onVideoPlayStateChanged(ShortPlay shortPlay, int index, int playbackState) {
                Logs.i(TAG, "onVideoPlayStateChanged() called with: shortPlay = [" + shortPlay + "], index = [" + index + "], playbackState = [" + playbackState + "]");
            }

            @Override
            public void onVideoPlayCompleted(ShortPlay shortPlay, int index) {
                Logs.i(TAG, "onVideoPlayCompleted: index=" + index + ",nextIndex=" + (index + 1));
            }

            @Override
            public void onEnterImmersiveMode() {
                // 进入沉浸式模式
                Logs.i(TAG, "onEnterImmersiveMode() called");
            }

            @Override
            public void onExitImmersiveMode() {
                // 退出沉浸式模式
                Logs.i(TAG, "onExitImmersiveMode() called");
            }

            @Override
            public boolean isNeedBlock(ShortPlay shortPlay, int index) {
                // 询问index集是否锁定，true锁定后则该集无法自动播放，需要通过showAdIfNeed里完成解锁
                // 默认对每一集均会询问，一旦返回false则此播放页不会再询问该集
                Logs.i(TAG, "isNeedBlock() called with: shortPlay = [" + shortPlay + "], index = [" + index + "]");
                //return unlockedIndexes.get(index, 0) != 1;
                return false;
            }

            @Override
            public void showAdIfNeed(ShortPlay shortPlay, int index, PSSDK.ShortPlayBlockResultListener listener) {
                // 当isNeedBlock指定index集锁定后，在用户切换到该集时，SDK不会播放视频，同时会调用此回调，可在此时展示激励广告或购买等交互，用户达成后调用listener.onShortPlayUnlocked告知SDK可播放该集
                Logs.i(TAG, "showAdIfNeed() called with: shortPlay = [" + shortPlay + "], index = [" + index + "], listener = [" + listener + "]");
                showUnLockDialog(shortPlay, index, listener);
            }

            @Override
            public void onVideoInfoFetched(ShortPlay shortPlay, int index, PSSDK.VideoPlayInfo videoPlayInfo) {
                currentVideoPlayInfo = videoPlayInfo;
                // 每一集视频准备好时调用此方法告知本集的视频信息
                resolutions = videoPlayInfo.supportResolutions;
                // 当前分辨率
                currentResolution = videoPlayInfo.currentResolution;

                if (resolutionChangeListener != null) {
                    resolutionChangeListener.onResolutionChanged(currentResolution.toString());
                }
                Logs.i(TAG, "onVideoInfoFetched: currentResolution=" + currentResolution);
            }

            @Override
            public List<View> onObtainPlayerControlViews() {
                ArrayList<View> views = new ArrayList<>();

                // 分享按钮
                CustomShareView shareView = new CustomShareView(getApplicationContext());
                views.add(shareView);
                shareView.setImageResource(R.drawable.share);
                FrameLayout.LayoutParams shareLP = new FrameLayout.LayoutParams(DpUtils.dp2px(getApplicationContext(), 32), DpUtils.dp2px(getApplicationContext(), 32));
                shareLP.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                shareLP.bottomMargin = DpUtils.dp2px(getApplicationContext(), 280);
                shareLP.rightMargin = DpUtils.dp2px(getApplicationContext(), 16);
                shareView.setLayoutParams(shareLP);
                shareView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT, shortPlay.title);
                        intent.putExtra(Intent.EXTRA_TEXT, shortPlay.desc);
                        startActivity(Intent.createChooser(intent, "分享短剧"));
                    }
                });

                // 点赞按钮
                CustomLikeView customLikeView = new CustomLikeView(getApplicationContext());
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                params.bottomMargin = DpUtils.dp2px(getApplicationContext(), 200);
                params.rightMargin = DpUtils.dp2px(getApplicationContext(), 16);
                customLikeView.setLayoutParams(params);
                views.add(customLikeView);

                // 收藏按钮
                CustomCollectView collectView = new CustomCollectView(getApplicationContext());
                params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                params.bottomMargin = DpUtils.dp2px(getApplicationContext(), 130);
                params.rightMargin = DpUtils.dp2px(getApplicationContext(), 16);
                collectView.setLayoutParams(params);
                views.add(collectView);

                // Loading
                CustomLoadingView loadingView = new CustomLoadingView(getApplicationContext());
                FrameLayout.LayoutParams loadingLP = new FrameLayout.LayoutParams(DpUtils.dp2px(getApplicationContext(), 48), DpUtils.dp2px(getApplicationContext(), 48));
                loadingLP.gravity = Gravity.CENTER;
                loadingView.setLayoutParams(loadingLP);
                views.add(loadingView);

                // 自定义失败界面
                CustomErrorView errorView = new CustomErrorView(getApplicationContext());
                errorView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        detailFragment.startPlay();
                    }
                });
                views.add(errorView);

                // 自定义进度条
                CustomProgressBar customProgressBar = new CustomProgressBar(getApplicationContext());
                FrameLayout.LayoutParams lpProgressBar = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                lpProgressBar.gravity = Gravity.BOTTOM;
                lpProgressBar.bottomMargin = DpUtils.dp2px(getApplicationContext(), 70);
                customProgressBar.setLayoutParams(lpProgressBar);
                views.add(customProgressBar);

                CustomOverlayView customOverlayView = new CustomOverlayView(getApplicationContext());
                params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                params.bottomMargin = DpUtils.dp2px(getApplicationContext(), 20);
                customOverlayView.setLayoutParams(params);
                progressChangeListener = customOverlayView;
                resolutionChangeListener = customOverlayView;
                views.add(customOverlayView);
                return views;
            }
        });
        if (detailFragment == null) {
            Toast.makeText(this, "创建播放页失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // 将播放页展示出来
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, detailFragment).show(detailFragment).commit();

        View view = getBottomDefaultView();
        detailFragment.setBottomExtraViewContent(view, ShortPlayFragment.BottomViewType.OTHER);
    }

    /*private View createFeedAdView() {
        if (feedAds.isEmpty()) {
            return null;
        }
        PAGNativeAd nativeAd = feedAds.remove(0);
        PAGNativeAdData nativeAdData = nativeAd.getNativeAdData();

        View adRootView = LayoutInflater.from(this).inflate(R.layout.feed_ad_view, null);
        ((RelativeLayout) adRootView.findViewById(R.id.tt_ad_logo)).addView(nativeAdData.getAdLogoView());
        ((TextView) adRootView.findViewById(R.id.tv_listitem_ad_title)).setText(nativeAdData.getTitle());
        ((TextView) adRootView.findViewById(R.id.tv_listitem_ad_desc)).setText(nativeAdData.getDescription());
        PAGImageItem icon = nativeAdData.getIcon();
        if (icon != null && icon.getImageUrl() != null) {
            Glide.with(this).load(icon.getImageUrl()).into(((ImageView) adRootView.findViewById(R.id.iv_listitem_icon)));
        }
        ((TextView) adRootView.findViewById(R.id.tt_creative_btn)).setText(TextUtils.isEmpty(nativeAdData.getButtonText()) ? "View" : nativeAdData.getButtonText());

        PAGMediaView mediaView = nativeAdData.getMediaView();
        if (mediaView != null) {
            ((FrameLayout) adRootView.findViewById(R.id.iv_listitem_video)).addView(mediaView);
        }

        ArrayList<View> clickViewList = new ArrayList<>();
        clickViewList.add(adRootView);
        ArrayList<View> clickCreativeList = new ArrayList<>();
        clickCreativeList.add(adRootView.findViewById(R.id.tt_creative_btn));
        nativeAd.registerViewForInteraction(((ViewGroup) adRootView), clickViewList, clickCreativeList, null, new PAGNativeAdInteractionListener() {
            @Override
            public void onAdShowed() {
                Logs.i(TAG, "onAdShowed() called");
            }

            @Override
            public void onAdClicked() {
                Logs.i(TAG, "onAdClicked() called");
            }

            @Override
            public void onAdDismissed() {
                Logs.i(TAG, "onAdDismissed() called");
            }
        });

        return adRootView;
    }*/

    private void loadRewardAd(PAGRewardedAdLoadCallback callback) {
        PAGRewardedRequest rewardedRequest = new PAGRewardedRequest();
        PAGRewardedAd.loadAd(App.REWARDAD_ID, rewardedRequest, new PAGRewardedAdLoadCallback() {
            @Override
            public void onError(@NonNull PAGErrorModel pagErrorModel) {
                if (callback != null) {
                    callback.onError(pagErrorModel);
                }
            }

            @Override
            public void onAdLoaded(PAGRewardedAd pagRewardedAd) {
                rewardedAd = pagRewardedAd;
                if (callback != null) {
                    callback.onAdLoaded(pagRewardedAd);
                }
            }
        });
    }

    private void loadPangleFeedAd() {
        PAGNativeRequest request = new PAGNativeRequest();
        PAGNativeAd.loadAd(App.NATIVEAD_ID, request, new PAGNativeAdLoadListener() {
            @Override
            public void onError(int code, String message) {
                Logs.i(TAG, "load pangle ad fail, code=" + code + ", message=" + message);
            }

            @Override
            public void onAdLoaded(PAGNativeAd pagNativeAd) {
                Logs.i(TAG, "load pangle ad success, " + pagNativeAd);
                feedAds.add(pagNativeAd);
            }
        });
    }

    private void loadPangleBannerAd() {

        PAGBannerRequest request = new PAGBannerRequest(PAGBannerSize.BANNER_W_320_H_50);
        PAGBannerAd.loadAd(App.BANNERAD_ID, request, new PAGBannerAdLoadListener() {
            @Override
            public void onError(int i, String s) {
                Logs.i(TAG, "onError() called with: i = [" + i + "], s = [" + s + "]");
            }

            @Override
            public void onAdLoaded(PAGBannerAd pagBannerAd) {
                Logs.i(TAG, "onAdLoaded() called with: pagBannerAd = [" + pagBannerAd + "]");
                bannerView = pagBannerAd.getBannerView();
                detailFragment.setBottomExtraViewContent(bannerView, ShortPlayFragment.BottomViewType.AD);
            }
        });
    }

    private void showUnLockDialog(ShortPlay shortPlay, int index, PSSDK.ShortPlayBlockResultListener listener) {
        UnlockDialog unlockDialog = new UnlockDialog();
        unlockDialog.show(getSupportFragmentManager(), "unlock");
        unlockDialog.setUnlockListener(new UnlockDialog.UnlockListener() {

            @Override
            public void onChooseCancel() {
            }

            @Override
            public void onChooseUnlock() {
                showUnlockAd(new PSSDK.ShortPlayBlockResultListener() {
                    @Override
                    public void onShortPlayUnlocked() {
                        if (listener != null) {
                            listener.onShortPlayUnlocked();
                        }
                        // 询问连续解锁
                        showUnlockMoreDialog();
                    }
                });
            }

        });
    }

    private void showUnlockMoreDialog() {
        if (hasShowUnlockMoreDialog || rewardedAd == null) {
            return;
        }
        hasShowUnlockMoreDialog = true;
        UnlockMoreDialog dialog = new UnlockMoreDialog();
        dialog.setListener(new IUnlockMoreListener() {
            @Override
            public void onChooseCancel() {

            }

            @Override
            public void onChooseUnlockMore() {
                showUnlockAd(new PSSDK.ShortPlayBlockResultListener() {
                    @Override
                    public void onShortPlayUnlocked() {
                        autoUnlock(2);
                    }
                });
            }
        });
        taskWhenResume = new Runnable() {
            @Override
            public void run() {
                dialog.show(getSupportFragmentManager(), "unlock_more");
            }
        };
    }

    private void showUnlockAd(PSSDK.ShortPlayBlockResultListener listener) {
        if (rewardedAd != null) {
            rewardedAd.setAdInteractionListener(new PAGRewardedAdInteractionListener() {
                @Override
                public void onUserEarnedReward(PAGRewardItem pagRewardItem) {
                }

                @Override
                public void onUserEarnedRewardFail(int i, String s) {

                }

                @Override
                public void onAdShowed() {

                }

                @Override
                public void onAdClicked() {

                }

                @Override
                public void onAdDismissed() {
                    listener.onShortPlayUnlocked();

                    // 用户看广告解锁上报日志
                    PSSDK.RevenueInfo revenueInfo = new PSSDK.RevenueInfo(PSSDK.RevenueInfo.RevenueType.IAA, PSSDK.RevenueInfo.CurrencyType.USD);
                    revenueInfo.revenue(0.1f); // 展示广告带来的收益，如果是CPM相关接口，需要提供CPM/1000的计算结果，单位美元
                    revenueInfo.adnName("xxx"); // 广告ADN名
                    revenueInfo.adFormat(PSSDK.RevenueInfo.AdFormat.REWARD_VIDEO); // 广告样式，比如激励视频等
                    revenueInfo.aboutUnlock(true); // 表示是和解锁有关

                    PSSDK.reportRevenueInfo(revenueInfo);
                }
            });
            rewardedAd.show(this);
            loadRewardAd(null);
        } else {
            loadRewardAd(new PAGRewardedAdLoadCallback() {
                @Override
                public void onError(@NonNull PAGErrorModel pagErrorModel) {
                    toast("加载广告失败");
                }

                @Override
                public void onAdLoaded(PAGRewardedAd pagRewardedAd) {
                    showUnlockAd(listener);
                }
            });
        }
    }

    private void showChooseIndexDialog() {
        ChooseIndexDialogActivity.start(this, shortPlay, playHistory.index, REQUEST_CODE_CHOOSE_INDEX);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE_INDEX && resultCode == RESULT_OK && data != null) {
            int chooseIndex = ChooseIndexDialogActivity.getChooseIndex(data);
            if (chooseIndex != -1) {
                detailFragment.startPlayIndex(chooseIndex);
            }
        } else if (requestCode == REQUEST_CODE_CHOOSE_RESOLUTION && resultCode == RESULT_OK && data != null) {
            Resolution resolution = ChooseResolutionDialogActivity.getChosenResolution(data);
            if (resolution != null) {
                currentResolution = resolution;
                detailFragment.setResolution(resolution);
            }
        }
    }

    @Override
    public void onChooseIndex(int index) {
        detailFragment.startPlayIndex(index);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logs.i(TAG, "onPause: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logs.i(TAG, "onResume: ");

        if (taskWhenResume != null) {
            taskWhenResume.run();
            taskWhenResume = null;
        }
    }

    private void showChooseResolutionDialog() {
        if (resolutions == null) {
            return;
        }
        ChooseResolutionDialogActivity.start(this, REQUEST_CODE_CHOOSE_RESOLUTION, resolutions, currentResolution);
    }

    @Override
    public void onBackPressed() {
        if (hasShowRetainDialog || rewardedAd == null) {
            super.onBackPressed();
            return;
        }
        hasShowRetainDialog = true;
        if (Math.random() > 0.5) {
            showUnlockRetainDialog();
        } else {
            showLotteryRetainDialog();
        }
    }

    private void showLotteryRetainDialog() {
        LotteryDialog dialog = new LotteryDialog();
        dialog.show(getSupportFragmentManager(), "lottery");
        dialog.setListener(new LotteryDialog.ILotteryUnlockListener() {
            @Override
            public void onChooseLottery() {
                showUnlockAd(new PSSDK.ShortPlayBlockResultListener() {
                    @Override
                    public void onShortPlayUnlocked() {
                        dialog.onAdShowed();
                    }
                });
            }

            @Override
            public void onChooseCancel() {
                DramaPlayActivity.super.onBackPressed();
            }

            @Override
            public void onGainUnlock(int unlockCount) {
                switch (unlockCount) {
                    case LotteryDialog.UNLOCK_TYPE_COUNT_1:
                        autoUnlock(1);
                        break;
                    case LotteryDialog.UNLOCK_TYPE_COUNT_6:
                        autoUnlock(6);
                        break;
                    case LotteryDialog.UNLOCK_TYPE_COUNT_ALL:
                        autoUnlock(shortPlay.total);
                        break;
                }
            }
        });
    }

    void autoUnlock(int expectUnlockCount) {
        int finalUnlockCount = 0;
        // 往后解锁
        for (int i = playHistory.index; i <= shortPlay.total && expectUnlockCount > 0; i++) {
            if (unlockedIndexes.get(i, 0) != 1) {
                unlockedIndexes.put(i, 1);
                Logs.i(TAG, "onShortPlayUnlocked: unlock " + i);
                expectUnlockCount--;
                finalUnlockCount++;
            }
        }
        // 还有多的，从前面检查解锁
        if (expectUnlockCount > 0) {
            for (int i = 1; i < playHistory.index && expectUnlockCount > 0; i++) {
                if (unlockedIndexes.get(i, 0) != 1) {
                    unlockedIndexes.put(i, 1);
                    Logs.i(TAG, "onShortPlayUnlocked: unlock " + i);
                    expectUnlockCount--;
                    finalUnlockCount++;
                }
            }
        }

        if (finalUnlockCount > 0) {
            toast("Unlocked " + finalUnlockCount + " episodes");
        }
    }

    private void showUnlockRetainDialog() {
        UnlockRetainDialog unlockMoreDialog = new UnlockRetainDialog();
        unlockMoreDialog.show(getSupportFragmentManager(), "unlock_more");
        unlockMoreDialog.setListener(new IUnlockMoreListener() {
            @Override
            public void onChooseCancel() {
                DramaPlayActivity.super.onBackPressed();
            }

            @Override
            public void onChooseUnlockMore() {
                showUnlockAd(new PSSDK.ShortPlayBlockResultListener() {
                    @Override
                    public void onShortPlayUnlocked() {
                        autoUnlock(5);
                    }
                });
            }
        });
    }

    public interface ProgressChangeListener {
        void onProgressChanged(int progress, int max);
    }

    public interface ResolutionChangeListener {
        void onResolutionChanged(String resoluton);
    }

    public interface IUnlockMoreListener {
        void onChooseCancel();

        void onChooseUnlockMore();
    }

    public static class CustomShareView extends androidx.appcompat.widget.AppCompatImageView implements PSSDK.IControlView {

        public CustomShareView(Context context) {
            super(context);
        }

        @Override
        public PSSDK.ControlViewType getControlViewType() {
            return PSSDK.ControlViewType.Share;
        }

        @Override
        public void bindItemData(ShortPlayFragment shortPlayFragment, ShortPlay shortPlay, int index) {

        }
    }

    private static class CustomProgressBar extends SeekBar implements PSSDK.IControlProgressBar, SeekBar.OnSeekBarChangeListener {

        private ShortPlayFragment shortPlayFragment;
        private int index;

        public CustomProgressBar(Context context) {
            super(context);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setMaxHeight(DpUtils.dp2px(context, 4));
            }
            setPadding(DpUtils.dp2px(context,4),0,DpUtils.dp2px(context,4),0);
            setThumb(getResources().getDrawable(R.drawable.custom_thumb, null));
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.custom_seekbar_track);
            Rect bounds = getProgressDrawable().getBounds();
            setProgressDrawable(drawable);
            getProgressDrawable().setBounds(bounds);
            setOnSeekBarChangeListener(this);
        }

        @Override
        public void onProgressChanged(int progressInSeconds, int durationInSeconds) {
            if (getMax() != durationInSeconds) {
                setMax(durationInSeconds);
            }
            setProgress(progressInSeconds);
        }

        @Override
        public void onVideoPlayStateChanged(ShortPlay shortPlay, int i, int i1) {

        }

        @Override
        public PSSDK.ControlViewType getControlViewType() {
            return PSSDK.ControlViewType.PROGRESS_BAR;
        }

        @Override
        public void bindItemData(ShortPlayFragment shortPlayFragment, ShortPlay shortPlay, int index) {
            this.shortPlayFragment = shortPlayFragment;
            this.index = index;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            shortPlayFragment.startPlayIndexAndTimeSeconds(this.index, getProgress());
        }
    }

    private static class CustomErrorView extends androidx.appcompat.widget.AppCompatTextView implements PSSDK.IControlView {

        public CustomErrorView(Context context) {
            super(context);
            setText(context.getText(R.string.s_error));
            setGravity(Gravity.CENTER);
            setBackgroundColor(Color.WHITE);
        }

        @Override
        public PSSDK.ControlViewType getControlViewType() {
            return PSSDK.ControlViewType.ERROR_PAGE;
        }

        @Override
        public void bindItemData(ShortPlayFragment shortPlayFragment, ShortPlay shortPlay, int index) {

        }
    }

    private static class CustomLoadingView extends ProgressBar implements PSSDK.IControlLoadingView {

        public CustomLoadingView(Context context) {
            super(context);
        }

        @Override
        public PSSDK.ControlViewType getControlViewType() {
            return PSSDK.ControlViewType.Loading;
        }

        @Override
        public void bindItemData(ShortPlayFragment shortPlayFragment, ShortPlay shortPlay, int index) {

        }

        @Override
        public void startAnimating() {

        }

        @Override
        public void stopAnimating() {

        }
    }

    public static class CustomCollectView extends androidx.appcompat.widget.AppCompatTextView implements PSSDK.IControlStatusView {
        private final Drawable collectDrawable;
        private final Drawable collectedDrawable;
        private PSSDK.ControlStatus status = PSSDK.ControlStatus.Normal;

        public CustomCollectView(Context context) {
            super(context);
            setGravity(Gravity.CENTER_HORIZONTAL);

            collectDrawable = getResources().getDrawable(R.drawable.collect);
            collectedDrawable = getResources().getDrawable(R.drawable.collected);
            collectDrawable.setBounds(0, 0, DpUtils.dp2px(context, 32), DpUtils.dp2px(context, 32));
            collectedDrawable.setBounds(0, 0, DpUtils.dp2px(context, 32), DpUtils.dp2px(context, 32));

            setTextColor(Color.WHITE);
        }

        @Override
        public void setCurrentStatus(ShortPlay shortPlay, int index, PSSDK.ControlStatus status, PSSDK.StatusExtraInfo extraInfo) {
            this.status = status;
            Logs.i(TAG, "setCurrentStatus-status=" + status + ",index=" + index);
            setCompoundDrawables(null, status == PSSDK.ControlStatus.Normal ? collectDrawable : collectedDrawable, null, null);
            setText(extraInfo.totalCollectCount + "");
            ShortUtils.followInsertOrDelete(getContext(), status != PSSDK.ControlStatus.Normal, shortPlay, index);
        }

        @Override
        public PSSDK.ControlStatus getCurrentStatus(ShortPlay shortPlay, int index) {
            return status;
        }

        @Override
        public PSSDK.ControlStatus onClicked(ShortPlay shortPlay, int index, PSSDK.ControlStatus currentStatus) {
            return currentStatus == PSSDK.ControlStatus.Normal ? PSSDK.ControlStatus.Selected : PSSDK.ControlStatus.Normal;
        }

        @Override
        public PSSDK.ControlViewType getControlViewType() {
            return PSSDK.ControlViewType.Collect;
        }

        @Override
        public void bindItemData(ShortPlayFragment shortPlayFragment, ShortPlay shortPlay, int index) {

        }
    }

    public static class CustomLikeView extends androidx.appcompat.widget.AppCompatTextView implements PSSDK.IControlStatusView {
        private final Drawable likeDrawable;
        private final Drawable likedDrawable;
        private PSSDK.ControlStatus status = PSSDK.ControlStatus.Normal;

        public CustomLikeView(Context context) {
            super(context);
            setGravity(Gravity.CENTER_HORIZONTAL);

            likeDrawable = getResources().getDrawable(R.drawable.like);
            likedDrawable = getResources().getDrawable(R.drawable.liked);
            likeDrawable.setBounds(0, 0, DpUtils.dp2px(context, 32), DpUtils.dp2px(context, 32));
            likedDrawable.setBounds(0, 0, DpUtils.dp2px(context, 32), DpUtils.dp2px(context, 32));

            setTextColor(Color.WHITE);
        }

        @Override
        public void setCurrentStatus(ShortPlay shortPlay, int index, PSSDK.ControlStatus status, PSSDK.StatusExtraInfo extraInfo) {
            this.status = status;

            setCompoundDrawables(null, status == PSSDK.ControlStatus.Normal ? likeDrawable : likedDrawable, null, null);
            setText(extraInfo.totalLikeCount + "");
        }

        @Override
        public PSSDK.ControlStatus getCurrentStatus(ShortPlay shortPlay, int index) {
            return status;
        }

        @Override
        public PSSDK.ControlStatus onClicked(ShortPlay shortPlay, int index, PSSDK.ControlStatus currentStatus) {
            return currentStatus == PSSDK.ControlStatus.Normal ? PSSDK.ControlStatus.Selected : PSSDK.ControlStatus.Normal;
        }

        @Override
        public PSSDK.ControlViewType getControlViewType() {
            return PSSDK.ControlViewType.Like;
        }

        @Override
        public void bindItemData(ShortPlayFragment shortPlayFragment, ShortPlay shortPlay, int index) {

        }
    }

    public static class LotteryDialog extends DialogFragment implements Handler.Callback {

        public static final int UNLOCK_TYPE_COUNT_1 = 1;
        public static final int UNLOCK_TYPE_COUNT_6 = 2;
        public static final int UNLOCK_TYPE_COUNT_ALL = 3;
        private static final int MSG_SHOW_LOTTERY_RESULT = 1;
        private static final int MSG_WAIT_USER_ENSURE = 2;
        private final Handler mHandler = new Handler(Looper.getMainLooper(), this);
        private ILotteryUnlockListener listener;
        private TextView okBtn;
        private int unlockType = 3;
        private boolean hasShowAd;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.dialog_lottery_unlock, container, false);
        }

        @Override
        public void onResume() {
            super.onResume();
            Window window = getDialog().getWindow();
            window.setBackgroundDrawable(null);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            getDialog().setCanceledOnTouchOutside(false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            okBtn = view.findViewById(R.id.btn_ok);
            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onChooseLottery();
                    }
                }
            });

            view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if (!hasShowAd && listener != null) {
                        listener.onChooseCancel();
                    }
                }
            });
        }

        public void setListener(ILotteryUnlockListener listener) {
            this.listener = listener;
        }

        public void onAdShowed() {
            okBtn.setCompoundDrawables(null, null, null, null);
            okBtn.setText("In the lottery");
            // 3s后显示抽奖结果
            mHandler.sendEmptyMessageDelayed(MSG_SHOW_LOTTERY_RESULT, 1000);
            hasShowAd = true;
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            super.onDismiss(dialog);

            mHandler.removeCallbacksAndMessages(null);
        }

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_SHOW_LOTTERY_RESULT:
                    unlockType = new Random().nextInt(3) + 1;
                    okBtn.setText("Use it now(3s)");

                    getView().findViewById(R.id.ll_lottery_result).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.ll_lottery_tip).setVisibility(View.GONE);
                    TextView resultTV = getView().findViewById(R.id.tv_lottery_result);
                    switch (unlockType) {
                        case UNLOCK_TYPE_COUNT_1:
                            resultTV.setText("1 episode unlocked");
                            break;
                        case UNLOCK_TYPE_COUNT_6:
                            resultTV.setText("6 episodes unlocked");
                            break;
                        case UNLOCK_TYPE_COUNT_ALL:
                            resultTV.setText("All episodes unlocked");
                            break;
                    }
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_WAIT_USER_ENSURE, 2, 0), 1000);
                    break;
                case MSG_WAIT_USER_ENSURE:
                    int number = msg.arg1;
                    okBtn.setText("Use it now(" + number + "s)");
                    number--;
                    if (number > 0) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_WAIT_USER_ENSURE, number, 0), 1000);
                    } else {
                        dismiss();
                        if (listener != null) {
                            listener.onGainUnlock(unlockType);
                        }
                    }
                    break;
            }
            return false;
        }

        public interface ILotteryUnlockListener {
            void onChooseLottery();

            void onChooseCancel();

            void onGainUnlock(int unlockCount);
        }
    }

    /**
     * 连续解锁弹窗
     */
    public static class UnlockMoreDialog extends DialogFragment implements Handler.Callback {

        private static final int MSG_UPDATE_OK_TEXT = 1;
        private final Handler mHandler = new Handler(Looper.getMainLooper(), this);
        private IUnlockMoreListener listener;
        private TextView okTV;
        private int count = 3;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.dialog_unlock_more, container, false);
        }

        @Override
        public void onResume() {
            super.onResume();
            Window window = getDialog().getWindow();
            window.setBackgroundDrawable(null);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

            if (count > 0) {
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_OK_TEXT, 1000);
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            mHandler.removeMessages(MSG_UPDATE_OK_TEXT);
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            super.onDismiss(dialog);
            mHandler.removeCallbacksAndMessages(null);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            TextView titleTV = view.findViewById(R.id.tv_title);
            titleTV.setText("Congrats! Episode unlocked");

            TextView descTV = view.findViewById(R.id.tv_desc);
            descTV.setText("Watch 1 more ad to unlock 2 more");

            okTV = view.findViewById(R.id.btn_ok);
            okTV.setText("Watch rewards ad (3s)");
            okTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if (listener != null) {
                        listener.onChooseUnlockMore();
                    }
                }
            });

            View unlockView = view.findViewById(R.id.btn_cancel);
            unlockView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if (listener != null) {
                        listener.onChooseCancel();
                    }
                }
            });
        }

        public void setListener(IUnlockMoreListener listener) {
            this.listener = listener;
        }

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_UPDATE_OK_TEXT) {
                okTV.setText("Watch rewards ad (" + count + "s)");
                count--;
                if (count < 0) {
                    okTV.performClick();
                } else {
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_OK_TEXT, count, 0), 1000);
                }
            }
            return false;
        }
    }

    public static class UnlockRetainDialog extends DialogFragment {

        private IUnlockMoreListener listener;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.dialog_unlock_more, container, false);
        }

        @Override
        public void onResume() {
            super.onResume();
            Window window = getDialog().getWindow();
            window.setBackgroundDrawable(null);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if (listener != null) {
                        listener.onChooseUnlockMore();
                    }
                }
            });

            View unlockView = view.findViewById(R.id.btn_cancel);
            unlockView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if (listener != null) {
                        listener.onChooseCancel();
                    }
                }
            });
        }

        public void setListener(IUnlockMoreListener listener) {
            this.listener = listener;
        }

    }

    public static class UnlockDialog extends DialogFragment {
        private UnlockListener listener;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.dialog_unlock, container, false);
        }

        @Override
        public void onResume() {
            super.onResume();
            Window window = getDialog().getWindow();
            window.setBackgroundDrawable(null);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        public void setUnlockListener(UnlockListener listener) {
            this.listener = listener;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onChooseCancel();
                    }
                    dismiss();
                }
            });

            View unlockView = view.findViewById(R.id.btn_unlock);
            unlockView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onChooseUnlock();
                    }
                    dismiss();
                }
            });
        }

        public interface UnlockListener {
            void onChooseCancel();

            void onChooseUnlock();
        }
    }

    private class CustomOverlayView extends FrameLayout implements PSSDK.IControlView, DramaPlayActivity.ProgressChangeListener, ResolutionChangeListener {

        private final TextView chooseIndexTitleTV;
        private final TextView dramaTitleTV;
        private final TextView dramaDescTV;
        private final SeekBar progressBar;
//        private final TextView tvResolution;
//        private final ImageView ivHd;

        public CustomOverlayView(Context context) {
            super(context);
            inflate(context, R.layout.player_overlay, this);

            chooseIndexTitleTV = findViewById(R.id.tv_overlay_choose_index_title);

            findViewById(R.id.ll_choose_index).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChooseIndexDialog();
                }
            });

            dramaTitleTV = findViewById(R.id.tv_overlay_drama_name);
            dramaDescTV = findViewById(R.id.tv_overlay_drama_desc);

            /*ivHd = findViewById(R.id.iv_hd);
            tvResolution = findViewById(R.id.tv_resolution);
            tvResolution.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChooseResolutionDialog();
                }
            });*/
            findViewById(R.id.iv_more).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChooseResolutionDialog();
                }
            });

            progressBar = findViewById(R.id.sb_overlay);
            progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (detailFragment != null) {
                        detailFragment.setCurrentPlayTimeSeconds(seekBar.getProgress());
                    }
                }
            });
        }

        @Override
        public PSSDK.ControlViewType getControlViewType() {
            return PSSDK.ControlViewType.CUSTOM;
        }

        @Override
        public void bindItemData(ShortPlayFragment shortPlayFragment, ShortPlay shortPlay, int index) {
            chooseIndexTitleTV.setText(shortPlay.total + getString(R.string.s_eps) + " - " + shortPlay.title);
            dramaTitleTV.setText(shortPlay.title);
            dramaDescTV.setText(shortPlay.desc);
        }

        @Override
        public void onProgressChanged(int progress, int max) {
            Logs.i(TAG, "onProgressChanged-progress=" + progress + ",max=" + max);
            if (progressBar.getMax() != max) {
                progressBar.setMax(max);
            }
            progressBar.setProgress(progress);
        }

        @Override
        public void onResolutionChanged(String resoluton) {
            Logs.i(TAG, "onResolutionChanged-resoluton=" + resoluton);
            String resolutionString = currentResolution.toString();
//            tvResolution.setText(resolutionString);
//            ivHd.setVisibility(resolutionString.contains("1080") ? View.VISIBLE : View.GONE);
        }
    }
}