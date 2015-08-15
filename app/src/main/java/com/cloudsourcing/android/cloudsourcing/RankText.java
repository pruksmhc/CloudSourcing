package com.cloudsourcing.android.cloudsourcing;

import android.content.res.Resources;

/**
 * Created by jsoyinka on 7/9/15.
 */
public class RankText {
    Resources mRes;

    public RankText RankText(Resources res) {
        mRes = res;
        return new RankText();
    }

    public static String updateRank(int rankText, Resources res) {
        switch (rankText) {
            case 6:
                return res.getString(R.string.rank_06);
            case 5:
                return res.getString(R.string.rank_05);
            case 4:
                return res.getString(R.string.rank_04);
            case 3:
                return res.getString(R.string.rank_03);
            case 2:
                return res.getString(R.string.rank_02);
            case 1:
                return res.getString(R.string.rank_01);
            case 0:
                return res.getString(R.string.rank_00);
            default:
                return res.getString(R.string.rank_overflow);
        }

    }

    public static String updateSubtitle(int titleText, Resources res) {

        switch (titleText) {
            case 6:
                return res.getString(R.string.caption_06);
            case 5:
                return res.getString(R.string.caption_05);
            case 4:
                return res.getString(R.string.caption_04);
            case 3:
                return res.getString(R.string.caption_03);
            case 2:
                return res.getString(R.string.caption_02);
            case 1:
                return res.getString(R.string.caption_01);
            case 0:
                return res.getString(R.string.caption_00);
            default:
                return res.getString(R.string.caption_overflow);
        }

    }

}

