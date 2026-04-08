package com.dramamore.shorts.yanqin.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.dramamore.shorts.yanqin.R;

public class RateUsDialog extends DialogFragment {
    private static final int DEFAULT_RATING = 3;

    private final ImageView[] starViews = new ImageView[5];
    private final TextView[] labelViews = new TextView[5];
    private final View[] starGroupViews = new View[5];
    private int currentRating = DEFAULT_RATING;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_rate_us, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() == null || getDialog().getWindow() == null) {
            return;
        }
        Window window = getDialog().getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.v_rate_dialog_dismiss).setOnClickListener(v -> dismissAllowingStateLoss());
        view.findViewById(R.id.iv_rate_close).setOnClickListener(v -> dismissAllowingStateLoss());

        starViews[0] = view.findViewById(R.id.iv_star_1);
        starViews[1] = view.findViewById(R.id.iv_star_2);
        starViews[2] = view.findViewById(R.id.iv_star_3);
        starViews[3] = view.findViewById(R.id.iv_star_4);
        starViews[4] = view.findViewById(R.id.iv_star_5);
        starGroupViews[0] = view.findViewById(R.id.ll_star_group_1);
        starGroupViews[1] = view.findViewById(R.id.ll_star_group_2);
        starGroupViews[2] = view.findViewById(R.id.ll_star_group_3);
        starGroupViews[3] = view.findViewById(R.id.ll_star_group_4);
        starGroupViews[4] = view.findViewById(R.id.ll_star_group_5);

        labelViews[0] = view.findViewById(R.id.tv_rate_label_1);
        labelViews[1] = view.findViewById(R.id.tv_rate_label_2);
        labelViews[2] = view.findViewById(R.id.tv_rate_label_3);
        labelViews[3] = view.findViewById(R.id.tv_rate_label_4);
        labelViews[4] = view.findViewById(R.id.tv_rate_label_5);

        for (int i = 0; i < starGroupViews.length; i++) {
            final int rating = i + 1;
            starGroupViews[i].setOnClickListener(v -> updateRating(rating));
        }

        updateRating(DEFAULT_RATING);

        EditText feedbackInput = view.findViewById(R.id.et_rate_feedback);
        view.findViewById(R.id.btn_rate_submit).setOnClickListener(v -> {
            if (!isAdded()) {
                return;
            }
            String feedback = feedbackInput.getText() == null ? "" : feedbackInput.getText().toString().trim();
            if (currentRating <= 0 && feedback.isEmpty()) {
                Toast.makeText(requireContext(), R.string.s_rate_feedback_hint, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(requireContext(), R.string.s_rate_submit_success, Toast.LENGTH_SHORT).show();
            dismissAllowingStateLoss();
        });
    }

    private void updateRating(int rating) {
        currentRating = rating;
        for (int i = 0; i < starViews.length; i++) {
            boolean selected = i < rating;
            starViews[i].setImageResource(selected ? R.drawable.collect_2 : R.drawable.collect_1);
            labelViews[i].setTextColor(selected ? 0xFFFF684D : 0xFFFFFFFF);
        }
    }
}
