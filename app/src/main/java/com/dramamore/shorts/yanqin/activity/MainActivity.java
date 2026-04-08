package com.dramamore.shorts.yanqin.activity;

import android.Manifest;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.dramamore.shorts.yanqin.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.dramamore.shorts.yanqin.fragment.FollowFragment;
import com.dramamore.shorts.yanqin.fragment.HomeFragment;
import com.dramamore.shorts.yanqin.fragment.MineFragment;
import com.dramamore.shorts.yanqin.fragment.RecommendFragment;
import com.dramamore.shorts.yanqin.utils.FragmentUtils;
import com.dramamore.shorts.yanqin.utils.Logs;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="MainActivity";
    private Fragment homeFragment, recommendFragment, followFragment, profileFragment;
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->
                    Logs.i(TAG, "notification permission result=" + isGranted));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setOnApplyWindowInsetsListener(null);
        navView.setPadding(0, 0, 0, 0);

        requestNotificationPermissionIfNeeded();
        initFragment();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Logs.i(TAG, "requestNotificationPermissionIfNeeded-skip: Android 12 and below do not require runtime notification permission");
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            Logs.i(TAG, "requestNotificationPermissionIfNeeded-skip: already granted");
            return;
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private void initFragment() {
        homeFragment = new HomeFragment();
        recommendFragment = null;
        followFragment = null;
        profileFragment = null;

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setItemIconTintList(null);

        FragmentUtils.switchFragment(this,homeFragment != null ? homeFragment : new HomeFragment());

        navView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            Logs.i(TAG,"setOnItemSelectedListener-itemId="+itemId);
            if (itemId == R.id.nav_home) {
                fragment = homeFragment != null ? homeFragment : new HomeFragment();
            } else if (itemId == R.id.nav_recommend) {
                if (recommendFragment == null) {
                    recommendFragment = new RecommendFragment();
                }
                fragment = recommendFragment;
            } else if (itemId == R.id.nav_follow) {
                if (followFragment == null) {
                    followFragment = new FollowFragment();
                }
                fragment = followFragment;
            } else if (itemId == R.id.nav_profile) {
                if (profileFragment == null) {
                    profileFragment = new MineFragment();
                }
                fragment = profileFragment;
            }

            if (itemId != R.id.nav_recommend && recommendFragment instanceof RecommendFragment) {
                ((RecommendFragment) recommendFragment).forcePausePlayback();
            }
            return FragmentUtils.switchFragment(MainActivity.this,fragment);
        });
    }

    /*private boolean switchFragment(Fragment target) {
        if (target == null || target == currentFragment) return false;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (currentFragment != null) {
            transaction.hide(currentFragment); // 隐藏当前的
        }

        if (!target.isAdded()) {
            // 如果没添加过，则添加并指定 Tag
            transaction.add(R.id.fragment_container, target).commit();
        } else {
            // 如果添加过了，直接显示
            transaction.show(target).commit();
        }

        currentFragment = target; // 更新当前引用
        return true;
    }*/

}
