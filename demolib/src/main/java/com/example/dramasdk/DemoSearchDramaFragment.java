package com.example.dramasdk;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DemoSearchDramaFragment extends AppFragment implements LanguageChooseDialog.ContentLanguageChangeListener {

    private static final String TAG = "DemoSearchDramaFragment";
    private EditText keywordET;
    private EditText pageIndexET;
    private EditText pageCountET;
    private CheckBox isFuzzyCB;
    private DramaListAdapter adapter;
    private TextView languageTV;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        keywordET = view.findViewById(R.id.et_keyword);
        pageIndexET = view.findViewById(R.id.et_page_index);
        pageCountET = view.findViewById(R.id.et_page_count);
        isFuzzyCB = view.findViewById(R.id.cb_fuzzy);

        languageTV = view.findViewById(R.id.tv_choose_language);
        languageTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfigDialog();
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.rlv);
        GridLayoutManager layoutManager = new GridLayoutManager(view.getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new DramaListAdapter(new DramaListAdapter.OnItemClickListener() {
            @Override
            public void onClickItem(int index) {
                ShortPlay shortPlay = adapter.getDataItem(index);
                FragmentActivity activity = getActivity();
                if (activity == null) {
                    return;
                }
                DramaPlayActivity.start(activity, shortPlay);
            }
        });
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btn_do_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSearch();
            }
        });
    }

    private void showConfigDialog() {
        LanguageChooseDialog configDialog = new LanguageChooseDialog();
        configDialog.setLanguageChangeListener(this);
        configDialog.show(getChildFragmentManager(), "config_dialog");
    }

    private void requestSearch() {

        int pageIndex = 0;
        try {
            pageIndex = Integer.parseInt(pageIndexET.getText().toString());
        } catch (NumberFormatException e) {
        }
        int pageCount = 0;
        try {
            pageCount = Integer.parseInt(pageCountET.getText().toString());
        } catch (NumberFormatException e) {
        }

        PSSDK.searchDrama(keywordET.getText().toString(), isFuzzyCB.isChecked(), pageIndex, pageCount, new PSSDK.FeedListResultListener() {
            @Override
            public void onFail(PSSDK.ErrorInfo errorInfo) {
                Log.d(TAG, "onFail() called with: errorInfo = [" + errorInfo + "]");
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.clearData();

                            Toast.makeText(activity, "Search error, " + errorInfo.msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay> result) {
                Log.d(TAG, "onSuccess() called with: result = [" + result + "]");

                FragmentActivity activity = getActivity();
                if (activity == null) {
                    return;
                }

                // 短剧列表
                List<ShortPlay> dataList = result.dataList;
                // 分页请求，是否还有更多数据
                boolean hasMore = result.hasMore;

                Collections.sort(dataList, new Comparator<ShortPlay>() {
                    @Override
                    public int compare(ShortPlay o1, ShortPlay o2) {
                        return Long.compare(o1.id, o2.id);
                    }
                });
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clearData();
                        adapter.addData(result.dataList);
                    }
                });
            }
        });
    }

    @Override
    public void onContentLanguageChanged(List<String> languages) {
        languageTV.setText("当前指定语言: " + languages + ", 点击切换");
        PSSDK.setContentLanguages(languages);

        adapter.clearData();
        requestSearch();
    }
}
