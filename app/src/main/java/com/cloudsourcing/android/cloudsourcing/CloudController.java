// Copyright 2004-present Facebook. All Rights Reserved.



package com.cloudsourcing.android.cloudsourcing;

import android.content.Context;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arbass on 7/6/15.
 */
public class CloudController {

    ArrayList<Cloud> mClouds;
    private static CloudController sCloudController;
    String TAG = CloudController.class.getSimpleName();

    private OnCloudSearchFinishedListener mListener;

    public static CloudController get(Context context) {
        if (sCloudController == null) {
            sCloudController = new CloudController(context);
        }
        return sCloudController;
    }

    //TODO: why is there a context here
    private CloudController(Context context) {
        mClouds = new ArrayList<>();
    }

    public void getCloud(OnCloudSearchFinishedListener fragment) {
        mListener = fragment;

        if (mClouds != null && mClouds.size() > 0) {
            mListener.onCloudFound(mClouds.remove(0));

        } else {
                ParseQuery<Cloud> query = ParseQuery.getQuery("Clouds"); //query the clouds.
                ParseUser user = ParseUser.getCurrentUser(); //get the user.

                ArrayList<String> seenClouds = (ArrayList<String>) user.get("seenClouds");
                //gets the arraylist full of strings with the seen clouds.
             if (seenClouds != null) {
                    query.whereNotContainedIn("objectId", seenClouds); //find
                }
                query.setLimit(3); //pull only three clouds.
                query.findInBackground(new FindCallback<Cloud>() {
                    @Override
                    public void done(List<Cloud> list, ParseException e) {
                        if (list != null && list.size() > 0) {

                            mClouds.addAll(list);
                            mListener.onCloudFound(mClouds.remove(0));
                        } else {
                            mListener.onCloudNotFound();
                        }
                    }
                });

        }
    }

    public static void uploadCloud(String encImg) {
        byte[] data = encImg.getBytes();
        ParseFile cloudPicFile = new ParseFile("cloud.txt", data);
        cloudPicFile.saveInBackground();

        Cloud cloud = new Cloud();
        cloud.setCloudPic(cloudPicFile);
        cloud.setUploadUser(ParseUser.getCurrentUser());
        cloud.saveInBackground();

    }


    public interface OnCloudSearchFinishedListener {
        void onCloudFound(Cloud cloud);

        void onCloudNotFound();
    }
}

