package com.dramamore.shorts.yanqin.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.anythink.banner.api.ATBannerListener;
import com.anythink.banner.api.ATBannerView;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.AdError;
import com.dramamore.shorts.yanqin.App;
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.dialog.LanguageChooseDialog;
import com.dramamore.shorts.yanqin.utils.ContentLanguageHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LanguageActivity extends AppCompatActivity {
    private static final String TAG = "LanguageActivity";
    private static final LinkedHashMap<String, String> languageDisplayNames = new LinkedHashMap<>();

    static {
        languageDisplayNames.put("zh_hans", "简体中文");
        languageDisplayNames.put("zh_hant", "繁體中文");
        languageDisplayNames.put("en", "English");
        languageDisplayNames.put("vi", "Tiếng Việt");
        languageDisplayNames.put("in", "Bahasa Indonesia");
        languageDisplayNames.put("th", "ไทย");
        languageDisplayNames.put("ja", "日本語");
        languageDisplayNames.put("ko", "한국어");
        languageDisplayNames.put("pt", "Português");
    }

    private final List<CheckBox> checkBoxes = new ArrayList<>();
    private LanguageChooseDialog.ContentLanguageChangeListener languageChangeListener;
    private FrameLayout adContainer;
    private ATBannerView topOnBannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.language), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initView();
        initTopOnBanner();
    }

    private void initView() {
        LinearLayout languageList = findViewById(R.id.ll_choices);

        String currentSetLanguage = ContentLanguageHelper.getSelectedContentLanguage();
        for (Map.Entry<String, String> entry : languageDisplayNames.entrySet()) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(entry.getValue());
            checkBox.setTag(entry.getKey());
            checkBox.setTextColor(Color.BLACK);
            checkBox.setPadding(0, 30, 0, 30);
            checkBox.setChecked(entry.getKey().equals(currentSetLanguage));
            checkBox.setOnClickListener(v -> selectLanguage(checkBox));
            languageList.addView(checkBox, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            checkBoxes.add(checkBox);
        }

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> checkedData = new ArrayList<>();
                checkedData.add(getCheckedLanguage());
                if (languageChangeListener != null) {
                    languageChangeListener.onContentLanguageChanged(checkedData);
                } else {
                    ContentLanguageHelper.setSelectedContentLanguages(checkedData);
                }
                finish();
            }
        });
    }

    private void selectLanguage(CheckBox selectedCheckBox) {
        for (CheckBox checkBox : checkBoxes) {
            checkBox.setChecked(checkBox == selectedCheckBox);
        }
    }

    private String getCheckedLanguage() {
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                return (String) checkBox.getTag();
            }
        }
        return ContentLanguageHelper.DEFAULT_LANGUAGE;
    }

    private void initTopOnBanner() {
        adContainer = findViewById(R.id.ad_container);
        if (adContainer == null) {
            return;
        }
        if (TextUtils.isEmpty(App.BANNERAD_ID)) {
            adContainer.setVisibility(View.GONE);
            Log.w(TAG, "skip TopOn banner: BANNERAD_ID is empty");
            return;
        }
        // TopOn init in App.java requires both TOPON_APP_ID and TOPON_APP_KEY.
        String topOnAppKey = App.getTopOnAppKey(this);
        if (TextUtils.isEmpty(App.TOPON_APP_ID) || TextUtils.isEmpty(topOnAppKey)) {
            adContainer.setVisibility(View.GONE);
            Log.w(TAG, "skip TopOn banner: TOPON_APP_ID or TOPON_APP_KEY is empty. Please set TOPON_APP_KEY in AndroidManifest.");
            return;
        }

        topOnBannerView = new ATBannerView(this);
        topOnBannerView.setPlacementId(App.BANNERAD_ID);
        topOnBannerView.setBannerAdListener(new ATBannerListener() {
            @Override
            public void onBannerLoaded() {
                adContainer.setVisibility(View.VISIBLE);
                Log.d(TAG, "TopOn banner loaded");
            }

            @Override
            public void onBannerFailed(AdError adError) {
                adContainer.setVisibility(View.GONE);
                Log.w(TAG, "TopOn banner load failed: " + (adError == null ? "unknown" : adError.getFullErrorInfo()));
            }

            @Override
            public void onBannerClicked(ATAdInfo atAdInfo) {
                Log.d(TAG, "TopOn banner clicked");
            }

            @Override
            public void onBannerShow(ATAdInfo atAdInfo) {
                Log.d(TAG, "TopOn banner shown");
            }

            @Override
            public void onBannerClose(ATAdInfo atAdInfo) {
                Log.d(TAG, "TopOn banner closed");
            }

            @Override
            public void onBannerAutoRefreshed(ATAdInfo atAdInfo) {
                Log.d(TAG, "TopOn banner auto refreshed");
            }

            @Override
            public void onBannerAutoRefreshFail(AdError adError) {
                Log.w(TAG, "TopOn banner auto refresh failed: " + (adError == null ? "unknown" : adError.getFullErrorInfo()));
            }
        });
        adContainer.removeAllViews();
        adContainer.addView(topOnBannerView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));
        adContainer.setVisibility(View.INVISIBLE);
        topOnBannerView.loadAd();
    }

    @Override
    protected void onDestroy() {
        if (topOnBannerView != null) {
            topOnBannerView.destroy();
            topOnBannerView = null;
        }
        if (adContainer != null) {
            adContainer.removeAllViews();
        }
        super.onDestroy();
    }
}
