// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.parse.ParseUser;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends PublicProfileFragment {

    private boolean unFoldMenu = false;
    float mInviteFriendsButtonStartPosition = 0;
    float mLoginButtonStartPosition = 0;
    private int REQUEST_ACHIEVEMENT_DETAIL = 1;



    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);

        //get the x value of the actual buttons for animation.
        mLoginButtonStartPosition = mLogOutButton.getX();
        mInviteFriendsButtonStartPosition = mFindFriendsButton.getX();


        return view;
    }

    @OnClick(R.id.log_out_button)
    public void logOutButtonClicked() {
        ParseUser user = ParseUser.getCurrentUser();
        user.logOut();

        getActivity().finish();
    }

    @OnClick(R.id.profile_functionality_button)
    public void profileFunctionalityClicked() {
        if (unFoldMenu == false) {
            //unfold the buttons to the sides of the profile functionality.
            Animator logOutMoveLeft = ObjectAnimator.ofFloat(mLogOutButton, "translationX", mLoginButtonStartPosition, 0).setDuration(300);
            logOutMoveLeft.start();
            Animator inviteFriendsButtonMoveRight = ObjectAnimator.ofFloat(mFindFriendsButton, "translationX", mInviteFriendsButtonStartPosition, 0).setDuration(300);
            inviteFriendsButtonMoveRight.start();

        } else {
            //if they are unfolded, fold them back into the profile funcitonality inwards.
            Animator logOutMoveRight = ObjectAnimator.ofFloat(mLogOutButton, "translationX", 0, mLoginButtonStartPosition).setDuration(300);
            logOutMoveRight.start();
            Animator inviteFriendsButtonMoveleft = ObjectAnimator.ofFloat(mFindFriendsButton, "translationX", 0, mInviteFriendsButtonStartPosition).setDuration(300);
            inviteFriendsButtonMoveleft.start();
        }
        unFoldMenu = !unFoldMenu;
    }

    @OnClick(R.id.invite_friends_button)
    //TODO check for updates in the ParseUser's friend_using_app list,
    public void inviteFriendsClicked() {
        //show the dialog to invite people via push notifications.
        String appLinkUrl, previewImageUrl;

        appLinkUrl = "https://fb.me/891626440891337";
        previewImageUrl = "https://www.mydomain.com/my_invite_image.jpg";

        if (AppInviteDialog.canShow()) {
            AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(appLinkUrl)
                    .setPreviewImageUrl(previewImageUrl)
                    .build();
            AppInviteDialog.show(this, content);
        }

    }

    public void disableUI() {
        mLogOutButton.setEnabled(false);
        mFindFriendsButton.setEnabled(false);
        mProfileFunctionalityButton.setEnabled(false);
    }


    public void enableUI() {
        mLogOutButton.setEnabled(true);
        mFindFriendsButton.setEnabled(true);
        mProfileFunctionalityButton.setEnabled(true);
    }

    @Override
    public void networkLost() {
        disableUI();
    }

    @Override
    public void networkRegained() {
        enableUI();
        refresh();
        //call the fragment to re-load profile.
    }

    public void refresh() {
        //refresh the profile.

    }


    public class AchievementViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @Bind(R.id.achievement_list_image_view)
        ImageView mImageView;

        private Achievement mAchievement;

        public AchievementViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bindAchievement(Achievement achievement) {
            mAchievement = achievement;
            mImageView.setImageDrawable(achievement.getAchievementImage(getResources()));
        }

        @Override
        public void onClick(View v) {
         AchievementDetailsDialog dialog = new AchievementDetailsDialog().newInstance(mAchievement);
            dialog.setTargetFragment(ProfileFragment.this,REQUEST_ACHIEVEMENT_DETAIL);
            dialog.show(getFragmentManager(), "Tag");
        }
    }

    public class AchievementAdapter extends RecyclerView.Adapter<AchievementViewHolder> {

        List<Achievement> mAchievements;

        public AchievementAdapter(List<Achievement> achievements) {
            mAchievements = achievements;
        }


        @Override
        public AchievementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.achievement_list_item, parent, false);
            return new AchievementViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AchievementViewHolder holder, final int position) {
            holder.bindAchievement(mAchievements.get(position));
        }

        @Override
        public int getItemCount() {
            return mAchievements.size();
        }
    }

}
