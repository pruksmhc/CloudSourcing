// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arbass on 7/22/15.
 */

@ParseClassName(Achievement.CLASS_NAME)
public class Achievement extends ParseObject {

    public static final String CLASS_NAME = "Achievements";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private boolean isUnlocked;
    private Date dateAchieved;

    public String getName() {
        return this.getString(NAME);
    }


    String getDescr() {
        return this.getString(DESCRIPTION);
    }

    public Drawable getAchievementImage(Resources resources) {
        if (isUnlocked()) {
            return Achievement.getImage(this, resources);
        } else {
            return resources.getDrawable(R.drawable.achievement_locked);
        }

    }



    public AchievementQualifiers getAchievementQualifiers() {
        return AchievementQualifiers.getQualifiers(this);
    }

    public boolean isAchieved(AchievementQualifiers userQualifiers) {
        return getAchievementQualifiers().isAchieved(userQualifiers);
    }

    public String getDateAchieved() {
        DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
        String output = outputFormatter.format(dateAchieved);
        //Get only the date
        return output;
    }

    public void setDateAchieved(Date dateAchieved) {
        this.dateAchieved = dateAchieved;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setIsUnlocked(boolean isUnlocked) {
        this.isUnlocked = isUnlocked;
    }

    private static Drawable getImage(Achievement achievement, Resources resources) {
        switch(achievement.getName()) {
            case "52 Cloud Pickup":
                return resources.getDrawable(R.drawable.achievement_52_cloud_pickup);
            case "Raining Cats and Dogs":
                return resources.getDrawable(R.drawable.achievement_raining_cats_and_dogs);
            case "Cloud Watch":
                return resources.getDrawable(R.drawable.achievement_cloud_watch);
            case "Cloud Nine":
                return resources.getDrawable(R.drawable.achievement_cloud_nine);
            case "Oh Snap(shot)":
                return resources.getDrawable(R.drawable.achievement_oh_snapshot);
            default:
                return resources.getDrawable(R.drawable.achievement_template);
        }

    }
}
