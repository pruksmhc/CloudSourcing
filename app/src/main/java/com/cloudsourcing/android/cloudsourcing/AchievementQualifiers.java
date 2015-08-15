// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Created by arbass on 7/23/15.
 */
public class AchievementQualifiers {

    private int cloudsUploaded;
    private int daysPlayed;
    private int daysPlayedStreak;
    private int cloudsPlayed;
    private int topAnswers;
    private int topAnswersStreak;
    private int friendsInvited;
    private int cloudsReported;
    private static final String CLOUDS_UPLOADED = "cloudsUploaded";
    private static final String DAYS_PLAYED = "daysPlayed";
    private static final String DAYS_PLAYED_STREAK = "daysPlayedStreak";
    private static final String CLOUDS_PLAYED = "cloudsPlayed";
    private static final String TOP_ANSWERS = "topAnswers";
    private static final String TOP_ANSWERS_STREAK = "topAnswersStreak";
    private static final String FRIENDS_INVITED = "friendsInvited";
    private static final String CLOUDS_REPORTED = "cloudsReported";

    public boolean isAchieved(AchievementQualifiers userQualifiers) {
        boolean passes = true;

        // This if will short-circuit if any of the conditions are true
        if (getCloudsUploaded() > userQualifiers.getCloudsUploaded()
                || getCloudsPlayed() > userQualifiers.getCloudsPlayed()
                || getDaysPlayed() > userQualifiers.getDaysPlayed()
                || getDaysPlayedStreak() > userQualifiers.getDaysPlayedStreak()
                || getCloudsPlayed() > userQualifiers.getCloudsPlayed()
                || getTopAnswers() > userQualifiers.getTopAnswers()
                || getTopAnswers() > userQualifiers.getTopAnswers()
                || getTopAnswersStreak() > userQualifiers.getTopAnswersStreak()
                || getFriendsInvited() > userQualifiers.getFriendsInvited()
                || getCloudsReported() > userQualifiers.getCloudsReported()) {

            passes = false;
        }

        return passes;
    }

    // Retrieves achievement qualifiers from user or achievement
    public static AchievementQualifiers getQualifiers(ParseObject object) {
        AchievementQualifiers qualifiers = new AchievementQualifiers();

        qualifiers.setCloudsUploaded(object.getInt(CLOUDS_UPLOADED));
        qualifiers.setDaysPlayed(object.getInt(DAYS_PLAYED));
        qualifiers.setDaysPlayedStreak(object.getInt(DAYS_PLAYED_STREAK));
        qualifiers.setCloudsPlayed(object.getInt(CLOUDS_PLAYED));
        qualifiers.setTopAnswers(object.getInt(TOP_ANSWERS));
        qualifiers.setTopAnswersStreak(object.getInt(TOP_ANSWERS_STREAK));
        qualifiers.setFriendsInvited(object.getInt(FRIENDS_INVITED));
        qualifiers.setCloudsReported(object.getInt(CLOUDS_REPORTED));

        return qualifiers;
    }

    public static void setQualifiers(ParseUser user, AchievementQualifiers qualifiers) {
        user.put(CLOUDS_UPLOADED, qualifiers.getCloudsUploaded());
        user.put(DAYS_PLAYED, qualifiers.getDaysPlayed());
        user.put(DAYS_PLAYED_STREAK, qualifiers.getDaysPlayedStreak());
        user.put(CLOUDS_PLAYED, qualifiers.getCloudsPlayed());
        user.put(TOP_ANSWERS, qualifiers.getTopAnswers());
        user.put(TOP_ANSWERS_STREAK, qualifiers.getTopAnswersStreak());
        user.put(FRIENDS_INVITED, qualifiers.getFriendsInvited());
        user.put(CLOUDS_REPORTED, qualifiers.getCloudsReported());
        user.saveEventually();  // Todo maybe not here
    }

    //This is only for demo purposes. Resets achievements
    public static void clearAchievements(ParseUser user) {
        user.put(CLOUDS_UPLOADED, 0);
        user.put(DAYS_PLAYED, 0);
        user.put(DAYS_PLAYED_STREAK, 0);
        user.put(CLOUDS_PLAYED, 0);
        user.put(TOP_ANSWERS, 0);
        user.put(TOP_ANSWERS_STREAK, 0);
        user.put(FRIENDS_INVITED, 0);
        user.put(CLOUDS_REPORTED, 0);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                AchievementHandler.get().clearAchievementHandler();
            }
        });
    }

    // Enum defining possible update actions
    public enum UpdateActions {
        INCR_CLOUDS_UPLOADED,
        INCR_DAYS_PLAYED,
        INCR_DAYS_PLAYED_STREAK,
        RESET_DAYS_PLAYED_STREAK,
        INCR_CLOUDS_PLAYED,
        INCR_TOP_ANSWERS,
        INCR_TOP_ANSWERS_STREAK,
        RESET_TOP_ANSWERS_STREAK,
        INCR_FRIENDS_INVITED,
        INCR_CLOUDS_REPORTED
    }


    public void updateQualifiers(List<UpdateActions> actions) {
        for (UpdateActions action : actions) {
            switch (action) {
                case INCR_CLOUDS_UPLOADED:
                    cloudsUploaded++;
                    break;
                case INCR_DAYS_PLAYED:
                    daysPlayed++;
                    break;
                case INCR_DAYS_PLAYED_STREAK:
                    daysPlayedStreak++;
                    break;
                case RESET_DAYS_PLAYED_STREAK:
                    daysPlayedStreak = 0;
                    break;
                case INCR_CLOUDS_PLAYED:
                    cloudsPlayed++;
                    break;
                case INCR_TOP_ANSWERS:
                    topAnswers++;
                    break;
                case INCR_TOP_ANSWERS_STREAK:
                    topAnswersStreak++;
                    break;
                case RESET_TOP_ANSWERS_STREAK:
                    topAnswersStreak = 0;
                    break;
                case INCR_FRIENDS_INVITED:
                    friendsInvited++;
                    break;
                case INCR_CLOUDS_REPORTED:
                    cloudsReported++;
            }
        }
    }

    public int getCloudsUploaded() {
        return cloudsUploaded;
    }

    public void setCloudsUploaded(int cloudsUploaded) {
        this.cloudsUploaded = cloudsUploaded;
    }

    public int getDaysPlayed() {
        return daysPlayed;
    }

    public void setDaysPlayed(int daysPlayed) {
        this.daysPlayed = daysPlayed;
    }

    public int getDaysPlayedStreak() {
        return daysPlayedStreak;
    }

    public void setDaysPlayedStreak(int daysPlayedStreak) {
        this.daysPlayedStreak = daysPlayedStreak;
    }

    public int getCloudsReported() {
        return cloudsReported;
    }

    public void setCloudsReported(int cloudsReported) {
        this.cloudsReported = cloudsReported;
    }

    public int getCloudsPlayed() {
        return cloudsPlayed;
    }

    public void setCloudsPlayed(int cloudsPlayed) {
        this.cloudsPlayed = cloudsPlayed;
    }

    public int getTopAnswers() {
        return topAnswers;
    }

    public void setTopAnswers(int topAnswers) {
        this.topAnswers = topAnswers;
    }

    public int getTopAnswersStreak() {
        return topAnswersStreak;
    }

    public void setTopAnswersStreak(int topAnswersStreak) {
        this.topAnswersStreak = topAnswersStreak;
    }

    public int getFriendsInvited() {
        return friendsInvited;
    }

    public void setFriendsInvited(int friendsInvited) {
        this.friendsInvited = friendsInvited;
    }

}
