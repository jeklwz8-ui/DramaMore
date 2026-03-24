package com.dramamore.shorts.yanqin.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bytedance.sdk.shortplay.api.PSSDK;
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.dialog.LanguageChooseDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageActivity extends AppCompatActivity {
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
    private LanguageChooseDialog.ContentLanguageChangeListener languageChangeListener;

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
    }

    private void initView() {
        LinearLayout languageList = findViewById(R.id.ll_choices);

        List<String> currentSetLanguage = PSSDK.getContentLanguages();
        for (Map.Entry<String, String> entry : languageDisplayNames.entrySet()) {
            CheckBox checkBox = new CheckBox(this);
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

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
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
                finish();
            }
        });
    }
}