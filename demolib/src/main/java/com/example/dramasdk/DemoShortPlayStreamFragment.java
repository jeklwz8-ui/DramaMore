package com.example.dramasdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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

import com.bytedance.sdk.shortplay.api.EpisodeData;
import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.bytedance.sdk.shortplay.api.ShortPlayFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 滑动切换剧集，直接播放
 */
public class DemoShortPlayStreamFragment extends AbsTabFragment {

    private static final String TAG = "ShortPlayStreamFragment";
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private FeedListAdapter feedListAdapter;

    public DemoShortPlayStreamFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shortplay_stream, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager2 vp2 = view.findViewById(R.id.vp_shortplay_feed);
        vp2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        feedListAdapter = new FeedListAdapter(this);
        vp2.setAdapter(feedListAdapter);
        vp2.setOffscreenPageLimit(1);
        vp2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                int nextPos = position + 1;
                ShortPlayFragment playFragment = feedListAdapter.getFragmentByPosition(nextPos);
                Log.d(TAG, "预加载下一部剧：pos=" + nextPos + ", " + playFragment);
                if (playFragment != null) {
                    playFragment.preLoadVideo(new PSSDK.ActionResultListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "onSuccess: 预加载成功");
                        }

                        @Override
                        public void onFail(PSSDK.ErrorInfo errorInfo) {
                            Log.d(TAG, "onSuccess: 预加载失败");
                        }
                    });
                }
            }
        });

        PSSDK.requestFeedList(1, 20, new PSSDK.FeedListResultListener() {
            @Override
            public void onFail(PSSDK.ErrorInfo errorInfo) {
                Log.d(TAG, "onFail() called with: errorInfo = [" + errorInfo + "]");
            }

            @Override
            public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> result) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setData(result);
                    }
                });
            }
        });
    }

    private void setData(PSSDK.FeedListLoadResult<ShortPlay> result) {
        feedListAdapter.appendData(result.dataList);
    }

    @Override
    public String getTitle(Context context) {
        return "Home";
    }

    @Override
    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(R.drawable.tab_home_selector);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        feedListAdapter.destroy();
    }

    private static class FeedListAdapter extends FragmentStateAdapter implements DefaultLifecycleObserver {

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
                    .playSingleItem(true);
            ShortPlayFragment detailFragment = PSSDK.createDetailFragment(shortPlay, builder.build(), new PSSDK.ShortPlayDetailPageListener() {
                private DramaPlayActivity.ProgressChangeListener progressChangeListener;

                @Override
                public void onOverScroll(int direction) {


                }

                @Override
                public void onProgressChange(ShortPlay shortPlay, int index, int currentPlayTime, int duration) {
                    if (progressChangeListener != null) {
                        progressChangeListener.onProgressChanged(currentPlayTime, duration);
                    }
                }

                @Override
                public boolean onPlayFailed(PSSDK.ErrorInfo errorInfo) {
                    return false;
                }

                @Override
                public void onShortPlayPlayed(ShortPlay shortPlay, int index, EpisodeData episodeData) {
                    Log.d(TAG, "onShortPlayPlayed() called with: shortPlay = [" + shortPlay + "], index = [" + index + "]");
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

                }

                @Override
                public void onExitImmersiveMode() {

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

                }

                @Override
                public List<View> onObtainPlayerControlViews() {
                    ArrayList<View> views = new ArrayList<>();

                    // 分享按钮
                    FragmentActivity activity = containerFragment.getActivity();

                    DramaPlayActivity.CustomShareView shareView = new DramaPlayActivity.CustomShareView(activity);
                    views.add(shareView);
                    shareView.setImageResource(R.drawable.share);
                    FrameLayout.LayoutParams shareLP = new FrameLayout.LayoutParams(DemoUtils.dp2Px(activity, 32), DemoUtils.dp2Px(activity, 32));
                    shareLP.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                    shareLP.rightMargin = DemoUtils.dp2Px(activity, 16);
                    shareLP.bottomMargin = DemoUtils.dp2Px(activity, 196);
                    shareView.setLayoutParams(shareLP);
                    shareView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_SUBJECT, shortPlay.title);
                            intent.putExtra(Intent.EXTRA_TEXT, shortPlay.desc);
                            activity.startActivity(Intent.createChooser(intent, "分享短剧"));
                        }
                    });

                    // 点赞按钮
                    DramaPlayActivity.CustomLikeView customLikeView = new DramaPlayActivity.CustomLikeView(activity);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                    params.rightMargin = DemoUtils.dp2Px(activity, 16);
                    params.bottomMargin = DemoUtils.dp2Px(activity, 122);
                    customLikeView.setLayoutParams(params);
                    views.add(customLikeView);

                    // 收藏按钮
                    DramaPlayActivity.CustomCollectView collectView = new DramaPlayActivity.CustomCollectView(activity);
                    params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                    params.rightMargin = DemoUtils.dp2Px(activity, 16);
                    params.bottomMargin = DemoUtils.dp2Px(activity, 60);
                    collectView.setLayoutParams(params);
                    views.add(collectView);

                    CustomOverlayView customOverlayView = new CustomOverlayView(activity);
                    customOverlayView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                    views.add(customOverlayView);

                    progressChangeListener = customOverlayView;
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

        public void appendData(List<ShortPlay> fragments) {
            int size = this.playList.size();
            this.playList.addAll(fragments);
            notifyItemRangeInserted(size, fragments.size());
        }

        public void destroy() {
            fragmentMap.clear();
        }
    }

    private static class CustomOverlayView extends FrameLayout implements PSSDK.IControlView, DramaPlayActivity.ProgressChangeListener {

        private final TextView chooseIndexTitleTV;
        private final TextView dramaTitleTV;
        private final TextView dramaDescTV;
        private final SeekBar progressBar;
        private ShortPlayFragment shortPlayFragment;
        private ShortPlay shortPlay;

        public CustomOverlayView(Context context) {
            super(context);
            inflate(context, R.layout.player_overlay_for_stream, this);

            chooseIndexTitleTV = findViewById(R.id.tv_overlay_choose_index_title);

            findViewById(R.id.ll_choose_index).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    DramaPlayActivity.start(v.getContext(), shortPlay);
                }
            });

            dramaTitleTV = findViewById(R.id.tv_overlay_drama_name);
            dramaDescTV = findViewById(R.id.tv_overlay_drama_desc);

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
                    if (shortPlayFragment != null) {
                        shortPlayFragment.setCurrentPlayTimeSeconds(seekBar.getProgress());
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
            this.shortPlayFragment = shortPlayFragment;
            this.shortPlay = shortPlay;
            chooseIndexTitleTV.setText(shortPlay.total + "集 - " + shortPlay.title);
            dramaTitleTV.setText(shortPlay.title);
            dramaDescTV.setText(shortPlay.desc);
        }

        @Override
        public void onProgressChanged(int progress, int max) {
            if (progressBar.getMax() != max) {
                progressBar.setMax(max);
            }
            progressBar.setProgress(progress);
        }
    }

}
