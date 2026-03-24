package com.dramamore.shorts.yanqin.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.utils.DpUtils;
import com.ss.ttvideoengine.Resolution;

public class ChooseResolutionDialogActivity extends AppCompatActivity {

    private static final String EXTRA_CURRENT_RESOLUTION = "current_resolution";
    private static final String EXTRA_SUPPORT_RESOLUTION_LIST = "support_resolution_list";
    private static final String EXTRA_CHOSEN_RESOLUTION = "chosen_resolution";

    public static void start(Activity activity, int requestCode, Resolution[] supportList, Resolution currentResolution) {
        Intent intent = new Intent(activity, ChooseResolutionDialogActivity.class);
        intent.putExtra(EXTRA_SUPPORT_RESOLUTION_LIST, supportList);
        intent.putExtra(EXTRA_CURRENT_RESOLUTION, currentResolution);
        activity.startActivityForResult(intent, requestCode);
    }

    public static @Nullable Resolution getChosenResolution(Intent data) {
        if (data == null) {
            return null;
        }
        return (Resolution) data.getSerializableExtra(EXTRA_CHOSEN_RESOLUTION);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 建议在 onCreate 顶部调用
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
       // 设置状态栏颜色为透明
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        Resolution[] supportList = (Resolution[]) intent.getSerializableExtra(EXTRA_SUPPORT_RESOLUTION_LIST);
        Resolution current = (Resolution) intent.getSerializableExtra(EXTRA_CURRENT_RESOLUTION);
        if (supportList == null || current == null || supportList.length == 0) {
            finish();
            return;
        }

        setContentView(R.layout.activity_choose_resolution_dialog);
        setFinishOnTouchOutside(true);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.resolution), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayout container = findViewById(R.id.ll_container);
        int verticalPadding = DpUtils.dp2px(this, 16);
        for (final Resolution resolution : supportList) {
            TextView item = new TextView(this);
            item.setText(resolution.toString());
            item.setTextColor(Color.WHITE);
            item.setGravity(Gravity.CENTER);
            item.setTextSize(16);
            item.setPadding(0, verticalPadding, 0, verticalPadding);
            if (resolution != current) {
                item.setAlpha(0.5f);
            } else {
                item.setTypeface(null, Typeface.BOLD);
            }
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (resolution != current) {
                        Intent data = new Intent();
                        data.putExtra(EXTRA_CHOSEN_RESOLUTION, resolution);
                        setResult(RESULT_OK, data);
                    }
                    finish();
                }
            });
            container.addView(item);
        }

        getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
    }
}