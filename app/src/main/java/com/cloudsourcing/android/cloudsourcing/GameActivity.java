// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by angelmaredia on 7/4/15.
 */

public class GameActivity extends InternetHandlingActivity {

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, GameActivity.class);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        FragmentManager fm = getSupportFragmentManager();
        Fragment mainFrag = fm.findFragmentById(R.id.game_fragment_container);
        if (mainFrag == null) {
            mainFrag = CloudFrag.newInstance();
            fm.beginTransaction()
                    .add(R.id.game_fragment_container, mainFrag)
                    .commit();
        }
        //you want to return the fragment containerid for this, and you want to loop through all the fragment containers.
    }

    @Override
    protected View getRootView() {
        return findViewById(R.id.game_fragment_container);
    }

    @Override
    protected ArrayList<InternetHandlingFragment> getHostedFrags(){
       ArrayList<InternetHandlingFragment> mInternetHandlingFragments = new ArrayList<>();
       FragmentManager fm = getSupportFragmentManager();
       InternetHandlingFragment mainFrag = (InternetHandlingFragment) fm.findFragmentById(R.id.game_fragment_container);
       mInternetHandlingFragments.add(mainFrag);
       return mInternetHandlingFragments;
   }

}

