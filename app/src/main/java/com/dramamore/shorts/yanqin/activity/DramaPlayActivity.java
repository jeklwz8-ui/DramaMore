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
import android.widget.LinearLayout;
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
import com.dramamore.shorts.yanqin.utils.VoiceModeHelper;
import com.google.gson.Gson;
import com.ss.ttvideoengine.Resolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DramaPlayActivity extends AppFragmentActivity implements IIndexChooseListener {
    private static final String TAG = "DramaPlayActivity";
    private static final String EXTRA_SHORT_PLAY = "short_play";
    private static final String EXTRA_SHORT_PLAY_INDEX = "short_play_index";
    private static final String EXTRA_SHORT_PLAY_FROM_SECONDS = "seconds";
    private static final int REQUEST_CODE_CHOOSE_RESOLUTION = 1; // 选择分辨率
    private static final int REQUEST_CODE_CHOOSE_INDEX = 2; // 选择索引
    private static final int FIXED_BOTTOM_BAR_HEIGHT_DP = 25; // 底部固定栏高度
    private static final int FIXED_BOTTOM_BAR_SCREEN_GAP_DP = 8; // 底部固定栏与屏幕的间距
    private static final int PROGRESS_TO_CHOOSE_GAP_DP = 5; // 进度条与选择条的间距
    private static final int BRIEF_TO_PROGRESS_GAP_DP = 8; //简介与进度条的间距
    private static final float MAX_VIDEO_SPEED = 3.0f; // 最大视频速度
    private static final float[] PLAY_SPEEDS = new float[]{1.0f, 1.5f, 2.0f};
    private static final String[] PLAY_SPEED_LABELS = new String[]{"1.0X", "1.5X", "2.0X"};
    private static final String DEFAULT_RESOLUTION_TEXT = "360P";
    private final List<PAGNativeAd> feedAds = new ArrayList<>();
    /**
     * 瀹歌尪袙闁夸胶娈戦崜褔娉?
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
    private TextView bottomChooseIndexTitleView;
    private View fixedChooseIndexView;
    private CustomOverlayView customOverlayView;
    private Resolution[] resolutions;
    private Resolution currentResolution;
    private boolean hasShowRetainDialog;
    private boolean hasShowUnlockMoreDialog;
    private boolean isInImmersiveMode;
    private int lastBottomInset;
    private Runnable taskWhenResume;
    private int currentPlaySpeedIndex = 0;

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
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        Intent intent = getIntent();
        Parcelable parcelableShortPlay = intent.getParcelableExtra(EXTRA_SHORT_PLAY);
        if (parcelableShortPlay == null) {
            finish();
            return;
        }

        shortPlay = (ShortPlay) parcelableShortPlay;

        // 濞村鐦痝son鎼村繐鍨崠鏍畱閸忕厧顔愰幀?
        Gson gson = new Gson();
        String json = gson.toJson(shortPlay);
        shortPlay = gson.fromJson(json, ShortPlay.class);
        //startFromIndex = intent.getIntExtra(EXTRA_SHORT_PLAY_INDEX, 1);
        //startFromSeconds = intent.getIntExtra(EXTRA_SHORT_PLAY_FROM_SECONDS, 0);
        if (shortPlay.episodes == null || shortPlay.episodes.isEmpty()) {
            toast("episodes is empty");
        }

        setContentView(R.layout.act_play);

        fixedChooseIndexView = findViewById(R.id.ll_choose_index_fixed);
        bottomChooseIndexTitleView = findViewById(R.id.tv_choose_index_title_fixed);
        fixedChooseIndexView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseIndexDialog();
            }
        });

        View playRoot = findViewById(R.id.play);
        ViewCompat.setOnApplyWindowInsetsListener(playRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            lastBottomInset = systemBars.bottom;
            applyBottomBarsLayout(lastBottomInset);
            return insets;
        });
        ViewCompat.requestApplyInsets(playRoot);
        // Fallback for devices/skins where insets callback may be delayed.
        playRoot.post(() -> applyBottomBarsLayout(0));

        // 妫板嫬濮炴潪鎴掍繆閹垱绁﹂獮鍨啞
        App.ensurePangleAdsSdkInit(getApplicationContext(), () -> {
            if (isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed())) {
                return;
            }
            loadPangleFeedAd();
            loadRewardAd(null);
        });

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
        // 姒涙顓婚崜?闂嗗棜袙闁?
        for (int i = 1; i <= 5; i++) {
            unlockedIndexes.put(i, 1);
        }

        PSSDK.DetailPageConfig.Builder builder = new PSSDK.DetailPageConfig.Builder();
        builder.displayTextVisibility(PSSDK.DetailPageConfig.TEXT_POS_BOTTOM_DESC, false);
        builder.displayTextVisibility(PSSDK.DetailPageConfig.TEXT_POS_BOTTOM_TITLE, false);
        builder.displayProgressBar(false);
        builder.startPlayIndex(startFromIndex);
        builder.enableImmersiveMode(10000) // 閵嗘劕褰查柅澶堚偓鎴炴尡閺€楣冦€夐弮鐘虫惙娴ｆ釜xxms閸氬酣娈ｉ挊蹇旀瀮鐎涙绻橀崗銉︾焽濞寸绱″Ο鈥崇础閿涘矂绮拋銈勭瑝閸氼垳鏁ゅ銈呭閼虫枻绱濋崥顖滄暏閺冭泛褰查幐鍥х暰閺冨爼妫?
                .playSingleItem(false); // 閵嗘劕褰查柅澶堚偓鎴濆涧閹绢厽鏂佹稉鈧梿鍡樐佸蹇ョ礉閻劋绨崷銊ョ磻閸欐垼鈧懐鏁ゆ径姘嚋閹绢厽鏂佹い绀攔agment鐎电钖勯弸鍕偓鐘崇拨閸斻劌鍨忛崜褍婧€閺咁垱妞傞敍宀勭帛鐠侇槍alse
        // 瀵偓閸氼垵鍤滈崝銊︽尡閺€鍙ョ瑓娑撯偓闂?
        builder.enableAutoPlayNext(true);
        builder.startPlayAtTimeSeconds(startFromSeconds);
        builder.hideLeftTopCloseAndTitle(false, new PSSDK.ShortPlayDetailPageCloseListener() {
            @Override
            public boolean onCloseClicked() {
                onBackPressed();
                return true;
            }
        });

        // 闁板秶鐤嗛獮鍨啞缁涙牜鏆?
        builder.adCustomProvider(new PSSDK.AdCustomProvider() {
            @Override
            public List<Integer> getDetailDrawAdPositions() {
                ArrayList<Integer> integers = new ArrayList<>();
                // 閸︺劎顑?闂嗗棎鈧胶顑?闂嗗棎鈧胶顑?0闂嗗棗鎮楅棃銏″絻閸忋儱绠嶉崨?
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
                        // 韫囶偄鍨濋崚鏉跨畭閸涘﹥褰冮崗銉ょ秴缂冾喗妞傜拫鍐暏閿涘苯褰叉禒銉ユ躬鏉╂瑩鍣风拠閿嬬湴楠炲灝鎲?
                        loadPangleFeedAd();
                    }

                    @Override
                    public View onObtainAdView(int position, int index) {
                        // 鏉╂柨娲栭獮鍨啞View閿涘苯顩у▽鈩冩箒閸欘垳鏁ら獮鍨啞閸掓瑨绻戦崶鐎梪ll
                        //return createFeedAdView();
                        return null;
                    }

                    @Override
                    public void onDestroy() {
                        // 閹绢厽鏂佹い鐢糕偓鈧崙鐑樻鐠嬪啰鏁ら敍灞藉讲閸︺劏绻栭柌宀勫櫞閺€鎯х畭閸涘﹨绁┃?
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
                Logs.i(TAG, "onProgressChange:index=" + index + ", progress=" + currentPlayTimeInSeconds + "/" + durationInSeconds);

                playHistory.index = index;
                playHistory.seconds = currentPlayTimeInSeconds;

                if (progressChangeListener != null) {
                    progressChangeListener.onProgressChanged(currentPlayTimeInSeconds, durationInSeconds);
                }
            }

            @Override
            public boolean onPlayFailed(PSSDK.ErrorInfo errorInfo) {
                // 鐟欏棝顣堕幘顓熸杹婢惰精瑙?
                Logs.i(TAG, "onPlayFailed() called with: errorInfo = [" + errorInfo + "]");
                if (errorInfo.code == PSSDK.ErrorInfo.ERROR_CODE_CURRENT_COUNTRY_NOT_SUPPORT) {
                    // 瑜版挸澧犻崷鏉垮隘娑撳秵鏁幐浣规尡閺€鎾呯礉SDK娴兼瓖oast閹绘劗銇氶敍灞界磻閸欐垼鈧懍绡冮崣顖欎簰閸︺劍顒濋弮鑸垫▔缁€鍝勮剨缁愭鐡戦弴鏉戝几婵傜晫娈戦幓鎰仛
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DramaPlayActivity.this);
                    dialogBuilder.setMessage("Current region is not supported for playback");
                    dialogBuilder.create().show();
                    // return true鐞涖劎銇氶弴鎸庡床閹哄DK閸愬懐娈慣oast閹绘劗銇?
                    return true;
                }
                return false;
            }

            @Override
            public void onShortPlayPlayed(ShortPlay shortPlay, int index, EpisodeData episodeData) {
                // 濮ｅ繋绔撮梿鍡楃磻婵鎸遍弨鐐閸ョ偠鐨熼敍灞藉讲閻劍娼电拋鏉跨秿閹绢厽鏂侀崢鍡楀蕉
                Logs.i(TAG, "onShortPlayPlayed() called with: shortPlay = [" + shortPlay + "], index = [" + index + "]");
                if (bottomChooseIndexTitleView != null) {
                    bottomChooseIndexTitleView.setText(buildChooseIndexTitle(shortPlay, index));
                }

                if (shortPlay.isCollected) {//瀹稿弶鏁归挊蹇撳灟閺囧瓨鏌婇崫顏冪闂?
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
                    // 閸掓銆冮柌灞炬Ц楠炲灝鎲￠弮璁圭礉鎼存洟鍎寸亸鍙樼瑝閸氬本妞傞弰鍓с仛banner娴滃棴绱濊ぐ鍗炴惙娴ｆ捇鐛欓敍灞惧床閹存劖娅橀柅姝穒ew
                } else {
                    // 閸掓銆冮柌灞炬Ц鐟欏棝顣堕敍灞藉讲娴犮儲妯夌粈鍝勭俺闁暈anner
                    if (bannerView != null) {
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
                // 鏉╂稑鍙嗗▽澶嬭箞瀵繑膩瀵?
                Logs.i(TAG, "onEnterImmersiveMode() called");
                updateImmersiveModeUi(true);
            }

            @Override
            public void onExitImmersiveMode() {
                // 闁偓閸戠儤鐭囧ù绋跨础濡€崇础
                Logs.i(TAG, "onExitImmersiveMode() called");
                updateImmersiveModeUi(false);
            }

            @Override
            public boolean isNeedBlock(ShortPlay shortPlay, int index) {
                // 鐠囥垽妫秈ndex闂嗗棙妲搁崥锕傛敚鐎规熬绱漷rue闁夸礁鐣鹃崥搴″灟鐠囥儵娉﹂弮鐘崇《閼奉亜濮╅幘顓熸杹閿涘矂娓剁憰渚€鈧俺绻僺howAdIfNeed闁插苯鐣幋鎰掗柨?
                // 姒涙顓荤€佃鐦℃稉鈧梿鍡楁綆娴兼俺顕楅梻顕嗙礉娑撯偓閺冿箒绻戦崶鐎巃lse閸掓瑦顒濋幘顓熸杹妞ゅ吀绗夋导姘晙鐠囥垽妫剁拠銉╂肠
                Logs.i(TAG, "isNeedBlock() called with: shortPlay = [" + shortPlay + "], index = [" + index + "]");
                //return unlockedIndexes.get(index, 0) != 1;
                return false;
            }

            @Override
            public void showAdIfNeed(ShortPlay shortPlay, int index, PSSDK.ShortPlayBlockResultListener listener) {
                // 瑜版悆sNeedBlock閹稿洤鐣緄ndex闂嗗棝鏀ｇ€规艾鎮楅敍灞芥躬閻劍鍩涢崚鍥ㄥ床閸掓媽顕氶梿鍡樻閿涘DK娑撳秳绱伴幘顓熸杹鐟欏棝顣堕敍灞芥倱閺冩湹绱扮拫鍐暏濮濄倕娲栫拫鍐跨礉閸欘垰婀銈嗘鐏炴洜銇氬┑鈧崝鍗炵畭閸涘﹥鍨ㄧ拹顓濇嫳缁涘姘︽禍鎺炵礉閻劍鍩涙潏鐐灇閸氬氦鐨熼悽鈺╥stener.onShortPlayUnlocked閸涘﹦鐓DK閸欘垱鎸遍弨鎹愵嚉闂?
                Logs.i(TAG, "showAdIfNeed() called with: shortPlay = [" + shortPlay + "], index = [" + index + "], listener = [" + listener + "]");
                showUnLockDialog(shortPlay, index, listener);
            }

            @Override
            public void onVideoInfoFetched(ShortPlay shortPlay, int index, PSSDK.VideoPlayInfo videoPlayInfo) {
                currentVideoPlayInfo = videoPlayInfo;
                // 濮ｅ繋绔撮梿鍡氼潒妫版垵鍣径鍥с偨閺冩儼鐨熼悽銊︻劃閺傝纭堕崨濠勭叀閺堫剟娉﹂惃鍕潒妫版垳淇婇幁?
                resolutions = videoPlayInfo.supportResolutions;
                // 瑜版挸澧犻崚鍡氶哺閻?
                currentResolution = videoPlayInfo.currentResolution;
                currentResolution = resolveEffectiveResolution(currentResolution);

                if (resolutionChangeListener != null) {
                    resolutionChangeListener.onResolutionChanged(getResolutionLabel(currentResolution));
                }
                applyCurrentPlaySpeed();
                Logs.i(TAG, "onVideoInfoFetched: currentResolution=" + currentResolution);
            }

            @Override
            public List<View> onObtainPlayerControlViews() {
                ArrayList<View> views = new ArrayList<>();

                // 閸掑棔闊╅幐澶愭尦
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
                        startActivity(Intent.createChooser(intent, "閸掑棔闊╅惌顓炲⒔"));
                    }
                });

                // 閻愮绂愰幐澶愭尦
                CustomLikeView customLikeView = new CustomLikeView(getApplicationContext());
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                params.bottomMargin = DpUtils.dp2px(getApplicationContext(), 200);
                params.rightMargin = DpUtils.dp2px(getApplicationContext(), 16);
                customLikeView.setLayoutParams(params);
                views.add(customLikeView);

                // 閺€鎯版閹稿鎸?
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

                // 閼奉亜鐣炬稊澶娿亼鐠愩儳鏅棃?
                CustomErrorView errorView = new CustomErrorView(getApplicationContext());
                errorView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        detailFragment.startPlay();
                    }
                });
                views.add(errorView);

                // 閼奉亜鐣炬稊澶庣箻鎼达附娼?
                customOverlayView = new CustomOverlayView(getApplicationContext());
                params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                params.bottomMargin = 0;
                customOverlayView.setLayoutParams(params);
                customOverlayView.setImmersiveMode(isInImmersiveMode);
                progressChangeListener = customOverlayView;
                resolutionChangeListener = customOverlayView;
                views.add(customOverlayView);
                applyBottomBarsLayout(lastBottomInset);
                return views;
            }
        });
        if (detailFragment == null) {
            Toast.makeText(this, "Create player page failed", Toast.LENGTH_SHORT).show();
            return;
        }

        // 鐏忓棙鎸遍弨楣冦€夌仦鏇犮仛閸戠儤娼?
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, detailFragment).show(detailFragment).commit();

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
                        // 鐠囥垽妫舵潻鐐电敾鐟欙綁鏀?
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

                    // 閻劍鍩涢惇瀣畭閸涘﹨袙闁夸椒绗傞幎銉︽）韫?
                    PSSDK.RevenueInfo revenueInfo = new PSSDK.RevenueInfo(PSSDK.RevenueInfo.RevenueType.IAA, PSSDK.RevenueInfo.CurrencyType.USD);
                    revenueInfo.revenue(0.1f); // 鐏炴洜銇氶獮鍨啞鐢附娼甸惃鍕暪閻╁绱濇俊鍌涚亯閺勭枌PM閻╃鍙ч幒銉ュ經閿涘矂娓剁憰浣瑰絹娓氭保PM/1000閻ㄥ嫯顓哥粻妤冪波閺嬫粣绱濋崡鏇氱秴缂囧骸鍘?
                    revenueInfo.adnName("xxx"); // 楠炲灝鎲DN閸?
                    revenueInfo.adFormat(PSSDK.RevenueInfo.AdFormat.REWARD_VIDEO); // 楠炲灝鎲￠弽宄扮础閿涘本鐦俊鍌涚负閸旇精顫嬫０鎴犵搼
                    revenueInfo.aboutUnlock(true); // 鐞涖劎銇氶弰顖氭嫲鐟欙綁鏀ｉ張澶婂彠

                    PSSDK.reportRevenueInfo(revenueInfo);
                }
            });
            rewardedAd.show(this);
            loadRewardAd(null);
        } else {
            loadRewardAd(new PAGRewardedAdLoadCallback() {
                @Override
                public void onError(@NonNull PAGErrorModel pagErrorModel) {
                    toast("load ad failed");
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

    private float getCurrentPlaySpeed() {
        return PLAY_SPEEDS[getCurrentPlaySpeedIndex()];
    }

    private String getCurrentPlaySpeedLabel() {
        return PLAY_SPEED_LABELS[getCurrentPlaySpeedIndex()];
    }

    private int getCurrentPlaySpeedIndex() {
        if (currentPlaySpeedIndex < 0 || currentPlaySpeedIndex >= PLAY_SPEEDS.length) {
            currentPlaySpeedIndex = 0;
        }
        return currentPlaySpeedIndex;
    }

    private String getResolutionLabel(@Nullable Resolution resolution) {
        if (resolution == null) {
            return "";
        }
        return resolution.toString().toUpperCase(Locale.US);
    }

    @Nullable
    private Resolution resolveEffectiveResolution(@Nullable Resolution preferredResolution) {
        if (preferredResolution != null) {
            return preferredResolution;
        }
        if (resolutions != null) {
            for (Resolution resolution : resolutions) {
                if (resolution != null) {
                    return resolution;
                }
            }
        }
        return null;
    }

    private String getResolutionButtonText(@Nullable Resolution resolution) {
        String resolutionLabel = getResolutionLabel(resolveEffectiveResolution(resolution));
        if (TextUtils.isEmpty(resolutionLabel)) {
            return DEFAULT_RESOLUTION_TEXT;
        }
        if (resolutionLabel.startsWith("HD ")) {
            return resolutionLabel;
        }
        if (resolutionLabel.contains("1080") || resolutionLabel.contains("720")) {
            return "HD " + resolutionLabel;
        }
        return resolutionLabel;
    }

    private String buildChooseIndexTitle(@NonNull ShortPlay shortPlay, int index) {
        int currentEpisode = index <= 0 ? 1 : index;
        int totalEpisodes = shortPlay.total > 0 ? shortPlay.total : (shortPlay.episodes == null ? 0 : shortPlay.episodes.size());
        if (totalEpisodes <= 0) {
            return "\u9009\u96c6";
        }
        if (currentEpisode > totalEpisodes) {
            currentEpisode = totalEpisodes;
        }
        return "\u9009\u96c6 \u00b7 \u7b2c" + currentEpisode + "\u96c6 \u00b7 \u5168" + totalEpisodes + "\u96c6";
    }

    private void applyCurrentPlaySpeed() {
        float speed = getCurrentPlaySpeed();
        if (speed <= 0f) {
            return;
        }
        if (speed > MAX_VIDEO_SPEED) {
            speed = MAX_VIDEO_SPEED;
        }
        if (detailFragment != null) {
            detailFragment.setVideoSpeed(speed);
        }
    }

    private void applyBottomBarsLayout(int bottomInset) {
        if (fixedChooseIndexView == null) {
            return;
        }
        int fixedBottomGapPx = DpUtils.dp2px(this, FIXED_BOTTOM_BAR_SCREEN_GAP_DP);
        int fixedBarBottomMarginPx = bottomInset + fixedBottomGapPx;
        int fixedBarHeightPx = DpUtils.dp2px(this, FIXED_BOTTOM_BAR_HEIGHT_DP);
        // SDK overlay may already include system insets internally.
        // Keep overlay controls relative to choose-bar height only, avoid double insets.
        int progressBottomMarginPx = fixedBarHeightPx + DpUtils.dp2px(this, PROGRESS_TO_CHOOSE_GAP_DP);
        int briefBottomMarginPx = progressBottomMarginPx + DpUtils.dp2px(this, BRIEF_TO_PROGRESS_GAP_DP);

        FrameLayout.LayoutParams fixedBarParams = (FrameLayout.LayoutParams) fixedChooseIndexView.getLayoutParams();
        fixedBarParams.bottomMargin = fixedBarBottomMarginPx;
        fixedChooseIndexView.setLayoutParams(fixedBarParams);

        if (customOverlayView != null) {
            customOverlayView.updateBottomLayout(briefBottomMarginPx, progressBottomMarginPx);
        }
    }

    private void updateImmersiveModeUi(boolean immersiveMode) {
        isInImmersiveMode = immersiveMode;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fixedChooseIndexView != null) {
                    fixedChooseIndexView.setVisibility(immersiveMode ? View.GONE : View.VISIBLE);
                }
                if (customOverlayView != null) {
                    customOverlayView.setImmersiveMode(immersiveMode);
                }
            }
        });
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
        // 瀵扳偓閸氬氦袙闁?
        for (int i = playHistory.index; i <= shortPlay.total && expectUnlockCount > 0; i++) {
            if (unlockedIndexes.get(i, 0) != 1) {
                unlockedIndexes.put(i, 1);
                Logs.i(TAG, "onShortPlayUnlocked: unlock " + i);
                expectUnlockCount--;
                finalUnlockCount++;
            }
        }
        // 鏉╂ɑ婀佹径姘辨畱閿涘奔绮犻崜宥夋桨濡偓閺屻儴袙闁?
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
                setMaxHeight(DpUtils.dp2px(context, 2));
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
            // 3s閸氬孩妯夌粈鐑樺▕婵傛牜绮ㄩ弸?
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
     * 鏉╃偟鐢荤憴锝夋敚瀵湱鐛?
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

        private final View briefLayout;
        private final TextView dramaTitleTV;
        private final TextView voiceModeTV;
        private final ImageView dramaCoverIV;
        private final TextView speedTV;
        private final TextView resolutionTV;
        private final View speedResolutionMenuLayout;
        private final LinearLayout speedMenuPanel;
        private final LinearLayout resolutionMenuPanel;
        private final SeekBar progressBar;
        private final int speedResolutionNormalTextColor = Color.parseColor("#CCFFFFFF");
        private final int speedResolutionActiveTextColor = Color.parseColor("#FFF84E40");
        private int speedButtonDefaultTextColor;
        private int resolutionButtonDefaultTextColor;
        private static final int MENU_TYPE_NONE = 0;
        private static final int MENU_TYPE_SPEED = 1;
        private static final int MENU_TYPE_RESOLUTION = 2;
        private int currentExpandedMenuType = MENU_TYPE_NONE;
        private boolean isImmersiveMode;
        @Nullable
        private AlertDialog voiceModeDialog;
//        private final TextView tvResolution;
//        private final ImageView ivHd;

        public CustomOverlayView(Context context) {
            super(context);
            inflate(context, R.layout.player_overlay, this);

            briefLayout = findViewById(R.id.ll_overlay_brief);

            dramaTitleTV = findViewById(R.id.tv_overlay_drama_name);
            voiceModeTV = findViewById(R.id.tv_voice_mode);
            voiceModeTV.setText(VoiceModeHelper.getModeLabel(VoiceModeHelper.getMode(getContext())));
            voiceModeTV.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showVoiceModeDialog();
                }
            });
            dramaCoverIV = findViewById(R.id.iv_overlay_cover);
            speedTV = findViewById(R.id.tv_speed);
            speedButtonDefaultTextColor = speedTV.getCurrentTextColor();
            speedTV.setText(getCurrentPlaySpeedLabel());
            speedTV.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSpeedResolutionMenu(MENU_TYPE_SPEED, speedTV);
                }
            });

            resolutionTV = findViewById(R.id.tv_resolution);
            resolutionButtonDefaultTextColor = resolutionTV.getCurrentTextColor();
            resolutionTV.setText(getResolutionButtonText(currentResolution));
            resolutionTV.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSpeedResolutionMenu(MENU_TYPE_RESOLUTION, resolutionTV);
                }
            });

            speedResolutionMenuLayout = findViewById(R.id.ll_speed_resolution_menu);
            speedMenuPanel = findViewById(R.id.ll_speed_menu_panel);
            resolutionMenuPanel = findViewById(R.id.ll_resolution_menu_panel);
            speedResolutionMenuLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setExpandedMenuType(MENU_TYPE_NONE);
                }
            });
            refreshSpeedMenuItems();
            refreshResolutionMenuItems();

            /*ivHd = findViewById(R.id.iv_hd);
            tvResolution = findViewById(R.id.tv_resolution);
            tvResolution.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChooseResolutionDialog();
                }
            });*/

            progressBar = findViewById(R.id.sb_overlay);
            Drawable progressDrawable = ContextCompat.getDrawable(getContext(), R.drawable.custom_seekbar_track);
            if (progressDrawable != null) {
                progressBar.setProgressDrawable(progressDrawable);
            }
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
            String chooseIndexTitle = buildChooseIndexTitle(shortPlay, index);
            if (bottomChooseIndexTitleView != null) {
                bottomChooseIndexTitleView.setText(chooseIndexTitle);
            }
            dramaTitleTV.setText(shortPlay.title);
            Glide.with(getContext())
                    .load(shortPlay.coverImage)
                    .into(dramaCoverIV);
            speedTV.setText(getCurrentPlaySpeedLabel());
            resolutionTV.setText(getResolutionButtonText(currentResolution));
            voiceModeTV.setText(VoiceModeHelper.getModeLabel(VoiceModeHelper.getMode(getContext())));
            refreshResolutionMenuItems();
            refreshSpeedMenuItems();
            setExpandedMenuType(MENU_TYPE_NONE);
            applyImmersiveModeVisibility();
        }

        private void showVoiceModeDialog() {
            Activity activity = DramaPlayActivity.this;
            if (activity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed())) {
                return;
            }
            final String[] items = new String[]{"不限", "配音", "原音"};
            int currentMode = VoiceModeHelper.getMode(getContext());
            dismissVoiceModeDialogIfShowing();
            voiceModeDialog = new AlertDialog.Builder(activity)
                    .setTitle("选择音频模式")
                    .setSingleChoiceItems(items, currentMode, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            VoiceModeHelper.setMode(getContext(), which);
                            voiceModeTV.setText(VoiceModeHelper.getModeLabel(which));
                            dialog.dismiss();

                            Toast.makeText(
                                    getContext(),
                                    "已切换为" + VoiceModeHelper.getModeLabel(which) + "，返回列表后生效",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    })
                    .create();
            voiceModeDialog.show();
        }

        private void dismissVoiceModeDialogIfShowing() {
            if (voiceModeDialog != null && voiceModeDialog.isShowing()) {
                voiceModeDialog.dismiss();
            }
            voiceModeDialog = null;
        }

        @Override
        protected void onDetachedFromWindow() {
            dismissVoiceModeDialogIfShowing();
            super.onDetachedFromWindow();
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
            resolutionTV.setText(getResolutionButtonText(currentResolution));
            refreshResolutionMenuItems();
//            tvResolution.setText(resolutionString);
//            ivHd.setVisibility(resolutionString.contains("1080") ? View.VISIBLE : View.GONE);
        }

        void setImmersiveMode(boolean immersiveMode) {
            isImmersiveMode = immersiveMode;
            if (immersiveMode) {
                setExpandedMenuType(MENU_TYPE_NONE);
            }
            applyImmersiveModeVisibility();
        }

        private void applyImmersiveModeVisibility() {
            int visibility = isImmersiveMode ? View.GONE : View.VISIBLE;
            speedTV.setVisibility(visibility);
            resolutionTV.setVisibility(visibility);
            briefLayout.setVisibility(visibility);
        }

        void updateBottomLayout(int briefBottomMarginPx, int progressBottomMarginPx) {
            FrameLayout.LayoutParams briefParams = (FrameLayout.LayoutParams) briefLayout.getLayoutParams();
            if (briefParams.bottomMargin != briefBottomMarginPx) {
                briefParams.bottomMargin = briefBottomMarginPx;
                briefLayout.setLayoutParams(briefParams);
            }
            FrameLayout.LayoutParams progressParams = (FrameLayout.LayoutParams) progressBar.getLayoutParams();
            if (progressParams.bottomMargin != progressBottomMarginPx) {
                progressParams.bottomMargin = progressBottomMarginPx;
                progressBar.setLayoutParams(progressParams);
            }
        }

        private void toggleSpeedResolutionMenu(int menuType, @NonNull View anchorView) {
            if (isImmersiveMode) {
                return;
            }
            if (currentExpandedMenuType == menuType) {
                setExpandedMenuType(MENU_TYPE_NONE);
                return;
            }
            if (menuType == MENU_TYPE_SPEED) {
                refreshSpeedMenuItems();
            } else if (menuType == MENU_TYPE_RESOLUTION) {
                refreshResolutionMenuItems();
            }
            setExpandedMenuType(menuType);
            speedResolutionMenuLayout.post(new Runnable() {
                @Override
                public void run() {
                    updateMenuPosition(anchorView);
                }
            });
        }

        private void updateMenuPosition(@NonNull View anchorView) {
            View parentView = (View) speedResolutionMenuLayout.getParent();
            if (parentView == null) {
                return;
            }
            FrameLayout.LayoutParams menuLayoutParams = (FrameLayout.LayoutParams) speedResolutionMenuLayout.getLayoutParams();
            menuLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
            int menuWidth = speedResolutionMenuLayout.getWidth();
            if (menuWidth <= 0) {
                menuWidth = speedResolutionMenuLayout.getMeasuredWidth();
            }
            if (menuWidth <= 0) {
                menuWidth = DpUtils.dp2px(getContext(), currentExpandedMenuType == MENU_TYPE_SPEED ? 56 : 70);
            }
            int anchorCenterX = anchorView.getLeft() + anchorView.getWidth() / 2;
            int desiredMenuLeft = anchorCenterX - menuWidth / 2;
            int maxLeft = Math.max(0, parentView.getWidth() - menuWidth);
            int clampedLeft = Math.max(0, Math.min(desiredMenuLeft, maxLeft));
            menuLayoutParams.topMargin = anchorView.getBottom() + DpUtils.dp2px(getContext(), 6);
            menuLayoutParams.rightMargin = Math.max(0, parentView.getWidth() - (clampedLeft + menuWidth));
            speedResolutionMenuLayout.setLayoutParams(menuLayoutParams);
        }

        private void setExpandedMenuType(int menuType) {
            currentExpandedMenuType = menuType;
            speedResolutionMenuLayout.setVisibility(menuType == MENU_TYPE_NONE ? View.GONE : View.VISIBLE);
            speedMenuPanel.setVisibility(menuType == MENU_TYPE_SPEED ? View.VISIBLE : View.GONE);
            resolutionMenuPanel.setVisibility(menuType == MENU_TYPE_RESOLUTION ? View.VISIBLE : View.GONE);
            speedTV.setSelected(menuType == MENU_TYPE_SPEED);
            resolutionTV.setSelected(menuType == MENU_TYPE_RESOLUTION);

            speedTV.setTextColor(menuType == MENU_TYPE_SPEED ? speedResolutionActiveTextColor : speedButtonDefaultTextColor);
            resolutionTV.setTextColor(menuType == MENU_TYPE_RESOLUTION ? speedResolutionActiveTextColor : resolutionButtonDefaultTextColor);
        }

        private void refreshSpeedMenuItems() {
            speedMenuPanel.removeAllViews();
            for (int i = 0; i < PLAY_SPEED_LABELS.length; i++) {
                final int speedIndex = i;
                TextView itemView = createMenuItemView(PLAY_SPEED_LABELS[i], i == getCurrentPlaySpeedIndex());
                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentPlaySpeedIndex = speedIndex;
                        applyCurrentPlaySpeed();
                        speedTV.setText(getCurrentPlaySpeedLabel());
                        refreshSpeedMenuItems();
                        setExpandedMenuType(MENU_TYPE_NONE);
                    }
                });
                speedMenuPanel.addView(itemView);
            }
        }

        private void refreshResolutionMenuItems() {
            resolutionMenuPanel.removeAllViews();
            List<Resolution> availableResolutions = new ArrayList<>();
            if (resolutions != null) {
                for (Resolution resolution : resolutions) {
                    if (resolution != null) {
                        availableResolutions.add(resolution);
                    }
                }
            }
            if (availableResolutions.isEmpty() && currentResolution != null) {
                availableResolutions.add(currentResolution);
            }
            if (availableResolutions.isEmpty()) {
                TextView emptyView = createMenuItemView("AUTO", false);
                emptyView.setClickable(false);
                emptyView.setTextColor(speedResolutionNormalTextColor);
                resolutionMenuPanel.addView(emptyView);
                return;
            }
            for (Resolution resolution : availableResolutions) {
                final Resolution selectedResolution = resolution;
                String resolutionLabel = getResolutionLabel(resolution);
                boolean isSelected = resolutionLabel.equals(getResolutionLabel(currentResolution));
                TextView itemView = createMenuItemView(resolutionLabel, isSelected);
                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentResolution = selectedResolution;
                        if (detailFragment != null) {
                            detailFragment.setResolution(selectedResolution);
                        }
                        resolutionTV.setText(getResolutionButtonText(selectedResolution));
                        refreshResolutionMenuItems();
                        setExpandedMenuType(MENU_TYPE_NONE);
                    }
                });
                resolutionMenuPanel.addView(itemView);
            }
        }

        private TextView createMenuItemView(String text, boolean isSelected) {
            TextView textView = new TextView(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    DpUtils.dp2px(getContext(), 28)
            );
            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER);
            textView.setText(text);
            textView.setTextSize(12f);
            textView.setTextColor(isSelected ? speedResolutionActiveTextColor : speedResolutionNormalTextColor);
            textView.setBackgroundColor(isSelected ? Color.parseColor("#40FFFFFF") : Color.TRANSPARENT);
            return textView;
        }
    }
}




