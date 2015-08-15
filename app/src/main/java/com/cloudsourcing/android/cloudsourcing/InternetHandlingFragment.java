// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.support.v4.app.DialogFragment;

/**
 * Created by yada on 7/24/15.
 */
public abstract class InternetHandlingFragment extends DialogFragment {

    /**
     * This class introduces two methods that must be implemented,
     * networkLost() and networkRegained(), which is the fragment's
     * way of dealing with the changes in network (freezing UI, we
     * may have to cancel Parse queries in some fragments, etc.).
     **/


    //Methods to implement in all activities to handle network changes.

    public abstract void networkLost();

    public abstract void networkRegained();


}
