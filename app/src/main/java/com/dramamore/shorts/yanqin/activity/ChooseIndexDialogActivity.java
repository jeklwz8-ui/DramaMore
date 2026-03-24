package com.dramamore.shorts.yanqin.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bytedance.sdk.shortplay.api.ShortPlay;
import com.dramamore.shorts.yanqin.R;
import com.dramamore.shorts.yanqin.dialog.IndexChooseDialog;
import com.dramamore.shorts.yanqin.listener.IIndexChooseListener;

public class ChooseIndexDialogActivity extends AppCompatActivity implements IIndexChooseListener {
    private static final String EXTRA_SHORT_PLAY = "shot_play";
    private static final String EXTRA_PLAYING_INDEX = "playing_index";
    private static final String EXTRA_CHOOSE_INDEX = "choose_index";

    public static void start(Activity activity, ShortPlay shortPlay, int playingIndex, int requestCode) {
        Intent intent = new Intent(activity, ChooseIndexDialogActivity.class);
        intent.putExtra(EXTRA_SHORT_PLAY, shortPlay);
        intent.putExtra(EXTRA_PLAYING_INDEX, playingIndex);
        activity.startActivityForResult(intent, requestCode);
    }

    public static int getChooseIndex(Intent data) {
        return data.getIntExtra(EXTRA_CHOOSE_INDEX, -1);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 建议在 onCreate 顶部调用
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        // 设置状态栏颜色为透明
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        Intent intent = getIntent();
        if (intent != null) {
            ShortPlay shortPlay = intent.getParcelableExtra(EXTRA_SHORT_PLAY);
            int playingIndex = intent.getIntExtra(EXTRA_PLAYING_INDEX, 1);

            IndexChooseDialog dialog = new IndexChooseDialog(this, shortPlay, playingIndex, this);
            dialog.show();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });

            setFinishOnTouchOutside(true);
        } else {
            finish();
        }
    }

    @Override
    public void onChooseIndex(int index) {
        Intent data = new Intent();
        data.putExtra(EXTRA_CHOOSE_INDEX, index);
        setResult(RESULT_OK, data);
        finish();
    }
}
