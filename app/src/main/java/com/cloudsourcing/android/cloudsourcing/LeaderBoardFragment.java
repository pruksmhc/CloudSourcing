// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class LeaderBoardFragment extends InternetHandlingFragment {

    private static final double PERCT_SCREEN_HEIGHT_FOR_DIALOG = 14.0/15.0;

    private Dialog progressDialog;

    @Bind(R.id.global_recycler_view)
    RecyclerView mUserRecyclerView;

    private UserAdapter mAdapter;
    private Resources mRes;
    private ParseUser mCurrentUser;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_leader_board, container, false);
        ButterKnife.bind(this, view);

        mRes = getResources();
        mCurrentUser = ParseUser.getCurrentUser();

        mUserRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    public class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ParseUser mUser;
        @Bind(R.id.list_item_username)
        TextView mUsername;

        @Bind(R.id.list_item_rank)
        TextView mRankText;

        @Bind(R.id.list_item_points)
        TextView mPointsText;

        @Bind(R.id.list_item_profile_picture)
        RoundProfilePictureView mPictureView;

        @Bind(R.id.list_item_position)
        TextView mPositionText;

        private boolean mPointsVisible;

        public UserHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bindUser(ParseUser user, int position) {
            mUser = user;
            mPictureView.setProfileId(mUser.getString("facebookId"));
            mUsername.setText(mUser.getString("facebookName"));
            mRankText.setText(mUser.getString("rank"));
            mPointsText.setText(Integer.toString(mUser.getInt("points")));
            mPositionText.setText("" + position);
            if (mCurrentUser == mUser) {
                mPositionText.setTextColor(mRes.getColor(R.color.kudos_blue));
            } else {
                mPositionText.setTextColor(mRes.getColor(R.color.pitch_black));
            }
        }

        @Override
        public void onClick(View v) {
            if (isFriendBoard()) {
                if (mCurrentUser != mUser) {
                    showPublicProfile(mUser);
                } else {
                    showPrivateProfile();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    public void networkLost() {
        //dismiss the progress dialog.
        progressDialog.dismiss();
    }

    public void networkRegained() {
        //refresh the UI, reload the
        //leadership borads.
        updateUI();
    }


    private class UserAdapter extends RecyclerView.Adapter<UserHolder> {

        private List<ParseUser> mUsers;

        public UserAdapter(List<ParseUser> users) {
            mUsers = users;
        }

        @Override
        public UserHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            //getting the layout of each viewholder.
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            //inflating the xml for each userholder.
            View view = layoutInflater.inflate(R.layout.list_item_user, viewGroup, false);
            //returnign new user holder.
            return new UserHolder(view);
        }

        @Override
        public void onBindViewHolder(UserHolder holder, int position) {
            //filling the contents of each specific viewholder.
            ParseUser user = mUsers.get(position);
            holder.bindUser(user, position + 1);
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
            //how many viewholders do you want.
        }

        public void setUsers(List<ParseUser> users) {
            mUsers = users;
        }
    }

    public void updateUI() {
        progressDialog = ProgressDialog.show(getActivity(), "", "Getting CloudMasters..", true);
        getUserFriends(new OnGotFriendsListener() {
            @Override
            public void gotFriends() {
                progressDialog.dismiss();
                LeaderBoard leaderBoard = LeaderBoard.get();
                List<ParseUser> users = leaderBoard.getUsers(isFriendBoard());
                if (mAdapter == null) {
                    mAdapter = new UserAdapter(users);
                    mUserRecyclerView.setAdapter(mAdapter);
                } else {
                    mAdapter.setUsers(users);
                    mAdapter.notifyDataSetChanged();
                }
            }

        });
    }

    private void getUserFriends(final OnGotFriendsListener listener) {
        //this necessitates the private listener interface.
        new GraphRequest(
                //getting the person's friends.
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        //after getting the response, then querying through json and
                        //saving the id fields of each friend into a Arraylist in the
                        //user_friends_id column
                        try {
                            ParseUser currentUser = ParseUser.getCurrentUser();
                            JSONArray friendId = response.getJSONObject().getJSONArray("data");
                            ArrayList<String> friendsList = new ArrayList<String>();
                            currentUser.put("user_friends_id", friendsList);
                            for (int i = 0; i < friendId.length(); i++) {
                                JSONObject friendData = friendId.getJSONObject(i);
                                String id = friendData.getString("id"); //getting the ID.i
                                currentUser.add("user_friends_id", id);
                            }
                            currentUser.saveInBackground();
                            //callback
                            listener.gotFriends();
                        } catch (Exception e) {

                        }
                    }
                }
        ).executeAsync();
        //should automatically update the friends list.
    }

    private interface OnGotFriendsListener {
        //this is the listener interface, when you call getFriends, you must insert this
        //listener, and then define the gotFriends() method to tell what to do after
        //the program has gotten friends.
        void gotFriends();
    }

    abstract boolean isFriendBoard();

    // Creates a dialog showing the selected user's public profile
    private void showPublicProfile(ParseUser mUser) {
        PublicProfileFragment pPFrag = PublicProfileFragment.newInstance(mUser);
        pPFrag.show(getActivity().getSupportFragmentManager(), "dialog");
        getActivity().getSupportFragmentManager().executePendingTransactions();
        DisplayMetrics metrics = mRes.getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        // Resizes dialog.
        pPFrag.getDialog().getWindow()
                .setLayout(width, (int) (height * PERCT_SCREEN_HEIGHT_FOR_DIALOG));
    }

    private void showPrivateProfile() {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        startActivity(intent);
    }

}
