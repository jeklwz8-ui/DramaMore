package com.dramamore.shorts.yanqin.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.dramamore.shorts.yanqin.BuildConfig;
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.activity.DramaPlayActivity;
import com.dramamore.shorts.yanqin.activity.HistoryActivity;
import com.dramamore.shorts.yanqin.activity.WebViewActivity;
import com.dramamore.shorts.yanqin.dao.HistoryDao;
import com.dramamore.shorts.yanqin.database.HistoryDatabase;
import com.dramamore.shorts.yanqin.dialog.LanguageChooseDialog;
import com.dramamore.shorts.yanqin.dialog.RateUsDialog;
import com.dramamore.shorts.yanqin.entity.HistoryDaoEntity;
import com.dramamore.shorts.yanqin.utils.Logs;
import com.dramamore.shorts.yanqin.utils.ShortUtils;
import com.dramamore.shorts.yanqin.widget.RoundImageView;

import java.util.List;

public class MineFragment extends Fragment implements LanguageChooseDialog.ContentLanguageChangeListener {
    private static final String TAG = "MineFragment";

    private HistoryDao historyDao;
    private LinearLayout llHis;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        HistoryDatabase historyDatabase = HistoryDatabase.getDatabase(getActivity());
        historyDao = historyDatabase.historyDao();
        initView(view);
        return view;
    }

    private void initView(View view) {
        view.findViewById(R.id.fl_history).setOnClickListener(v -> HistoryActivity.start(getContext()));
        llHis = view.findViewById(R.id.ll_his);
        view.findViewById(R.id.fl_lang).setOnClickListener(v -> {
            LanguageChooseDialog configDialog = new LanguageChooseDialog();
            configDialog.setLanguageChangeListener(MineFragment.this);
            configDialog.show(getChildFragmentManager(), "config_dialog");
        });
        view.findViewById(R.id.fl_rate).setOnClickListener(v -> showRateUsDialog());
        view.findViewById(R.id.fl_share).setOnClickListener(v -> shareApp());
        view.findViewById(R.id.fl_protocol).setOnClickListener(v -> WebViewActivity.start(getContext()));
        view.findViewById(R.id.fl_clear).setOnClickListener(v -> {
            PSSDK.clearLocalCache();
            Toast.makeText(getActivity(), getString(R.string.s_clear_success), Toast.LENGTH_SHORT).show();
        });
        TextView tvVersion = view.findViewById(R.id.tv_version);
        tvVersion.setText(BuildConfig.VERSION_NAME);
    }

    private void showRateUsDialog() {
        if (!isAdded()) {
            return;
        }
        if (getChildFragmentManager().findFragmentByTag("rate_us_dialog") != null) {
            return;
        }
        RateUsDialog dialog = new RateUsDialog();
        dialog.show(getChildFragmentManager(), "rate_us_dialog");
    }

    private void shareApp() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        String packageName = context.getPackageName();
        String shareContent = getString(R.string.s_share_content, packageName);
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, shareContent);
            startActivity(Intent.createChooser(intent, getString(R.string.s_share_title)));
        } catch (Exception e) {
            Logs.i(TAG, "shareApp error: " + e.getMessage());
            Toast.makeText(context, getString(R.string.s_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
        loadCache();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadHistory();
            loadCache();
        }
    }

    @Override
    public void onContentLanguageChanged(List<String> languages) {
        Logs.i(TAG, "onContentLanguageChanged: " + languages);
        PSSDK.setContentLanguages(languages);

        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        Intent intent = activity.getIntent();
        activity.finish();
        startActivity(intent);
    }

    private void loadCache() {
    }

    private void loadHistory() {
        HistoryDatabase.executor.execute(() -> {
            List<HistoryDaoEntity> newData = historyDao.getPagedHistories(3, 0);
            if (!newData.isEmpty()) {
                updateUI(newData);
            }
        });
    }

    private void updateUI(List<HistoryDaoEntity> newData) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(() -> {
            Logs.i(TAG, "updateUI-size=" + newData.size());
            for (int i = 0; i < newData.size(); i++) {
                if (i >= 3) {
                    break;
                }
                HistoryDaoEntity historyDaoEntity = newData.get(i);
                ShortPlay shortPlay = ShortUtils.jsonToShortPlay(historyDaoEntity.short_json);
                View childAt = llHis.getChildAt(i);
                childAt.setOnClickListener(v -> DramaPlayActivity.start(getActivity(), shortPlay));
                RoundImageView imageView = childAt.findViewById(R.id.ic_cover);
                imageView.setRadius(10);
                Glide.with(activity)
                        .load(shortPlay.coverImage)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView);
                TextView tvHotValue = childAt.findViewById(R.id.tv_hot_value);
                tvHotValue.setText(ShortUtils.convertToK(shortPlay.totalCollectCount));
                TextView tvEpisode = childAt.findViewById(R.id.tv_episode);
                tvEpisode.setText(shortPlay.total + getString(R.string.s_eps));
                TextView tvName = childAt.findViewById(R.id.tv_name);
                tvName.setText(shortPlay.title);
                TextView tvSawNum = childAt.findViewById(R.id.tv_saw_num);
                Context context = getContext();
                if (context != null) {
                    tvSawNum.setText(context.getString(R.string.s_saw_num) + historyDaoEntity.play_index + context.getString(R.string.s_eps));
                }
            }
        });
    }
}
