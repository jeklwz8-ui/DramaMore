package com.example.dramasdk;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.example.dramasdk.feedlist.HomeTabFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class MainActivity extends AppFragmentActivity implements PSSDK.FeedListResultListener {


    private static final String TAG = "MainActivity";
    private boolean showRecommend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act_main);

        ViewPager2 viewPager2 = findViewById(R.id.main_vp);
        TabLayout tabLayout = findViewById(R.id.main_tab_layout);
        MainViewPagerAdapter pagerAdapter = new MainViewPagerAdapter(this);
        viewPager2.setAdapter(pagerAdapter);
        viewPager2.setUserInputEnabled(false);


        new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                TextView view = new TextView(MainActivity.this);
                view.setText(pagerAdapter.getItem(position).getTitle(MainActivity.this));
                view.setTextSize(12);
                view.setTextColor(Color.parseColor("#5A5A5A"));
                view.setGravity(Gravity.CENTER_HORIZONTAL);
                view.setCompoundDrawablesWithIntrinsicBounds(null, pagerAdapter.getItem(position).getIcon(MainActivity.this), null, null);
                tab.setCustomView(view);
            }
        }).attach();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ((TextView) tab.getCustomView()).setTypeface(null, Typeface.BOLD);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                ((TextView) tab.getCustomView()).setTypeface(null, Typeface.NORMAL);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!showRecommend) {
            showRecommend = true;

            PSSDK.requestPopularDrama(1, 1, this);
        }
    }

    @Override
    public void onFail(PSSDK.ErrorInfo errorInfo) {
        Log.d(TAG, "onFail() called with: errorInfo = [" + errorInfo + "]");
    }

    @Override
    public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> result) {
        Log.d(TAG, "onSuccess() called with: result = [" + result + "]");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()){
                    return;
                }
                showRecommendDialog(result.dataList);
            }
        });
    }

    private void showRecommendDialog(List<ShortPlay> dataList) {
        RecommendDialog dialog = new RecommendDialog(dataList, new IOnClickListener() {
            @Override
            public void onClickOK(ShortPlay shortPlay) {
                DramaPlayActivity.start(MainActivity.this, shortPlay);
            }
        });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(dialog, "RecommendDialog");
        transaction.commitAllowingStateLoss(); // 允许状态丢失的提交
    }

    private interface IOnClickListener {
        void onClickOK(ShortPlay shortPlay);
    }

    private static class MainViewPagerAdapter extends FragmentStateAdapter {

        private final AbsTabFragment[] fragments = new AbsTabFragment[]{
                new DemoShortPlayStreamFragment(),
                new HomeTabFragment(),
                new MyTabFragment(),
        };

        public MainViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments[position];
        }

        @Override
        public int getItemCount() {
            return fragments.length;
        }

        public AbsTabFragment getItem(int position) {
            return fragments[position];
        }
    }

    public static final class RecommendDialog extends DialogFragment {

        private final List<ShortPlay> dataList;
        private final IOnClickListener onClickListener;
        private final CountDownTimer timer;
        private TextView okButton;

        public RecommendDialog(List<ShortPlay> dataList, IOnClickListener onClickListener) {
            this.dataList = dataList;
            this.onClickListener = onClickListener;
            timer = new CountDownTimer(4000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    okButton.setText("Continue to watch(" + millisUntilFinished / 1000 + "s)");
                }

                @Override
                public void onFinish() {
                    dismiss();
                }
            };
        }

        @Override
        public void onResume() {
            super.onResume();
            Window window = getDialog().getWindow();
            window.setBackgroundDrawable(null);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

            timer.start();
        }

        @Override
        public void onPause() {
            super.onPause();
            timer.cancel();
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            super.onDismiss(dialog);

            timer.cancel();
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.dialog_main_recommend, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            view.findViewById(R.id.tv_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            if (!dataList.isEmpty()) {
                ShortPlay shortPlay = dataList.get(0);

                ImageView coverIV = view.findViewById(R.id.iv_cover);
                Glide.with(view.getContext()).load(shortPlay.coverImage).into(coverIV);


                TextView titleView = view.findViewById(R.id.tv_title);
                titleView.setText(shortPlay.title);

                TextView descView = view.findViewById(R.id.tv_desc);
                descView.setText(shortPlay.total + " Episodes / Free");

                okButton = view.findViewById(R.id.tv_go_to_watch);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        if (onClickListener != null) {
                            onClickListener.onClickOK(shortPlay);
                        }
                    }
                });
            }
        }
    }
}
