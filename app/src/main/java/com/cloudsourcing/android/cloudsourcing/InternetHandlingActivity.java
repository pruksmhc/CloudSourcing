// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.parse.ParseUser;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by yada on 7/23/15.
 */
public abstract class InternetHandlingActivity extends AppCompatActivity {

    /**
     * The purpose of this class is to have the activities subscribe
     * to the eventbus, and communicate to their respective fragments
     * to freeze or unfreeze the UI.
     * *
     */
    protected static int REQUEST_NETWORK_LOST = 5;
    protected static int REQUEST_NETWORK_REGAIN = 6;
    protected Fragment mFragment;
    protected ParseUser mGlobalUser;


    public void onEvent(NetworkChangedEvent event) {
        //When each activity detects an event in the eventbus
        //check the network.
        checkNetwork();
    }

    public void checkNetwork() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in air plan mode it will be null
        if (netInfo != null && netInfo.isConnected()) {
            //THere is connection, meaning there was no internet connection before
            //and there is a connection now.
            onHandleConnectionRegained();
        } else if (netInfo == null || !netInfo.isConnected()) {
            //on airplane mode and/or internet connectivity is lost.
            //this means there was connection efore and now there
            //is no connectin.
            onHandleConnectionLost();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkUserLogged();
        //subscribe to the eventbus.
        //The eventbus is a singleton.
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //unsubscribe to the eventbus.
        EventBus.getDefault().unregister(this);
    }


    //we want this to be abstract because different activities
    //are designed differently, from fragments with the UI to
    //having all the uI to itself.
    public void onHandleConnectionRegained() {
        for (InternetHandlingFragment fragment : getHostedFrags()){
            fragment.networkRegained();
        }
        Snackbar.make(getRootView(), "Internet connected", Snackbar.LENGTH_LONG).show();
    }

    public void onHandleConnectionLost(){
        for (InternetHandlingFragment fragment : getHostedFrags()){
            fragment.networkLost();
        }
        Snackbar.make(getRootView(), "Internet lost", Snackbar.LENGTH_LONG).show();
    }

    // Method determines whether user has logged out and needs to return to login activity
    private void checkUserLogged() {
        mGlobalUser = ParseUser.getCurrentUser();
        Resources res = getResources();

        // Checks if current user is logged out, and current activity isn't the launcher activity
        if (mGlobalUser == null && !(getClass().getSimpleName()
                                        .equals(res.getString(R.string.launcher_activity)))) {

            // Checks to see if current activity is top of stack, if so starts Login and finishes
            if (getCallingActivity() == null) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);

                finish();
            }
            // Otherwise it just finishes
            else {
                finish();
            }

        }
    }

    protected abstract View getRootView();
    protected abstract ArrayList<InternetHandlingFragment> getHostedFrags();
    //this method returns an arraylist of internetHandlingFragments
    //must be implemented by every activity to return gramens.
}
