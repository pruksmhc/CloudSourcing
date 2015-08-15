// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by arbass on 7/13/15.
 * <p/>
 * This is a custom view that displays the results in a list,
 * a next button.
 */
public class ResultsView extends LinearLayout {

    //================================================================================
    // Properties
    //================================================================================

    private Context mContext;

    //================================================================================
    // View Binders
    //================================================================================

    @Bind(R.id.results_list_view)
    ListView mResultsListView;


    //================================================================================
    // Constructors
    //================================================================================


    public ResultsView(Context context) {
        this(context, null);
    }

    public ResultsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResultsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        // Inflate layout
        LayoutInflater.from(context)
                .inflate(R.layout.results_layout, this, true);

        ButterKnife.bind(this);

    }


    //================================================================================
    // Public Methods
    //================================================================================

    public void bindResults(GameResults gameResults) {
        mResultsListView.setAdapter(new ResultsListAdapter(gameResults));

    }


    //================================================================================
    // Click Listeners
    //================================================================================

    //================================================================================
    // Inner Classes
    //================================================================================

    public class ResultsListAdapter extends BaseAdapter {

        private List<GameResults.ResultEntry> mResultEntries;


        public ResultsListAdapter(GameResults gameResults) {
            mResultEntries = gameResults.mResultEntries;
        }

        @Override
        public int getCount() {
            return mResultEntries.size();
        }

        @Override
        public GameResults.ResultEntry getItem(int position) {
            return mResultEntries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;       // What else can I do here?
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.results_list_item, parent, false);
            convertView.setClickable(true);

            // Get an inflator
            ButterKnife.bind(getContext(), convertView);
            ViewHolder holder = new ViewHolder(convertView);
            //creating the item and binding ig.
            GameResults.ResultEntry entry = mResultEntries.get(position);
            int rank = entry.getRank();
            String answerText = entry.getGuess();
            int popularity = entry.getPopularity();
            //   Profile mProfile = Profile.getCurrentProfile();
            ParseUser mUser = ParseUser.getCurrentUser();
            holder.bindAnswer(rank, mUser, popularity, answerText);
            return convertView;
        }


    }

    private class ViewHolder implements View.OnClickListener {
        private TextView mRankTextView;
        private RoundProfilePictureView mProfilePictureView;
        private TextView mAnswerTextView;
        private TextView mPopularityTextView;
        private String mUserAnswer;

        public ViewHolder(View view) {
            view.setOnClickListener(this);
            mRankTextView = (TextView) view.findViewById(R.id.rank_text_view);
            mProfilePictureView = (RoundProfilePictureView) view.findViewById(R.id.fb_profile_image_view);
            mAnswerTextView = (TextView) view.findViewById(R.id.answer_text_view);
            mPopularityTextView = (TextView) view.findViewById(R.id.popularity_text_view);
        }

        @Override
        public void onClick(View v) {
            if (mPopularityTextView.getVisibility() == View.INVISIBLE) {
                //show percentages.
                mPopularityTextView.setVisibility(View.VISIBLE);
            } else {
                mPopularityTextView.setVisibility(View.INVISIBLE);
               //hide percentages.
            }
        }

        public void bindAnswer(int rank, ParseUser user, int popularity, String answer) {
            //for centering purposes.


            if (rank == -1) {
                // This is the users guess
                mRankTextView.setVisibility(View.GONE);
                mProfilePictureView.setProfileId(user.getString("facebookId"));
                mProfilePictureView.setVisibility(View.VISIBLE);
                mPopularityTextView.setText(popularity + "%");
                mAnswerTextView.setTextColor(getResources().getColor(R.color.cs_blue));
                //for your own personal answer
                if(answer.length() >14){
                    //centering so that the user's answer looks centered
                    //if the user's answer is over 15 characters.
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAnswerTextView.getLayoutParams();
                    //make the user's answer to the right of the FB profile image.
                    params.addRule(RelativeLayout.RIGHT_OF, R.id.fb_profile_image_view);
                    //set the margins so it looks centered.
                    params.setMargins(50,40 ,0, 0);
                    //this is for the ellipsized attribute.
                    //We want it to show "..." so this shrinks hte width
                    //so the "..."
                    params.width= 600;
                    mAnswerTextView.setLayoutParams(params); //causes layout update
                    mAnswerTextView.setText(answer);
                }
                else{
                    //if the answer length is shorter than 14.
                    RelativeLayout.LayoutParams head_params = (RelativeLayout.LayoutParams) mAnswerTextView.getLayoutParams();
                    head_params.setMargins(50,40 ,0, 0);
                    //set the layout parameters.
                    mAnswerTextView.setLayoutParams(head_params);
                    mAnswerTextView.setText(answer);
                }
            } else {
                //For answers that are not yours.
                mRankTextView.setText(Integer.toString(rank));
                RelativeLayout.LayoutParams head_params = (RelativeLayout.LayoutParams) mAnswerTextView.getLayoutParams();
               //set the margins to be centered (with the rank)
                head_params.setMargins(40,40,0, 0);
                mAnswerTextView.setLayoutParams(head_params);
                mAnswerTextView.setText(answer);
                //make the percentages invisible.
                mPopularityTextView.setText(popularity + "%");
            }
            mUserAnswer = answer;
            mPopularityTextView.setVisibility(View.INVISIBLE);
        }
    }


}
