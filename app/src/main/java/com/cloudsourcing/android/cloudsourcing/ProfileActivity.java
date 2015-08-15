// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import java.util.ArrayList;

public class ProfileActivity extends InternetHandlingActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.profile_container);
        if (fragment == null) {
            fragment = ProfileFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.profile_container, fragment)
                    .commit();
        }
    }

    @Override
    protected View getRootView() {
        return findViewById(R.id.profile_container);
    }

    @Override
    protected ArrayList<InternetHandlingFragment> getHostedFrags(){
        ArrayList<InternetHandlingFragment> mInternetHandlingFragments = new ArrayList<>();
        FragmentManager fm = getSupportFragmentManager();
        InternetHandlingFragment mainFrag = (InternetHandlingFragment) fm.findFragmentById(R.id.profile_container);
        mInternetHandlingFragments.add(mainFrag);
        return mInternetHandlingFragments;
    }



}
