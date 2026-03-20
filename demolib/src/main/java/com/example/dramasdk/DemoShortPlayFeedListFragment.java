package com.example.dramasdk;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.sdk.shortplay.api.PSSDK;
import com.example.dramasdk.feedlist.AbsFeedListFragment;
import com.example.dramasdk.feedlist.HotListFragment;
import com.example.dramasdk.feedlist.LatestListFragment;
import com.example.dramasdk.feedlist.NormalListFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

/**
 * 短剧列表，点击后进入播放页
 */
public class DemoShortPlayFeedListFragment extends AppFragment implements LanguageChooseDialog.ContentLanguageChangeListener {

    private static final String TAG = "PSSDK.FeedListDemo";
    private final AbsFeedListFragment[] listFragments = new AbsFeedListFragment[]{new NormalListFragment(), new HotListFragment(), new LatestListFragment()};
    private Button configButton;
    private FragmentStateAdapter pageListAdapter;
    private ViewPager2 viewPager2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shortplay_feed_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        configButton = view.findViewById(R.id.btn_config);
        configButton.setText("当前内容语言: " + PSSDK.getContentLanguages() + ", 点击切换");
        configButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfigDialog();
            }
        });

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        viewPager2 = view.findViewById(R.id.vp2);

        pageListAdapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return listFragments[position];
            }

            @Override
            public int getItemCount() {
                return listFragments.length;
            }
        };
        viewPager2.setAdapter(pageListAdapter);

        new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(listFragments[position].getTitle());
            }
        }).attach();
    }

    private void showConfigDialog() {
        LanguageChooseDialog configDialog = new LanguageChooseDialog();
        configDialog.setLanguageChangeListener(this);
        configDialog.show(getChildFragmentManager(), "config_dialog");
    }

    @Override
    public void onContentLanguageChanged(List<String> languages) {
        Log.d(TAG, "onContentLanguageChanged() called with: languages = [" + languages + "]");

        configButton.setText("当前指定语言: " + languages + ", 点击切换");
        PSSDK.setContentLanguages(languages);

        pageListAdapter.notifyDataSetChanged();

        for (int i = 0; i < listFragments.length; i++) {
            AbsFeedListFragment listFragment = listFragments[i];
            listFragment.clearData(viewPager2.getCurrentItem() == i);

        }
    }
}
