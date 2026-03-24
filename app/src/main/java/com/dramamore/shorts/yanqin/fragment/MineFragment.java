package com.dramamore.shorts.yanqin.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import com.dramamore.shorts.yanqin.database.FollowDatabase;
import com.dramamore.shorts.yanqin.database.HistoryDatabase;
import com.dramamore.shorts.yanqin.dialog.LanguageChooseDialog;
import com.dramamore.shorts.yanqin.entity.FollowDaoEntity;
import com.dramamore.shorts.yanqin.entity.HistoryDaoEntity;
import com.dramamore.shorts.yanqin.utils.AppUtils;
import com.dramamore.shorts.yanqin.utils.FileUtils;
import com.dramamore.shorts.yanqin.utils.Logs;
import com.dramamore.shorts.yanqin.utils.ShortUtils;
import com.dramamore.shorts.yanqin.widget.RoundImageView;

import java.util.List;

public class MineFragment extends Fragment implements LanguageChooseDialog.ContentLanguageChangeListener {
    HistoryDao historyDao;
    private LinearLayout llHis;
    private static final String TAG = "MineFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 建议创建对应的 layout 文件：fragment_home.xml
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        HistoryDatabase historyDatabase = HistoryDatabase.getDatabase(getActivity());
        historyDao = historyDatabase.historyDao();

        initView(view);
        return view;
    }

    private void initView(View view) {
        view.findViewById(R.id.fl_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryActivity.start(getContext());
            }
        });
        llHis = view.findViewById(R.id.ll_his);
        view.findViewById(R.id.fl_lang).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LanguageChooseDialog configDialog = new LanguageChooseDialog();
                configDialog.setLanguageChangeListener(MineFragment.this);
                configDialog.show(getChildFragmentManager(), "config_dialog");
            }
        });
        view.findViewById(R.id.fl_protocol).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewActivity.start(getContext());
            }
        });
        view.findViewById(R.id.fl_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 清除本地缓存
                 */
                PSSDK.clearLocalCache();
                Toast.makeText(getActivity(), getString(R.string.s_clear_success), Toast.LENGTH_SHORT).show();
            }
        });
        TextView tvVersion = view.findViewById(R.id.tv_version);
        tvVersion.setText(BuildConfig.VERSION_NAME);
    }

    public void openUrlInExternalBrowser(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            // 标记为外部浏览器打开，避免唤起其他应用
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 检查是否有可用的浏览器应用
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Logs.i(TAG, "应用跳转失败-url=" + url);
            }
        } catch (Exception e) {
            Logs.i(TAG, "应用跳转异常：" + e.getMessage());
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
        Intent intent = activity.getIntent();
        activity.finish();
        startActivity(intent);
    }

    private void loadCache() {

    }

    private void loadHistory() {
        HistoryDatabase.executor.execute(new Runnable() {
            @Override
            public void run() {
                // 1. 获取当前页数据
                List<HistoryDaoEntity> newData = historyDao.getPagedHistories(3, 0);
                if (!newData.isEmpty()) {
                    // 3. 更新偏移量，为下一页做准备
                    // 4. 将数据回调给 UI 层 (比如通过 LiveData 或 Handler)
                    updateUI(newData);
                }
            }
        });
    }

    private void updateUI(List<HistoryDaoEntity> newData) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Logs.i(TAG, "updateUI-size=" + newData.size());
                for (int i = 0; i < newData.size(); i++) {
                    if (i >= 3) break;
                    HistoryDaoEntity historyDaoEntity = newData.get(i);
                    ShortPlay shortPlay = ShortUtils.jsonToShortPlay(historyDaoEntity.short_json);
                    View childAt = llHis.getChildAt(i);
                    childAt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DramaPlayActivity.start(getActivity(), shortPlay);
                        }
                    });
                    RoundImageView imageView = childAt.findViewById(R.id.ic_cover);
                    imageView.setRadius(10);
                    Glide.with(getActivity()).load(shortPlay.coverImage).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
                    TextView tvHotValue = childAt.findViewById(R.id.tv_hot_value);
                    tvHotValue.setText(ShortUtils.convertToK(shortPlay.totalCollectCount));
                    TextView tvEpisode = childAt.findViewById(R.id.tv_episode);
                    tvEpisode.setText(shortPlay.total + getString(R.string.s_eps));
                    TextView tvName = childAt.findViewById(R.id.tv_name);
                    tvName.setText(shortPlay.title);
                    TextView tvSawNum = childAt.findViewById(R.id.tv_saw_num);
                    tvSawNum.setText(getContext().getString(R.string.s_saw_num) + historyDaoEntity.play_index + getContext().getString(R.string.s_eps));
                }
            }
        });

    }

}

