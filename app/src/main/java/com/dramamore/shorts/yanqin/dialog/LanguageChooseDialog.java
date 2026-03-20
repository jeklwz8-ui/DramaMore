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
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bytedance.sdk.shortplay.api.PSSDK;
import com.dramamore.shorts.yanqin.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageChooseDialog extends DialogFragment {
    private static final HashMap<String, String> languageDisplayNames = new HashMap<>();

    static {
        languageDisplayNames.put("zh_hans", "简体中文");
        languageDisplayNames.put("zh_hant", "繁体中文");
        languageDisplayNames.put("en", "英语");
        languageDisplayNames.put("vi", "越南语");
        languageDisplayNames.put("in", "印尼语");
        languageDisplayNames.put("th", "泰语");
        languageDisplayNames.put("ja", "日语");
        languageDisplayNames.put("ko", "韩语");
        languageDisplayNames.put("pt", "葡萄牙语");
        languageDisplayNames.put("ar", "阿拉伯语");
    }

    private final List<CheckBox> checkBoxes = new ArrayList<>();
    private ContentLanguageChangeListener languageChangeListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_config, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(attributes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout languageList = view.findViewById(R.id.ll_choices);

        List<String> currentSetLanguage = PSSDK.getContentLanguages();
        for (Map.Entry<String, String> entry : languageDisplayNames.entrySet()) {
            CheckBox checkBox = new CheckBox(view.getContext());
            checkBox.setText(entry.getKey() + "/" + entry.getValue());
            checkBox.setTag(entry.getKey());
            checkBox.setTextColor(Color.BLACK);
            checkBox.setPadding(0, 30, 0, 30);
            if (currentSetLanguage != null) {
                checkBox.setChecked(currentSetLanguage.contains(entry.getKey()));
            }
            languageList.addView(checkBox, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            checkBoxes.add(checkBox);
        }

        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> checkedData = new ArrayList<>();
                for (CheckBox checkBox : checkBoxes) {
                    if (checkBox.isChecked()) {
                        checkedData.add((String) checkBox.getTag());
                    }
                }
                if (languageChangeListener != null) {
                    languageChangeListener.onContentLanguageChanged(checkedData);
                }
                dismiss();
            }
        });
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
