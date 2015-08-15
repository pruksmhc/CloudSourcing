// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

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
 * Created by arbass on 7/21/15.
 */
public class AchievementDialog extends DialogFragment {
    protected Achievement mAchievement;
    private OnDialogInteractionListener mListener;

    //================================================================================
    // View Binders
    //================================================================================

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

    //================================================================================
    // Public Methods
    //================================================================================

    public static AchievementDialog newInstance(
            Achievement achievement, OnDialogInteractionListener onDialogInteractionListener) {
        AchievementDialog dialog = new AchievementDialog();
        dialog.setAchievement(achievement);
        dialog.setListener(onDialogInteractionListener);

        return dialog;
    }

    public void setListener(OnDialogInteractionListener listener) {
        mListener = listener;
    }


    //================================================================================
    // Lifecycle
    //================================================================================

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
                mListener.onDialogButtonPressed();
                dismiss();
            }
        });
        mShareButton = (ShareButton) view.findViewById(R.id.share_with_friends);
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("https://developers.facebook.com")).
                        setContentTitle("I just earned the" + mAchievement.getName() + " achievement" +
                                "in Cloudsourcing!").
                        setContentDescription("Cloudsourcing- Family Feud, but clouds.")
                .build();
        mShareButton.setShareContent(content);


        bindAchievement();

        return view;
    }

    //================================================================================
    // Private Methods
    //================================================================================

    private void setAchievement(Achievement achievement) {
        mAchievement = achievement;
    }



    private void bindAchievement() {
        mAchievementNameTextView.setText(mAchievement.getName());
        mAchievementDescrTextView.setText(mAchievement.getDescr());
    }

    //================================================================================
    // Click Listeners
    //================================================================================

    @OnClick(R.id.achievement_okay_button)
    public void okayButtonClicked() {
        mListener.onDialogButtonPressed();
        dismiss();
    }

    //================================================================================
    // Interfaces
    //================================================================================

    public interface OnDialogInteractionListener {
        void onDialogButtonPressed();
    }
}
