package com.example.dramasdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.bytedance.sdk.shortplay.api.PSSDK;

import java.util.ArrayList;
import java.util.List;

public class MyTabFragment extends AbsTabFragment implements LanguageChooseDialog.ContentLanguageChangeListener {

    private final List<String> oldLanguages = new ArrayList<>();

    @Override
    public String getTitle(Context context) {
        return "My";
    }

    @Override
    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(R.drawable.tab_my_selector);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView sdkVersionView = view.findViewById(R.id.tv_sdk_version);
        sdkVersionView.setText(PSSDK.getVersion());

        view.findViewById(R.id.tv_set_language).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfigDialog();
            }
        });

        view.findViewById(R.id.tv_watch_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), WatchHistoryActivity.class));
            }
        });
        view.findViewById(R.id.tv_api_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ApiTestActivity.class));
            }
        });
    }

    private void showConfigDialog() {
        List<String> contentLanguages = PSSDK.getContentLanguages();
        if (contentLanguages != null) {
            oldLanguages.addAll(contentLanguages);
        }
        LanguageChooseDialog configDialog = new LanguageChooseDialog();
        configDialog.setLanguageChangeListener(this);
        configDialog.show(getChildFragmentManager(), "config_dialog");
    }

    @Override
    public void onContentLanguageChanged(List<String> languages) {
        PSSDK.setContentLanguages(languages);
        if (!oldLanguages.equals(languages)) {
            FragmentActivity activity = getActivity();
            Intent intent = activity.getIntent();
            activity.finish();
            startActivity(intent);
        }
    }
}
