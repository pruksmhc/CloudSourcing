package com.cloudsourcing.android.cloudsourcing;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by arbass on 7/22/15.
 */
public class AchievementHandler {

    private static AchievementHandler sAchievementHandler;
    private ArrayList<Achievement> mAchievements;
    private ArrayList<Achievement> mEarnedAchievements;
    private ArrayList<Achievement> mUnearnedAchievements;

    private boolean mAllAchievementsFetched;
    private boolean mUserAchievementsFetched;
    private static final String JOIN_TABLE = "UserAchievements";
    private static final String USER = "user";
    private static final String ACHIEVEMENT = "achievement";

    public static AchievementHandler get() {
        if (sAchievementHandler == null) {
            sAchievementHandler = new AchievementHandler();
        }
        return sAchievementHandler;

    }

    private AchievementHandler() {
        // Fetch all achievements in Parse
        mAchievements = new ArrayList<>();
        mEarnedAchievements = new ArrayList<>();
        mUnearnedAchievements = new ArrayList<>();
        mAllAchievementsFetched = false;
        mUserAchievementsFetched = false;

    }

    public void clearAchievementHandler() {
        mEarnedAchievements.clear();
        mUnearnedAchievements.clear();
        mUnearnedAchievements.addAll(mAchievements);
        for (Achievement achievement : mAchievements) {
            achievement.setIsUnlocked(false);
        }

    }

    public ArrayList<Achievement> fetchNewAchievements(
            AchievementQualifiers userQualifiers,
            ParseUser user) {

        ArrayList<Achievement> newAchievements = new ArrayList<>();
        ArrayList<ParseObject> joinTableEntries = new ArrayList<>();

        for (Achievement achievement : mUnearnedAchievements) {
            if (achievement.isAchieved(userQualifiers)) {
                achievement.setDateAchieved(new Date());
                achievement.setIsUnlocked(true);
                mEarnedAchievements.add(achievement);
                newAchievements.add(achievement);

                // Add to join table
                ParseObject entry = new ParseObject(JOIN_TABLE);
                entry.put(USER, user);
                entry.put(ACHIEVEMENT, achievement);
                joinTableEntries.add(entry);
            }

        }
        mUnearnedAchievements.removeAll(newAchievements);
        ParseObject.saveAllInBackground(joinTableEntries);

        return newAchievements;

    }

    public void updateAchievements(
            ParseUser user,
            final OnAchievementsUpdatedListener listener) {

        //First clear all achievements
        mAchievements.clear();
        mEarnedAchievements.clear();
        mUnearnedAchievements.clear();


        //All achievements query
        ParseQuery<Achievement> allQuery = ParseQuery.getQuery("Achievements");
        allQuery.findInBackground(new FindCallback<Achievement>() {
            @Override
            public void done(List<Achievement> list, ParseException e) {
                if (e == null) {
                    mAchievements.addAll(list);
                    mAllAchievementsFetched = true;
                    checkCompletion(listener);
                } else {
                    throw new RuntimeException("Find Achievements Error: " + e.getMessage());
                }
            }
        });

        // User achievements query
        ParseQuery<ParseObject> userQuery = ParseQuery.getQuery("UserAchievements");
        userQuery.whereEqualTo("user", user);
        userQuery.include("achievement");
        userQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    for (ParseObject item : list) {
                        Achievement achievement = (Achievement) item.getParseObject("achievement");
                        achievement.setDateAchieved(item.getCreatedAt());
                        achievement.setIsUnlocked(true);
                        mEarnedAchievements.add(achievement);
                    }
                    mUserAchievementsFetched = true;
                    checkCompletion(listener);
                } else {
                    throw new RuntimeException("Find User Achievements Error");
                }
            }
        });

    }

    // This method is a gate to the listener that requires both threads be completed
    private synchronized void checkCompletion(OnAchievementsUpdatedListener listener) {
        if (mAllAchievementsFetched && mUserAchievementsFetched) {

            // Populate unearned list
            mUnearnedAchievements.addAll(mAchievements);
            mUnearnedAchievements.removeAll(mEarnedAchievements);

            listener.onAchievementsUpdated();
        }
    }


    public interface OnAchievementsUpdatedListener {
        void onAchievementsUpdated();
    }

    public List<Achievement> getAllAchievements() {
        return mAchievements;
    }


}
