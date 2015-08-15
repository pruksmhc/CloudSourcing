// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jsoyinka on 7/10/15.
 */
public class LeaderBoard {
    private static LeaderBoard sLeaderBoard;
    private ParseUser mCurrentUser = ParseUser.getCurrentUser();
    int mCount;

    List<ParseUser> mUsers = new ArrayList<>();
    String TAG = "com.cloudsourcing.android.cloudsourcing.LeaderBoard";


    public static LeaderBoard get() {  //Soon Will Contain Boolean To Choose Global vs. Friends List
        if (sLeaderBoard == null) {
            sLeaderBoard = new LeaderBoard();
        }
        return sLeaderBoard;
    }

    //update users.
    public List<ParseUser> getUsers(boolean isFriendBoard) {
        if (isFriendBoard) {
            fetchFriendUsers();
        } else {
            fetchGlobalUsers();
        }
        return mUsers;
    }

    private void fetchFriendUsers() {
        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");

        try {
            ArrayList<String> friendsList = (ArrayList<String>) mCurrentUser.get("user_friends_id");
            friendsList.add(friendsList.size(), mCurrentUser.getString("facebookId"));
            query.whereContainedIn("facebookId", friendsList);
            query.orderByDescending("points");
            mUsers = (ArrayList<ParseUser>) query.find();
        } catch (ParseException e){
            Log.e(TAG, e.getMessage());
        }

    }
    private void fetchGlobalUsers() {
        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");

        try {
            query.orderByDescending("points");
            mUsers = (ArrayList<ParseUser>) query.find();
        } catch (ParseException e){
            Log.e(TAG, e.getMessage());
        }

    }
}
