// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;


import com.google.gson.Gson;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.net.URL;


/**
 * Created by arbass on 7/2/15.
 */
@ParseClassName("Clouds")
public class Cloud extends ParseObject {

    //================================================================================
    // Properties
    //================================================================================

    private CloudAnswers mCloudAnswers;

    private String cloudPicBase64;
    private static final String UPLOAD_USER = "userUploaded";


    public String getCloudId() {
        return this.getObjectId();
    }

    public String getUrl() {
        return (String) this.get("url");
    }

    public void setUrl(URL imageUrl) {
        this.put("url", imageUrl);
    }

    public CloudAnswers mCloudAnswers() {
        return mCloudAnswers;
    }

    public void reported() {
        int numReports = this.getInt("numReported");

        this.increment("numReported"); //add the number of reports.
        this.saveInBackground();//save to server.
    }


    private CloudAnswers getCloudAnswers() {

        CloudAnswers cloudAnswers;
        String json = (String) this.get("resultsTEST");

        // There are no previous answers in parse
        if (json == null) {
            cloudAnswers = new CloudAnswers();
        } else {
            cloudAnswers = deJSONify(json);
            // Cloud answers could be null if json improperly formed
            if (cloudAnswers == null) {
                cloudAnswers = new CloudAnswers();
            }
        }

        return cloudAnswers;
    }

    public void setUploadUser(ParseUser user) {
        this.put(UPLOAD_USER, user);
    }

    public void setCloudPic(ParseFile file) {
        this.put("cloudUploadedPic", file);

    }

    public String getCloudPic() {
        ParseFile cloudPic = (ParseFile) this.get("cloudUploadedPic");
        try {
            byte[] data = cloudPic.getData();
            cloudPicBase64 = new String(data, "UTF-8");
        } catch (Exception e) {

        }
        return cloudPicBase64;
    }

    public void getResults(String answer, OnPointsCalcListener listener) {

        mCloudAnswers = getCloudAnswers();
        // gets result object to pass back to CloudFrag
        mCloudAnswers.getResults(answer, listener); //this is what jumpstarts everything

    }


    /**
     * Saves user's answer to the cloud object,
     * updates top answers and saves to parse
     *
     * @param answer the users answer
     */
    public void saveAnswer(String answer) {

        // Adds answer to cloud answers
        mCloudAnswers.addAnswer(answer);

        // Stores new answers on cloud
        this.put("resultsTEST", JSONify(mCloudAnswers));

        this.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });

    }

    private String JSONify(CloudAnswers answers) {
        Gson gson = new Gson();
        String json = gson.toJson(answers);
        return json;
    }

    private CloudAnswers deJSONify(String json) {
        Gson gson = new Gson();
        CloudAnswers cloudAnswers = gson.fromJson(json, CloudAnswers.class);
        return cloudAnswers;
    }

    public interface OnPointsCalcListener {
        void onPointsCalc(GameResults results);
    }

}

