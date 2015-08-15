// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

/**
 * Created by yada on 7/31/15.
 */

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yada on 7/31/15.
 */
public class AchievementDetailsDialog extends DialogFragment {

    protected Achievement mAchievement;

    @Bind(R.id.title_achievement)
    TextView mTitleAchieved;


    @Bind(R.id.achievement_name_text_view)
    TextView mAchievementNameTextView;

    @Bind(R.id.achievement_description_text_view)
    TextView mAchievementDescrTextView;

    @Bind(R.id.achievement_okay_button)
    Button mOkayButton;

    @Bind(R.id.achievement_image_view)
    ImageView mAchievementImageView;

    @Bind(R.id.share_with_friends)
    ShareButton mShareButton;

    @Bind(R.id.date_achieved)
    TextView mDateAchieved;

    public static AchievementDetailsDialog newInstance(
            Achievement achievement) {
        AchievementDetailsDialog dialog = new AchievementDetailsDialog();
        dialog.setAchievement(achievement);
        return dialog;
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getDialog() != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(getDialog().getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            Window window = getDialog().getWindow();
            window.setAttributes(lp);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_achievement, container, false);
        ButterKnife.bind(this, view);

        mAchievementImageView.setImageDrawable(mAchievement.getAchievementImage(getResources()));
        mAchievementNameTextView =
                (TextView) view.findViewById(R.id.achievement_name_text_view);

        mAchievementDescrTextView =
                (TextView) view.findViewById(R.id.achievement_description_text_view);

        mOkayButton = (Button) view.findViewById(R.id.achievement_okay_button);
        mOkayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mShareButton = (ShareButton) view.findViewById(R.id.share_with_friends);
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("https://developers.facebook.com")).
                        setContentTitle("I just earned the " + mAchievement.getName() + " achievement" +
                                " in Cloudsourcing!").
                        setContentDescription("Cloudsourcing- Family Feud, but clouds.")
                .build();
        mShareButton.setShareContent(content);
        if (mAchievement.isUnlocked()) {
            mTitleAchieved.setText("Status: Achieved");

            mDateAchieved.setText("Date unlocked: "+ mAchievement.getDateAchieved().toString());
        } else {
            mTitleAchieved.setText("Locked");
            mDateAchieved.setText("Keep on playing to unlock");
        }




        bindAchievement();

        return view;
    }

    //================================================================================
    // Private Methods
    //================================================================================

    protected void setAchievement(Achievement achievement) {
        mAchievement = achievement;
    }



    private void bindAchievement() {
        mAchievementNameTextView.setText(mAchievement.getName());
        mAchievementDescrTextView.setText(mAchievement.getDescr());
    }

    @OnClick(R.id.achievement_okay_button)
    public void okayButtonClicked() {
        dismiss();
    }



}
