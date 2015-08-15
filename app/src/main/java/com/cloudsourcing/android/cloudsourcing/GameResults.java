package com.cloudsourcing.android.cloudsourcing;

import java.util.ArrayList;

/**
 * Created by arbass on 7/7/15.
 */
public class GameResults {

    int mPointsScored;
    int mUserRank;

    ArrayList<ResultEntry> mResultEntries = new ArrayList<>();

    public void addResultEntry(int rank, String guess, int popularity) {
        ResultEntry newEntry = new ResultEntry(rank, guess, popularity);
        mResultEntries.add(newEntry);

    }

    public int getUserRank() {
        return mUserRank;
    }

    public void setUserRank(int userRank) {
        mUserRank = userRank;
    }

    public int getPointsScored() {
        return mPointsScored;
    }

    public void setPointsScored(int pointsScored) {
        mPointsScored = pointsScored;
    }

    public ArrayList<ResultEntry> getResultEntries() {
        return mResultEntries;
    }

    public class ResultEntry {
        int mRank;
        String mGuess;
        int mPopularity;

        private ResultEntry(int rank, String guess, int popularity) {
            mRank = rank;
            mGuess = guess;
            mPopularity = popularity;
        }

        public int getRank() {
            return mRank;
        }

        public void setRank(int rank) {
            mRank = rank;
        }

        public String getGuess() {
            return mGuess;
        }

        public void setGuess(String guess) {
            mGuess = guess;
        }

        public int getPopularity() {
            return mPopularity;
        }

        public void setPopularity(int popularity) {
            mPopularity = popularity;
        }
    }

}
