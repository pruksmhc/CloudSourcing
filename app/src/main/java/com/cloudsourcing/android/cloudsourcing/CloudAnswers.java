package com.cloudsourcing.android.cloudsourcing;

import android.util.Log;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by arbass on 7/8/15.
 */

public class CloudAnswers {

    int mTotalVotes;

    ArrayList<CloudAnswer> mTopAnswers;

    HashMap<String, Integer> mAnswers;

    public CloudAnswers() {
        mAnswers = new HashMap<>();
        mTopAnswers = new ArrayList<>();
    }

    private void updateTopAnswers(CloudAnswer newAnswer) {
        CloudAnswer oldAnswer;


        // Loop through top answers
        for (int i = 0; i < 3; i++) {
            oldAnswer = mTopAnswers.get(i);

            // The new answer is the same
            if (newAnswer.sameKey(oldAnswer)) {

                // Replace with new version
                mTopAnswers.remove(i);
                mTopAnswers.add(i, newAnswer);
                break;

            }
            // Answer is larger than a topAnswer
            else if (newAnswer.compareTo(oldAnswer) > 0) {
                // Bump top answers down to make room
                bumpTopAnswers(i);
                mTopAnswers.set(i, newAnswer);
                break;
            }
        }
    }

    // Bumps down top answers beginning at index
    private void bumpTopAnswers(int index) {
        CloudAnswer mTempAnswer = mTopAnswers.get(index+1);
        for(int i = 2; i > index;i--){
            mTopAnswers.set(i, mTopAnswers.get(i-1));
            //working backwards, set the current index
            //to the one before it
            //until you hit the index.

        }

    }

    /**
     * Adds answer to cloud answers and updates top answers
     *
     * @param answer the user's answer
     */
    public void addAnswer(String answer) {

        // Gets count of user's answer
        Integer count = mAnswers.get(answer);

        // Count is null if not found in hash-map
        if (count == null) {
            count = 0;
        }

        // Creates new CloudAnswer to store in answers
        CloudAnswer newAnswer = new CloudAnswer(answer, count + 1);

        // Update hashmap counts
        mAnswers.put(answer, count + 1);

        // Updates top answers if needed
        updateTopAnswers(newAnswer);

        // Increments total vote count
        mTotalVotes++;

    }

    // Removes null entries to mTopAnswers
    private void clearNullEntries() {
        while (mTopAnswers.size() < 4) {
            mTopAnswers.add(new CloudAnswer("", 0));
        }
    }


    /**
     * Creates a GameResults object based on the previous answers
     * to a cloud and the answer the user entered.
     *
     * @param answer the text the user entered
     * @return a new GameResults object with results
     */
    public void getResults(String answer, Cloud.OnPointsCalcListener listener) {

        GameResults results = new GameResults();

        // Handles if there are not 4 top answers
        clearNullEntries();

        // Gets the frequency of the text the user entered
        Integer foundCount = mAnswers.get(answer);

        // Null if not found
        if (foundCount == null) {
            foundCount = 0;
        }

        // Creates new answer with text and found count
        CloudAnswer newAnswer = new CloudAnswer(answer, foundCount);

        // Used to prevent answer for being recorded multiple times
        boolean answerLogged = false;
        Integer userRank = null;
        Integer userPopularity = null;

        // Loops through topAnswers to generate GameResults
        for (CloudAnswer oldAnswer : mTopAnswers) {

            // Calculates what percent of users entered same answer
            int tempPopularity;
            int tempRank = mTopAnswers.indexOf(oldAnswer) + 1;

            if ((oldAnswer.sameKey(newAnswer)                // If matches
                    || mTopAnswers.indexOf(oldAnswer) == 3  // If last entry
                    || oldAnswer.getText().equals("") )     // If entry is empty
                    && !answerLogged) {                     // Answer has not already been added

                tempPopularity = (int) ((float) newAnswer.getCount() / (float) mTotalVotes * 100);
                results.addResultEntry(-1, answer, tempPopularity); //todo return rank and pop
                answerLogged = true;
                userRank = tempRank;
                userPopularity = tempPopularity;

            } else {
                // Store the oldAnswer in the results
                String tempString = oldAnswer.getText();
                tempPopularity = (int) ((float) oldAnswer.getCount() / (float) mTotalVotes * 100);
                results.addResultEntry(tempRank, tempString, tempPopularity);

            }
        }
        calculatePoints(userRank, userPopularity, results, listener);
        results.setUserRank(userRank);
    }

    private void calculatePoints(int rank,
                                 int popularity,
                                 final GameResults results,
                                 final Cloud.OnPointsCalcListener listener) {

        HashMap<String, Integer> data = new HashMap<>();
        data.put("AnswerRank", rank);
        data.put("Percentage", popularity);
        ParseCloud.callFunctionInBackground("calculatePoints", data, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer points, ParseException e) {
                if (e == null) {
                    results.setPointsScored(points);
                    Log.d("Points: ", points.toString());
                    listener.onPointsCalc(results);
                }
            }
        });

    }


    private class CloudAnswer implements Comparable { //TODO JSON

        String mText;
        Integer mCount;

        public CloudAnswer(String text, int count) {
            mText = text;
            mCount = count;
        }

        public String getText() {
            return mText;
        }

        public void setText(String text) {
            mText = text;
        }

        public Integer getCount() {
            return mCount;
        }

        public void setCount(Integer count) {
            mCount = count;
        }

        public boolean sameKey(CloudAnswer other) {
            return (this.getText().equals((other.getText())));
        }


        @Override
        public int compareTo(Object another) {
            return this.getCount() - ((CloudAnswer) another).getCount();
        }

//        @Override
//        //Cloud answers are
//        public boolean equals(Object o) {
//            return (compareTo(o) == 0);
//        }
    }

}


