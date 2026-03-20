package com.dramamore.shorts.yanqin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dramamore.shorts.yanqin.R;

public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 建议创建对应的 layout 文件：fragment_home.xml
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        return view;
    }
}

