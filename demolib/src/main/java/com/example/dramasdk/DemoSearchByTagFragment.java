package com.example.dramasdk;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.List;

public class DemoSearchByTagFragment extends AppFragment implements LanguageChooseDialog.ContentLanguageChangeListener {

    private static final String TAG = "DemoSearchByTagFragment";
    private EditText tagIdET;
    private EditText pageIndexET;
    private EditText pageCountET;
    private DramaListAdapter adapter;
    private TextView languageTV;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_by_tag, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tagIdET = view.findViewById(R.id.et_tag_id);
        pageIndexET = view.findViewById(R.id.et_page_index);
        pageCountET = view.findViewById(R.id.et_page_count);

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

        long tagId = 0;
        try {
            tagId = Long.parseLong(tagIdET.getText().toString());
        } catch (NumberFormatException e) {
        }

        PSSDK.requestDramaByTag(tagId, pageIndex, pageCount, new PSSDK.FeedListResultListener() {
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
        languageTV.setText("当前内容语言: " + languages + ", 点击切换");
        PSSDK.setContentLanguages(languages);

        adapter.clearData();
        requestSearch();
    }
}
