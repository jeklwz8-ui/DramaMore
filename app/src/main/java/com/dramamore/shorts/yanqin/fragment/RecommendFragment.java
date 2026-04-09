package com.dramamore.shorts.yanqin.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bytedance.sdk.shortplay.api.EpisodeData;
import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.bytedance.sdk.shortplay.api.ShortPlayFragment;
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.activity.DramaPlayActivity;
import com.dramamore.shorts.yanqin.dialog.IndexChooseDialog;
import com.dramamore.shorts.yanqin.listener.IIndexChooseListener;
import com.dramamore.shorts.yanqin.utils.DpUtils;
import com.dramamore.shorts.yanqin.utils.Logs;
import com.ss.ttvideoengine.Resolution;
import com.ss.ttvideoengine.TTVideoEngineInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecommendFragment extends Fragment {
    private static final String TAG = "RecommendFragment";
    private static final int MAX_CONTINUOUS_FILTERED_EMPTY_PAGES = 8;
    private int currentPage = 1;
    private boolean hasMore = false;
    private boolean isLoading = false;
    private int continuousFilteredEmptyPages = 0;
    private int currentSelectedPosition = 0;
    private ViewPager2 recommendViewPager;
    private View recommendBottomActionsView;
    private TextView recommendChooseIndexTitleTV;
    private TextView recommendPlayAllTV;
    private SeekBar recommendProgressBar;
    private boolean isBottomProgressTracking;
    private final Map<Integer, ProgressState> progressStateMap = new HashMap<>();
    @Nullable
    private ShortPlay currentVisibleShortPlay;
    private int currentVisibleEpisodeIndex = 1;
    @Nullable
    private IndexChooseDialog chooseIndexDialog;
    @Nullable
    private Runnable pendingStartPlaybackRunnable;
    private FeedListAdapter feedListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommend, container, false);

        initViewPage(view);

        return view;
    }

    private void initViewPage(View view) {
        recommendViewPager = view.findViewById(R.id.vp_shortplay_feed);
        recommendBottomActionsView = view.findViewById(R.id.ll_recommend_bottom_actions);
        recommendChooseIndexTitleTV = view.findViewById(R.id.tv_recommend_choose_index_title);
        recommendPlayAllTV = view.findViewById(R.id.tv_recommend_play_all);
        recommendProgressBar = view.findViewById(R.id.sb_recommend_progress);
        recommendProgressBar.setMax(1);
        recommendProgressBar.setProgress(0);
        recommendProgressBar.setVisibility(View.GONE);
        recommendProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isBottomProgressTracking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isBottomProgressTracking = false;
                if (feedListAdapter == null) {
                    return;
                }
                ShortPlayFragment currentFragment = feedListAdapter.getFragmentByPosition(currentSelectedPosition);
                if (currentFragment != null) {
                    currentFragment.setCurrentPlayTimeSeconds(seekBar.getProgress());
                }
            }
        });
        View chooseIndexView = view.findViewById(R.id.ll_recommend_choose_index);
        chooseIndexView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseIndexDialog();
            }
        });
        recommendPlayAllTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentVisibleShortPlay != null) {
                    DramaPlayActivity.start(v.getContext(), currentVisibleShortPlay);
                }
            }
        });
        if (recommendBottomActionsView != null) {
            recommendBottomActionsView.setVisibility(View.GONE);
        }
        recommendViewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        feedListAdapter = new FeedListAdapter(this);
        recommendViewPager.setAdapter(feedListAdapter);
        recommendViewPager.setOffscreenPageLimit(3);
        recommendViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                currentSelectedPosition = position;
                refreshBottomActionsForPosition(position);
                refreshBottomProgressForPosition(position);
                startPlaybackForPosition(position);
                int nextPos = position + 1;
                ShortPlayFragment playFragment = feedListAdapter.getFragmentByPosition(nextPos);
                Log.d(TAG, "棰勫姞杞戒笅涓€閮ㄥ墽锛歱os=" + nextPos + ", " + playFragment);
                if (playFragment != null) {
                    playFragment.preLoadVideo(new PSSDK.ActionResultListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "onSuccess: preload success");
                        }

                        @Override
                        public void onFail(PSSDK.ErrorInfo errorInfo) {
                            Log.d(TAG, "onSuccess: preload failed");
                        }
                    });
                }

                if (position == feedListAdapter.getItemCount() - 1 && hasMore) {
                    Logs.i(TAG, "loadMoreData-鍔犺浇鏇村-pos=" + position + ",hasMore=" + hasMore);
                    loadMoreData();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoading && (currentPage == 1 || (feedListAdapter != null && feedListAdapter.getItemCount() == 0))) {
            loadMoreData();
        } else {
            startRecommendPlayback();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseRecommendPlayback();
    }

    @Override
    public void onStop() {
        super.onStop();
        pauseRecommendPlayback();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            pauseRecommendPlayback();
        } else {
            if (!isLoading && feedListAdapter != null && feedListAdapter.getItemCount() == 0) {
                loadMoreData();
            } else {
                startRecommendPlayback();
            }
        }
    }

    @Override
    public void onDestroyView() {
        stopRecommendPlayback();
        dismissChooseIndexDialog();
        if (feedListAdapter != null) {
            feedListAdapter.destroy();
            feedListAdapter = null;
        }
        currentVisibleShortPlay = null;
        currentVisibleEpisodeIndex = 1;
        recommendBottomActionsView = null;
        recommendChooseIndexTitleTV = null;
        recommendPlayAllTV = null;
        isBottomProgressTracking = false;
        progressStateMap.clear();
        recommendProgressBar = null;
        recommendViewPager = null;
        super.onDestroyView();
    }

    private void pauseRecommendPlayback() {
        dismissChooseIndexDialog();
        cancelPendingStartPlayback();
        if (feedListAdapter != null) {
            feedListAdapter.pauseAllPlayback();
        }
    }

    private void stopRecommendPlayback() {
        cancelPendingStartPlayback();
        if (feedListAdapter != null) {
            feedListAdapter.stopAllPlayback();
        }
    }

    public void forcePausePlayback() {
        pauseRecommendPlayback();
    }



    private void startRecommendPlayback() {
        if (!isAdded() || isHidden() || recommendViewPager == null) {
            return;
        }
        startPlaybackForPosition(currentSelectedPosition);
    }

    private void startPlaybackForPosition(int position) {
        cancelPendingStartPlayback();
        if (!isAdded() || isHidden() || recommendViewPager == null) {
            return;
        }
        if (feedListAdapter == null) {
            return;
        }
        ShortPlayFragment fragment = feedListAdapter.getFragmentByPosition(position);
        if (fragment != null) {
            fragment.startPlay();
            return;
        }
        if (recommendViewPager != null) {
            pendingStartPlaybackRunnable = new Runnable() {
                @Override
                public void run() {
                    pendingStartPlaybackRunnable = null;
                    if (!isAdded() || isHidden() || feedListAdapter == null) {
                        return;
                    }
                    ShortPlayFragment delayedFragment = feedListAdapter.getFragmentByPosition(position);
                    if (delayedFragment != null) {
                        delayedFragment.startPlay();
                    }
                }
            };
            recommendViewPager.postDelayed(pendingStartPlaybackRunnable,120);
        }
    }

    private void cancelPendingStartPlayback() {
        if (recommendViewPager != null && pendingStartPlaybackRunnable != null) {
            recommendViewPager.removeCallbacks(pendingStartPlaybackRunnable);
        }
        pendingStartPlaybackRunnable = null;
    }


    private void loadMoreData() {
        if (isLoading) {
            return;
        }
        isLoading = true;
        PSSDK.requestFeedList(currentPage, 20, feedListLoadResult);
    }

    private PSSDK.FeedListResultListener feedListLoadResult = new PSSDK.FeedListResultListener() {
        @Override
        public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> result) {
            Logs.i(TAG, "loadMoreData-onSuccess-feedListLoadResult-hasMore=" + result.hasMore + ",size=" + result.dataList.size());

            FragmentActivity activity = getActivity();
            if (activity == null) {
                isLoading = false;
                return;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isLoading = false;
                    int requestedPage = currentPage;
                    List<ShortPlay> finalList = result.dataList;
                    boolean useUnfilteredFallback = false;
                    hasMore = result.hasMore;
                    if (result.hasMore) {
                        currentPage++;
                    }

                    if (finalList != null && !finalList.isEmpty()) {
                        continuousFilteredEmptyPages = 0;
                        if (requestedPage == 1) {
                            progressStateMap.clear();
                            feedListAdapter.setData(finalList);
                            currentSelectedPosition = 0;
                            refreshBottomActionsForPosition(0);
                            refreshBottomProgressForPosition(0);
                            if (recommendViewPager != null) {
                                recommendViewPager.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        startRecommendPlayback();
                                    }
                                });
                            }
                        } else {
                            feedListAdapter.appendData(finalList);
                        }
                    } else if (result.hasMore && !useUnfilteredFallback) {
                        continuousFilteredEmptyPages++;
                        if (continuousFilteredEmptyPages <= MAX_CONTINUOUS_FILTERED_EMPTY_PAGES) {
                            loadMoreData();
                        } else {
                            Logs.i(TAG, "loadMoreData-skip too many empty filtered pages, stop auto continue");
                        }
                    }
                }
            });
        }

        @Override
        public void onFail(PSSDK.ErrorInfo errorInfo) {
            Logs.i(TAG, "loadMoreData-onFail-errorInfo=" + errorInfo);
            isLoading = false;
            // 澶勭悊閿欒鎻愮ず
        }
    };

    private void refreshBottomActionsForPosition(int position) {
        if (feedListAdapter == null) {
            return;
        }
        ShortPlay shortPlay = feedListAdapter.getShortPlayByPosition(position);
        if (shortPlay == null) {
            if (recommendBottomActionsView != null) {
                recommendBottomActionsView.setVisibility(View.GONE);
            }
            currentVisibleShortPlay = null;
            return;
        }
        currentVisibleShortPlay = shortPlay;
        currentVisibleEpisodeIndex = 1;
        if (recommendBottomActionsView != null) {
            recommendBottomActionsView.setVisibility(View.VISIBLE);
        }
        updateBottomChooseTitle(shortPlay, currentVisibleEpisodeIndex);
    }

    private void refreshBottomProgressForPosition(int position) {
        if (recommendProgressBar == null) {
            return;
        }
        ProgressState state = progressStateMap.get(position);
        boolean shouldShow = state != null && state.hasVideoFrame;
        int durationInSeconds = state == null ? 0 : state.durationInSeconds;
        int progressInSeconds = state == null ? 0 : state.progressInSeconds;
        int safeMax = Math.max(durationInSeconds, 1);
        if (recommendProgressBar.getMax() != safeMax) {
            recommendProgressBar.setMax(safeMax);
        }
        recommendProgressBar.setProgress(Math.max(0, Math.min(progressInSeconds, safeMax)));
        recommendProgressBar.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
    }

    private void onBottomProgressChanged(int position, int progressInSeconds, int durationInSeconds) {
        ProgressState state = progressStateMap.get(position);
        if (state == null) {
            state = new ProgressState();
            progressStateMap.put(position, state);
        }
        state.durationInSeconds = Math.max(durationInSeconds, 0);
        state.progressInSeconds = Math.max(progressInSeconds, 0);
        if (state.durationInSeconds > 0) {
            state.hasVideoFrame = true;
        }
        if (position != currentSelectedPosition || recommendProgressBar == null || isBottomProgressTracking) {
            return;
        }
        int safeMax = Math.max(state.durationInSeconds, 1);
        if (recommendProgressBar.getMax() != safeMax) {
            recommendProgressBar.setMax(safeMax);
        }
        recommendProgressBar.setProgress(Math.max(0, Math.min(state.progressInSeconds, safeMax)));
        recommendProgressBar.setVisibility(state.hasVideoFrame ? View.VISIBLE : View.GONE);
    }

    private void markCurrentProgressWaitingForFrame() {
        if (recommendProgressBar == null) {
            return;
        }
        ProgressState state = progressStateMap.get(currentSelectedPosition);
        if (state == null) {
            state = new ProgressState();
            progressStateMap.put(currentSelectedPosition, state);
        }
        state.hasVideoFrame = false;
        state.progressInSeconds = 0;
        state.durationInSeconds = 0;
        recommendProgressBar.setProgress(0);
        recommendProgressBar.setVisibility(View.GONE);
    }

    private void onCurrentEpisodeChanged(int position, @NonNull ShortPlay shortPlay, int index) {
        if (position != currentSelectedPosition) {
            return;
        }
        currentVisibleShortPlay = shortPlay;
        currentVisibleEpisodeIndex = index <= 0 ? 1 : index;
        updateBottomChooseTitle(shortPlay, currentVisibleEpisodeIndex);
        markCurrentProgressWaitingForFrame();
    }

    private void updateBottomChooseTitle(@NonNull ShortPlay shortPlay, int index) {
        if (recommendChooseIndexTitleTV == null) {
            return;
        }
        int currentEpisode = index <= 0 ? 1 : index;
        int totalEpisodes = shortPlay.total > 0 ? shortPlay.total : (shortPlay.episodes == null ? 1 : shortPlay.episodes.size());
        if (totalEpisodes <= 0) {
            totalEpisodes = 1;
        }
        if (currentEpisode > totalEpisodes) {
            currentEpisode = totalEpisodes;
        }
        String progressLabel = shortPlay.progressState == ShortPlay.PROGRESS_STATE_END ? "已完结" : "连载中";
        recommendChooseIndexTitleTV.setText("第" + currentEpisode + "集 · 全" + totalEpisodes + "集（" + progressLabel + "）");
    }

    private void showChooseIndexDialog() {
        if (!isAdded() || currentVisibleShortPlay == null) {
            return;
        }
        FragmentActivity activity = getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        dismissChooseIndexDialog();
        chooseIndexDialog = new IndexChooseDialog(activity, currentVisibleShortPlay, currentVisibleEpisodeIndex, new IIndexChooseListener() {
            @Override
            public void onChooseIndex(int index) {
                currentVisibleEpisodeIndex = index <= 0 ? 1 : index;
                if (currentVisibleShortPlay != null) {
                    updateBottomChooseTitle(currentVisibleShortPlay, currentVisibleEpisodeIndex);
                }
                if (feedListAdapter == null) {
                    return;
                }
                ShortPlayFragment fragment = feedListAdapter.getFragmentByPosition(currentSelectedPosition);
                if (fragment != null) {
                    markCurrentProgressWaitingForFrame();
                    fragment.startPlayIndex(currentVisibleEpisodeIndex);
                }
            }
        });
        chooseIndexDialog.show();
    }

    private void dismissChooseIndexDialog() {
        if (chooseIndexDialog != null && chooseIndexDialog.isShowing()) {
            chooseIndexDialog.dismiss();
        }
        chooseIndexDialog = null;
    }

    private static class ProgressState {
        int progressInSeconds;
        int durationInSeconds;
        boolean hasVideoFrame;
    }

    private interface ResolutionChangeListener {
        void onResolutionChanged(String resolution);
    }

    private static class CustomOverlayView extends FrameLayout implements PSSDK.IControlView, DramaPlayActivity.ProgressChangeListener, ResolutionChangeListener {

        private static final float MAX_VIDEO_SPEED = 3.0f;
        private static final float[] PLAY_SPEEDS = new float[]{1.0f, 1.5f, 2.0f};
        private static final String[] PLAY_SPEED_LABELS = new String[]{"1.0X", "1.5X", "2.0X"};
        private static final String DEFAULT_RESOLUTION_TEXT = "360P";
        private static final int MENU_TYPE_NONE = 0;
        private static final int MENU_TYPE_SPEED = 1;
        private static final int MENU_TYPE_RESOLUTION = 2;

//        private final TextView chooseIndexTitleTV;
        private final TextView dramaTitleTV;
//        private final TextView dramaDescTV;
        private final ImageView dramaCoverIV;
//        private final TextView playAllTV;
        private final TextView speedTV;
        private final TextView resolutionTV;
        private final View speedResolutionMenuLayout;
        private final LinearLayout speedMenuPanel;
        private final LinearLayout resolutionMenuPanel;
        private final SeekBar progressBar;

        private final int speedResolutionNormalTextColor = Color.parseColor("#CCFFFFFF");
        private final int speedResolutionActiveTextColor = Color.parseColor("#FFF84E40");

        private ShortPlayFragment shortPlayFragment;
        private ShortPlay shortPlay;
        private int currentPlaySpeedIndex = 0;
        private Resolution[] resolutions;
        private Resolution currentResolution;
        private int currentExpandedMenuType = MENU_TYPE_NONE;
        private int speedButtonDefaultTextColor;
        private int resolutionButtonDefaultTextColor;

        public CustomOverlayView(Context context) {
            super(context);
            inflate(context, R.layout.player_overlay_for_stream, this);
//            View oldBottomActions = findViewById(R.id.ll_bottom_actions);
//            oldBottomActions.setVisibility(View.GONE);

//            chooseIndexTitleTV = findViewById(R.id.tv_overlay_choose_index_title);
//            findViewById(R.id.ll_choose_index).setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    DramaPlayActivity.start(v.getContext(), shortPlay);
//                }
//            });
//            playAllTV = findViewById(R.id.tv_play_all);
//            playAllTV.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    DramaPlayActivity.start(v.getContext(), shortPlay);
//                }
//            });

            dramaTitleTV = findViewById(R.id.tv_overlay_drama_name);
//            dramaDescTV = findViewById(R.id.tv_overlay_drama_desc);
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
            applyTopButtonOffset();
            speedResolutionMenuLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setExpandedMenuType(MENU_TYPE_NONE);
                }
            });

            progressBar = findViewById(R.id.sb_overlay);
            progressBar.setVisibility(View.GONE);
            progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (shortPlayFragment != null) {
                        shortPlayFragment.setCurrentPlayTimeSeconds(seekBar.getProgress());
                    }
                }
            });

            refreshSpeedMenuItems();
            refreshResolutionMenuItems();
        }

        private void applyTopButtonOffset() {
            int buttonTopMargin = DpUtils.dp2px(getContext(), 16);
            int menuTopMargin = DpUtils.dp2px(getContext(), 50);

            FrameLayout.LayoutParams speedParams = (FrameLayout.LayoutParams) speedTV.getLayoutParams();
            speedParams.topMargin = buttonTopMargin;
            speedTV.setLayoutParams(speedParams);

            FrameLayout.LayoutParams resolutionParams = (FrameLayout.LayoutParams) resolutionTV.getLayoutParams();
            resolutionParams.topMargin = buttonTopMargin;
            resolutionTV.setLayoutParams(resolutionParams);

            FrameLayout.LayoutParams menuParams = (FrameLayout.LayoutParams) speedResolutionMenuLayout.getLayoutParams();
            menuParams.topMargin = menuTopMargin;
            speedResolutionMenuLayout.setLayoutParams(menuParams);
        }

        @Override
        public PSSDK.ControlViewType getControlViewType() {
            return PSSDK.ControlViewType.CUSTOM;
        }

        @Override
        public void bindItemData(ShortPlayFragment shortPlayFragment, ShortPlay shortPlay, int index) {
            this.shortPlayFragment = shortPlayFragment;
            this.shortPlay = shortPlay;
//            chooseIndexTitleTV.setText(buildChooseIndexTitle(shortPlay, index));
            dramaTitleTV.setText(shortPlay.title);
//            dramaDescTV.setText(shortPlay.desc);
            Glide.with(getContext())
                    .load(shortPlay.coverImage)
                    .into(dramaCoverIV);
            speedTV.setText(getCurrentPlaySpeedLabel());
            resolutionTV.setText(getResolutionButtonText(currentResolution));
            refreshResolutionMenuItems();
            refreshSpeedMenuItems();
            setExpandedMenuType(MENU_TYPE_NONE);
            applyCurrentPlaySpeed();
        }

        private String buildChooseIndexTitle(@NonNull ShortPlay shortPlay, int index) {
            int currentEpisode = index <= 0 ? 1 : index;
            int totalEpisodes = shortPlay.total > 0 ? shortPlay.total : 1;
            if (currentEpisode > totalEpisodes) {
                currentEpisode = totalEpisodes;
            }
            String progressLabel = shortPlay.progressState == ShortPlay.PROGRESS_STATE_END ? "已完结" : "连载中";
            return "第" + currentEpisode + "集 · 全" + totalEpisodes + "集（" + progressLabel + "）";
        }

        @Override
        public void onProgressChanged(int progress, int max) {
            if (progressBar.getMax() != max) {
                progressBar.setMax(max);
            }
            progressBar.setProgress(progress);
        }

        @Override
        public void onResolutionChanged(String resolution) {
            resolutionTV.setText(getResolutionButtonText(currentResolution));
            refreshResolutionMenuItems();
        }

        void setResolutionData(@Nullable Resolution[] resolutions, @Nullable Resolution currentResolution) {
            this.resolutions = resolutions;
            this.currentResolution = resolveEffectiveResolution(currentResolution);
            onResolutionChanged(getResolutionLabel(this.currentResolution));
        }

        private float getCurrentPlaySpeed() {
            if (currentPlaySpeedIndex < 0 || currentPlaySpeedIndex >= PLAY_SPEEDS.length) {
                currentPlaySpeedIndex = 0;
            }
            return PLAY_SPEEDS[currentPlaySpeedIndex];
        }

        private String getCurrentPlaySpeedLabel() {
            if (currentPlaySpeedIndex < 0 || currentPlaySpeedIndex >= PLAY_SPEEDS.length) {
                currentPlaySpeedIndex = 0;
            }
            return PLAY_SPEED_LABELS[currentPlaySpeedIndex];
        }

        private void applyCurrentPlaySpeed() {
            float speed = getCurrentPlaySpeed();
            if (speed <= 0f) {
                return;
            }
            if (speed > MAX_VIDEO_SPEED) {
                speed = MAX_VIDEO_SPEED;
            }
            if (shortPlayFragment != null) {
                shortPlayFragment.setVideoSpeed(speed);
            }
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

        private void toggleSpeedResolutionMenu(int menuType, @NonNull View anchorView) {
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
                TextView itemView = createMenuItemView(PLAY_SPEED_LABELS[i], i == currentPlaySpeedIndex);
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
                        if (shortPlayFragment != null) {
                            shortPlayFragment.setResolution(selectedResolution);
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

    private class FeedListAdapter extends FragmentStateAdapter implements DefaultLifecycleObserver {

        private final List<ShortPlay> playList = new ArrayList<>();
        @NonNull
        private final Fragment containerFragment;
        private final HashMap<Integer, ShortPlayFragment> fragmentMap = new HashMap<>();

        public FeedListAdapter(@NonNull Fragment fragment) {
            super(fragment);
            this.containerFragment = fragment;
        }

        public @Nullable ShortPlayFragment getFragmentByPosition(int position) {
            return fragmentMap.get(position);
        }

        public @Nullable ShortPlay getShortPlayByPosition(int position) {
            if (position < 0 || position >= playList.size()) {
                return null;
            }
            return playList.get(position);
        }

        @Override
        public void onDestroy(@NonNull LifecycleOwner owner) {
            DefaultLifecycleObserver.super.onDestroy(owner);

            for (Map.Entry<Integer, ShortPlayFragment> entry : fragmentMap.entrySet()) {
                if (entry.getValue() == owner) {
                    fragmentMap.remove(entry.getKey());
                    break;
                }
            }
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            ShortPlay shortPlay = playList.get(position);
            PSSDK.DetailPageConfig.Builder builder = new PSSDK.DetailPageConfig.Builder();
            builder.hideLeftTopCloseAndTitle(true, null)
                    .displayBottomExtraView(false)
                    .displayProgressBar(false)
                    .displayTextVisibility(PSSDK.DetailPageConfig.TEXT_POS_BOTTOM_TITLE, false)
                    .displayTextVisibility(PSSDK.DetailPageConfig.TEXT_POS_BOTTOM_DESC, false)
                    // Fill the screen width first, keep aspect ratio to avoid vertical stretching.
                    .setVideoDisplayMode(TTVideoEngineInterface.IMAGE_LAYOUT_ASPECT_FILL_X)
                    .playSingleItem(true);
            ShortPlayFragment detailFragment = PSSDK.createDetailFragment(shortPlay, builder.build(), new PSSDK.ShortPlayDetailPageListener() {
                private DramaPlayActivity.ProgressChangeListener progressChangeListener;
                private ResolutionChangeListener resolutionChangeListener;
                private Resolution[] resolutions;
                private Resolution currentResolution;
                @Override
                public void onOverScroll(int direction) {


                }

                @Override
                public void onProgressChange(ShortPlay shortPlay, int index, int currentPlayTime, int duration) {
                    if (progressChangeListener != null) {
                        progressChangeListener.onProgressChanged(currentPlayTime, duration);
                    }
                    RecommendFragment.this.onBottomProgressChanged(position, currentPlayTime, duration);
                }

                @Override
                public boolean onPlayFailed(PSSDK.ErrorInfo errorInfo) {
                    return false;
                }

                @Override
                public void onShortPlayPlayed(ShortPlay shortPlay, int index, EpisodeData episodeData) {
                    Log.d(TAG, "onShortPlayPlayed() called with: shortPlay = [" + shortPlay + "], index = [" + index + "]");
                    RecommendFragment.this.onCurrentEpisodeChanged(position, shortPlay, index);
                }

                @Override
                public void onItemSelected(int position, ItemType type, int index) {

                }

                @Override
                public void onVideoPlayStateChanged(ShortPlay shortPlay, int index, int playbackState) {

                }

                @Override
                public void onVideoPlayCompleted(ShortPlay shortPlay, int index) {

                }

                @Override
                public void onEnterImmersiveMode() {
                    // no-op: keep required SDK callback implemented without immersive UI logic
                }

                @Override
                public void onExitImmersiveMode() {
                    // no-op: keep required SDK callback implemented without immersive UI logic
                }

                @Override
                public boolean isNeedBlock(ShortPlay shortPlay, int index) {
                    return false;
                }

                @Override
                public void showAdIfNeed(ShortPlay shortPlay, int index, PSSDK.ShortPlayBlockResultListener listener) {

                }

                @Override
                public void onVideoInfoFetched(ShortPlay shortPlay, int index, PSSDK.VideoPlayInfo videoPlayInfo) {
                    resolutions = videoPlayInfo.supportResolutions;
                    currentResolution = videoPlayInfo.currentResolution;
                    if (currentResolution == null && resolutions != null) {
                        for (Resolution resolution : resolutions) {
                            if (resolution != null) {
                                currentResolution = resolution;
                                break;
                            }
                        }
                    }
                    if (resolutionChangeListener != null) {
                        if (resolutionChangeListener instanceof CustomOverlayView) {
                            ((CustomOverlayView) resolutionChangeListener).setResolutionData(resolutions, currentResolution);
                        }
                        resolutionChangeListener.onResolutionChanged(currentResolution == null ? "" : currentResolution.toString());
                    }
                }

                @Override
                public List<View> onObtainPlayerControlViews() {
                    ArrayList<View> views = new ArrayList<>();

                    // 鍒嗕韩鎸夐挳
                    FragmentActivity activity = containerFragment.getActivity();
                    DramaPlayActivity.CustomShareView shareView = new DramaPlayActivity.CustomShareView(activity);
                    views.add(shareView);
                    shareView.setImageResource(R.drawable.share);
                    FrameLayout.LayoutParams shareLP = new FrameLayout.LayoutParams(DpUtils.dp2px(activity, 32), DpUtils.dp2px(activity, 32));
                    shareLP.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                    shareLP.rightMargin = DpUtils.dp2px(activity, 16);
                    shareLP.bottomMargin = DpUtils.dp2px(activity, 280);
                    shareView.setLayoutParams(shareLP);
                    shareView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_SUBJECT, shortPlay.title);
                            intent.putExtra(Intent.EXTRA_TEXT, shortPlay.desc);
                            activity.startActivity(Intent.createChooser(intent, "鍒嗕韩鐭墽"));
                        }
                    });

                    // 鐐硅禐鎸夐挳
                    DramaPlayActivity.CustomLikeView customLikeView = new DramaPlayActivity.CustomLikeView(activity);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                    params.rightMargin = DpUtils.dp2px(activity, 16);
                    params.bottomMargin = DpUtils.dp2px(activity, 200);
                    customLikeView.setLayoutParams(params);
                    views.add(customLikeView);

                    // 鏀惰棌鎸夐挳
                    DramaPlayActivity.CustomCollectView collectView = new DramaPlayActivity.CustomCollectView(activity);
                    params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                    params.rightMargin = DpUtils.dp2px(activity, 16);
                    params.bottomMargin = DpUtils.dp2px(activity, 130);
                    collectView.setLayoutParams(params);
                    views.add(collectView);

                    CustomOverlayView customOverlayView = new CustomOverlayView(activity);
                    params=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                    params.bottomMargin = DpUtils.dp2px(activity, 20);
                    customOverlayView.setLayoutParams(params);
                    views.add(customOverlayView);
                    progressChangeListener = customOverlayView;
                    resolutionChangeListener = customOverlayView;
                    customOverlayView.setResolutionData(resolutions, currentResolution);

                    return views;
                }
            });
            if (detailFragment != null) {
                detailFragment.getLifecycle().addObserver(this);
                fragmentMap.put(position, detailFragment);
            }
            return detailFragment;
        }

        @Override
        public int getItemCount() {
            return playList.size();
        }

        public void setData(List<ShortPlay> fragments) {
            this.playList.clear();
            this.playList.addAll(fragments);
            // ViewPager2 full refresh: replacing entire list with range-insert may cause holder position inconsistency.
            fragmentMap.clear();
            notifyDataSetChanged();
        }

        public void appendData(List<ShortPlay> fragments) {
            int size = this.playList.size();
            this.playList.addAll(fragments);
            notifyItemRangeInserted(size, fragments.size());
        }

        public void pauseAllPlayback() {
            for (ShortPlayFragment fragment : new ArrayList<>(fragmentMap.values())) {
                if (fragment != null) {
                    fragment.pausePlay();
                }
            }
        }

        public void stopAllPlayback() {
            for (ShortPlayFragment fragment : new ArrayList<>(fragmentMap.values())) {
                if (fragment != null) {
                    fragment.stopPlay();
                }
            }
        }

        public void destroy() {
            stopAllPlayback();
            fragmentMap.clear();
        }
    }



}





