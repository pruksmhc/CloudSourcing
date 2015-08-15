// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;


public class LeaderBoardActivity extends InternetHandlingActivity {
    ViewPager viewPager;
    LeaderBoardPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.global_board_tab));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.friend_board_tab));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new LeaderBoardPagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(mAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setBackgroundResource(R.drawable.gameactivity_background_small2);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected View getRootView() {
        return findViewById(R.id.leaderboard_fragment_container);
    }

    @Override
    protected ArrayList<InternetHandlingFragment> getHostedFrags() {
        ArrayList<InternetHandlingFragment> mInternetHandlingFragments = new ArrayList<>();
        InternetHandlingFragment frag1 = (InternetHandlingFragment) mAdapter.getRegisteredFragment(0);
        InternetHandlingFragment frag2 = (InternetHandlingFragment) mAdapter.getRegisteredFragment(1);
        mInternetHandlingFragments.add(frag1);
        mInternetHandlingFragments.add(frag2);
        return mInternetHandlingFragments;

    }


    private class LeaderBoardPagerAdapter extends DynamicFragmentStatePagerAdapter {
        int mNumOfTabs;

        public LeaderBoardPagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    GlobalBoardFragment gTab = GlobalBoardFragment.newInstance();
                    return gTab;
                case 1:
                    FriendBoardFragment fTab = FriendBoardFragment.newInstance();

                    return fTab;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }


}
