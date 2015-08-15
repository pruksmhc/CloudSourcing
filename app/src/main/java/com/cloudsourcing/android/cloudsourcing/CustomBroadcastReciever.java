// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.greenrobot.event.EventBus;

/**
 * Created by yada on 7/24/15.
 */
public class CustomBroadcastReciever extends BroadcastReceiver {
    /**
     * Broadcast recievers recieve signals from
     * System level.
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        EventBus.getDefault().post(new NetworkChangedEvent());
        //post to the event bus when it has recieved a
        //change in network state.
    }
}
