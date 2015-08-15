package com.cloudsourcing.android.cloudsourcing;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by jsoyinka on 7/29/15.
 */
public class PublicProfileFragment extends InternetHandlingFragment {

    @Bind(R.id.log_out_button)
    FloatingActionButton mLogOutButton;

    @Bind(R.id.profile_functionality_button)
    FloatingActionButton mProfileFunctionalityButton;

    @Bind(R.id.invite_friends_button)
    FloatingActionButton mFindFriendsButton;

    @Bind(R.id.fb_profile_image_view)
    RoundProfilePictureView mFBProfileImage;

    @Bind(R.id.name_text_view)
    TextView mUsernameText;

    @Bind(R.id.points_text_view)
    TextView mPointsText;

    @Bind(R.id.rank_text_view)
    TextView mRankText;

    @Bind(R.id.achievement_recycler_view)
    RecyclerView mAchievementRecyclerView;

    private ParseUser mUser;
    public static ParseUser sPublicUser;
    private int mPoints;
    private String mRank;
    public int REQUEST_ACHIEVEMENT_DETAIL = 1;

    public static PublicProfileFragment newInstance(ParseUser user) {
        PublicProfileFragment pPFrag = new PublicProfileFragment();
        sPublicUser = user;
        return pPFrag;
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);

        //setting the shadow for the user's name.
        mUser = ParseUser.getCurrentUser();
        mUsernameText.setShadowLayer(1.5f, -1, 1, Color.LTGRAY);


        // Checks to see if profile fragment was accessed with a public user
        if (sPublicUser != null) {
            usePublicProfileSettings();
        } else {
            usePrivateProfileSettings();
        }

        mRank = mUser.getString("rank");
        mRankText.setText(mRank);

        mPoints = mUser.getInt("points");
        mPointsText.setText(Integer.toString(mPoints) + " points");

        mFBProfileImage.setProfileId(mUser.getString("facebookId"));
        mUsernameText.setText(mUser.getString("facebookName"));

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sPublicUser = null;
    }

    @Override
    public void networkLost() {
    }

    @Override
    public void networkRegained() {
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
            if (mAchievement == null) {
                mImageView.setImageDrawable(getResources()
                        .getDrawable(R.drawable.achievement_locked));
            } else {
                mImageView.setImageDrawable(achievement.getAchievementImage(getResources()));
            }
        }

        @Override
        public void onClick(View v) {
            AchievementDetailsDialog dialog = new AchievementDetailsDialog().newInstance(mAchievement);
            dialog.setTargetFragment(PublicProfileFragment.this,REQUEST_ACHIEVEMENT_DETAIL);
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
            if (mAchievements == null) {
                holder.bindAchievement(null);
            } else {
                holder.bindAchievement(mAchievements.get(position));
            }
        }

        @Override
        public int getItemCount() {
            if (mAchievements == null) {
                return 1;
            } else {
                return mAchievements.size();
            }
        }
    }

    private void usePublicProfileSettings() {
        // Modifies view for public user
        mUser = sPublicUser;
        mProfileFunctionalityButton.setVisibility(View.GONE);
        mLogOutButton.setVisibility(View.GONE);
        mFindFriendsButton.setVisibility(View.GONE);

        // Query for achievements
        List<Achievement> achievements = findPublicUserAchievements(sPublicUser);

        // Instantiate Achievement RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mAchievementRecyclerView.setLayoutManager(layoutManager);

    }

    private void usePrivateProfileSettings() {
        // Achievement RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mAchievementRecyclerView.setLayoutManager(layoutManager);
        AchievementAdapter adapter =
                new AchievementAdapter(AchievementHandler.get().getAllAchievements());
        mAchievementRecyclerView.setAdapter(adapter);
    }

    private ArrayList<Achievement> findPublicUserAchievements(ParseUser user) {
        final ArrayList<Achievement> achievements = new ArrayList<>();
        ParseQuery<ParseObject> allQuery = ParseQuery.getQuery("UserAchievements");
        allQuery.whereEqualTo("user", user);
        allQuery.include("achievement");
        allQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    for (ParseObject item : list) {
                        Achievement achievement = (Achievement) item.getParseObject("achievement");
                        achievement.setIsUnlocked(true);
                        achievements.add(achievement);
                    }
                    AchievementAdapter adapter = new AchievementAdapter(achievements);
                    mAchievementRecyclerView.setAdapter(adapter);
                } else {
                    throw new RuntimeException("Find Achievements Error: " + e.getMessage());
                }
            }
        });

        return achievements;
    }


}
