package com.dramamore.shorts.yanqin.utils;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dramamore.shorts.yanqin.R;

public class FragmentUtils {

    public static boolean switchFragment(Activity activity, Fragment target) {
        FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        // 或者通过 findFragmentByTag 查找，但最稳妥是遍历已添加的
        for (Fragment fragment : fm.getFragments()) {
            if (fragment != null && fragment.isVisible()) {
                transaction.hide(fragment);
            }
        }

        // 2. 处理目标 Fragment
        String tag = target.getClass().getSimpleName();
        Fragment existingFragment = fm.findFragmentByTag(tag);

        if (existingFragment == null) {
            // 没添加过：add
            transaction.add(R.id.fragment_container, target, tag);
        } else {
            // 已添加过：直接 show
            transaction.show(existingFragment);
        }
        Logs.i("FragmentUtils", "switchFragment-tag="+tag);
        transaction.commitAllowingStateLoss(); // 避免在 Activity 状态保存后提交导致的崩溃
        return true;
    }

}
