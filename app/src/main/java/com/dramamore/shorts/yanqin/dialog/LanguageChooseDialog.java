package com.dramamore.shorts.yanqin.dialog;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.anythink.banner.api.ATBannerListener;
import com.anythink.banner.api.ATBannerView;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.AdError;
import com.bytedance.sdk.shortplay.api.PSSDK;
import com.dramamore.shorts.yanqin.App;
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.utils.ContentLanguageHelper;
import com.dramamore.shorts.yanqin.utils.DpUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LanguageChooseDialog extends DialogFragment {
    private static final String TAG = "LanguageChooseDialog";
    private static final LinkedHashMap<String, String> languageDisplayNames = new LinkedHashMap<>();

    public void initLangStr(){
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
    private ContentLanguageChangeListener languageChangeListener;
    private FrameLayout adContainer;
    private ATBannerView topOnBannerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog);
        initLangStr();

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_config, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(attributes);

        // 👇 核心：内容延伸到状态栏
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridLayout gridLayout = view.findViewById(R.id.gl_choices);

        String currentSetLanguage = ContentLanguageHelper.getSelectedContentLanguage();
        for (Map.Entry<String, String> entry : languageDisplayNames.entrySet()) {
            CheckBox checkBox = new CheckBox(view.getContext());
            checkBox.setText(entry.getValue());
            checkBox.setTag(entry.getKey());
            checkBox.setTextColor(Color.WHITE);
            checkBox.setButtonDrawable(null);
            checkBox.setBackground(getResources().getDrawable(R.drawable.bg_lang_round));
            checkBox.setPadding(20, 30, 20, 30);
            checkBox.setChecked(entry.getKey().equals(currentSetLanguage));
            checkBox.setOnClickListener(v -> selectLanguage(checkBox));
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            // 3. 关键：设置 LayoutParams 确保平分宽度
            // columnSpec 参数 1: 位置(UNDEFINED 自动排)；参数 2: 权重(1f 代表平分)
            GridLayout.Spec rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
            GridLayout.Spec colSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);

            // 必须将宽度设为 0，权重才会生效
            params.width = 0;
            params.height = DpUtils.dp2px(getContext(), 50); // 或者 WRAP_CONTENT
            params.setMargins(10, 10, 10, 10); // 设置间距

            gridLayout.addView(checkBox, params);
            checkBoxes.add(checkBox);
        }

        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> checkedData = new ArrayList<>();
                checkedData.add(getCheckedLanguage());
                if (languageChangeListener != null) {
                    languageChangeListener.onContentLanguageChanged(checkedData);
                }
                dismiss();
            }
        });

        initTopOnBanner(view);
    }

    private void initTopOnBanner(@NonNull View rootView) {
        adContainer = rootView.findViewById(R.id.ad_container);
        if (adContainer == null) {
            return;
        }
        if (TextUtils.isEmpty(App.BANNERAD_ID)) {
            adContainer.setVisibility(View.GONE);
            Log.w(TAG, "skip TopOn banner: BANNERAD_ID is empty");
            return;
        }
        String topOnAppKey = App.getTopOnAppKey(requireContext());
        if (TextUtils.isEmpty(App.TOPON_APP_ID) || TextUtils.isEmpty(topOnAppKey)) {
            adContainer.setVisibility(View.GONE);
            Log.w(TAG, "skip TopOn banner: TOPON_APP_ID or TOPON_APP_KEY is empty. Please set TOPON_APP_KEY in AndroidManifest.");
            return;
        }
        if (getActivity() == null) {
            adContainer.setVisibility(View.GONE);
            return;
        }

        topOnBannerView = new ATBannerView(requireActivity());
        topOnBannerView.setPlacementId(App.BANNERAD_ID);
        Map<String, Object> localExtra = new HashMap<>();
        localExtra.put(ATAdConst.KEY.AD_WIDTH, 320);
        localExtra.put(ATAdConst.KEY.AD_HEIGHT, 50);
        topOnBannerView.setLocalExtra(localExtra);
        topOnBannerView.setBannerAdListener(new ATBannerListener() {
            @Override
            public void onBannerLoaded() {
                if (adContainer != null) {
                    adContainer.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "TopOn banner loaded");
            }

            @Override
            public void onBannerFailed(AdError adError) {
                if (adContainer != null) {
                    adContainer.setVisibility(View.GONE);
                }
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
        adContainer.setVisibility(View.VISIBLE);
        topOnBannerView.loadAd();
        Log.d(TAG, "TopOn banner request started, placementId=" + App.BANNERAD_ID);
    }

    private void selectLanguage(@NonNull CheckBox selectedCheckBox) {
        for (CheckBox checkBox : checkBoxes) {
            checkBox.setChecked(checkBox == selectedCheckBox);
        }
    }

    @NonNull
    private String getCheckedLanguage() {
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                return (String) checkBox.getTag();
            }
        }
        return ContentLanguageHelper.DEFAULT_LANGUAGE;
    }

    public void setLanguageChangeListener(ContentLanguageChangeListener languageChangeListener) {
        this.languageChangeListener = languageChangeListener;
    }

    @Override
    public void onDestroyView() {
        if (topOnBannerView != null) {
            topOnBannerView.destroy();
            topOnBannerView = null;
        }
        if (adContainer != null) {
            adContainer.removeAllViews();
            adContainer = null;
        }
        super.onDestroyView();
    }

    public interface ContentLanguageChangeListener {
        void onContentLanguageChanged(List<String> languages);
    }

    public static class LanguageData implements Checkable {
        public final String displayName;
        public String name;
        private boolean checked;

        public LanguageData(String displayName, String name) {
            this.displayName = displayName;
            this.name = name;
        }

        @Override
        public boolean isChecked() {
            return checked;
        }

        @Override
        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        @Override
        public void toggle() {
            checked = !checked;
        }

        @Override
        public String toString() {
            return displayName + '/' + name;
        }
    }
}
