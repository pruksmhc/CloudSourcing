// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

/**
 * Created by jsoyinka on 7/15/15.
 */

public class GlobalBoardFragment extends LeaderBoardFragment {

    public static GlobalBoardFragment newInstance() {
        return new GlobalBoardFragment();
    }

    @Override
    boolean isFriendBoard() {
        return false;
    }
}
