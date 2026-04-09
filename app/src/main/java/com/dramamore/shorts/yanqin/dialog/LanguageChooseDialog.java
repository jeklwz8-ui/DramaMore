package com.dramamore.shorts.yanqin.dialog;

import android.graphics.Color;
import android.os.Bundle;
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

import com.bytedance.sdk.shortplay.api.PSSDK;
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.utils.ContentLanguageHelper;
import com.dramamore.shorts.yanqin.utils.DpUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LanguageChooseDialog extends DialogFragment {
    private static final LinkedHashMap<String, String> languageDisplayNames = new LinkedHashMap<>();

    public void initLangStr(){
        languageDisplayNames.put("zh_hans", getString(R.string.s_zh_hans));
        languageDisplayNames.put("zh_hant", getString(R.string.s_zh_hant));
        languageDisplayNames.put("en", getString(R.string.s_en));
        languageDisplayNames.put("vi", getString(R.string.s_vi));
        languageDisplayNames.put("in", getString(R.string.s_in));
        languageDisplayNames.put("th", getString(R.string.s_th));
        languageDisplayNames.put("ja", getString(R.string.s_ja));
        languageDisplayNames.put("ko", getString(R.string.s_ko));
        languageDisplayNames.put("pt", getString(R.string.s_pt));;
    }

    private final List<CheckBox> checkBoxes = new ArrayList<>();
    private ContentLanguageChangeListener languageChangeListener;

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
