// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

/**
 * Created by jsoyinka on 7/15/15.
 */

public class FriendBoardFragment extends LeaderBoardFragment {

    public static FriendBoardFragment newInstance() {
        return new FriendBoardFragment();
    }

    @Override
    boolean isFriendBoard() {
        return true;
    }
}
