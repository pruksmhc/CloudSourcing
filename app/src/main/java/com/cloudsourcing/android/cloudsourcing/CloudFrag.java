// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cloudsourcing.android.cloudsourcing.StateAnimator.Builder;
import static com.cloudsourcing.android.cloudsourcing.StateAnimator.DEFAULT_STATE;
import static com.cloudsourcing.android.cloudsourcing.StateAnimator.StateChangeListener;

/**
 * Created by angelmaredia on 7/4/15.
 */

public class CloudFrag extends InternetHandlingFragment
        implements CloudController.OnCloudSearchFinishedListener,
        CloudImageView.CloudImageViewListener,
        Cloud.OnPointsCalcListener {
    //================================================================================
    // Properties
    //================================================================================

    private Cloud mCloud;
    private GameResults mResults;
    private CameraActivity mCamera = new CameraActivity();
    private CloudController mCloudController;
    private ParseUser mUser;
    private String mUserAnswer;
    private int mRankIndex;
    public static final int REPORT_DIALOG_FRAGMENT = 1;
    private static final int REQUEST_PHOTO = 0;
    private final String TAG = "com.cloudsourcing.android.cloudsourcing.cloudfrag";
    private boolean mResultsLoaded;

    //Initializing the starting values of "translationY" for toolbar button animations.
    float mStartLeadershipPosition = 0;
    float mStartProfilePosition = 0;
    float mCameraButtonStartingPosition = 0;

    private boolean unFoldMenu = false;


    //================================================================================
    // View Binders
    //================================================================================

    @Bind(R.id.submit_button)
    ImageButton mSubmitButton;

    @Bind(R.id.user_input_view)
    View mInputView;

    @Bind(R.id.input_edit_text)
    EditText mInputEditText;

    @Bind(R.id.leadership_button)
    FloatingActionButton mLeadershipButton;

    @Bind(R.id.menu_button)
    FloatingActionButton mMenuButton;

    @Bind(R.id.camera_button)
    FloatingActionButton mCameraButton;

    @Bind(R.id.actual_profile_button)
    FloatingActionButton mProfileButton;

    @Bind(R.id.cloud_image_view)
    CloudImageView mCloudImageView;

    @Bind(R.id.reset_button)
    Button mResetButton;

    @Bind(R.id.results_view)
    ResultsView mResultsView;

    @Bind(R.id.load_cloud_view)
    View mLoadCloud;

    @Bind(R.id.load_cloud_textview)
    TextView mLoadCloudTextview;

    @Bind(R.id.next_button)
    ImageButton mNextButton;


    //================================================================================
    // Constructors
    //================================================================================


    public static CloudFrag newInstance() {

        return new CloudFrag();
    }

    //================================================================================
    // Lifecycle
    //================================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mUser = ParseUser.getCurrentUser();
        mCloudController = CloudController.get(getActivity());

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu); //inflating the menu bar.
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_cloud, container, false);
        ButterKnife.bind(this, view);
        //Setting the starting positions of TranslationY to the ones defined in XML.
        mStartProfilePosition = mProfileButton.getTranslationY();
        mStartLeadershipPosition = mLeadershipButton.getTranslationY();
        mCameraButtonStartingPosition = mCameraButton.getTranslationY();

        mCloudImageView.setListener(this);

        mInputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    mSubmitButton.callOnClick();
                    return true;
                }
                return false;
            }
        });

        //responds to text changes in the box
        mInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (unFoldMenu) {
                    //closes menu when typing
                    foldMenu();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        newCloud();

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        //collapse the menu
        disappearMenu();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Depending on the request code recieved, the fragment
        //will implemenet different actions (this is the mechanism taht
        //the activity communicates with its host fragment to freeze/unfreeze
        //UI.

        if (resultCode != Activity.RESULT_OK) { //intent did not work.
            return;
        }
        switch (requestCode) {
            //flagging dialog.
            case REPORT_DIALOG_FRAGMENT:
                //if the request code ws from the dialog.
                mCloud.reported();
                String cloudId = mCloud.getCloudId(); //get the cloud Id.
                mUser.add("seenClouds", cloudId); //adds url to the seenClouds user array.
                Snackbar.make(getView(), "The photo has been flagged. ", Snackbar.LENGTH_LONG).show();

                AchievementQualifiers qualifiers = AchievementQualifiers.getQualifiers(mUser);

                ArrayList<AchievementQualifiers.UpdateActions> reportedAction = new ArrayList<>();
                reportedAction.add(AchievementQualifiers.UpdateActions.INCR_CLOUDS_REPORTED);

                // Perform updates
                qualifiers.updateQualifiers(reportedAction);
                AchievementQualifiers.setQualifiers(mUser, qualifiers);

                List<Achievement> achievements = AchievementHandler.get().fetchNewAchievements(qualifiers, mUser);

                for (Achievement achievement : achievements) {
                    AchievementDialog dialog = AchievementDialog.newInstance(
                            achievement,
                            new AchievementDialog.OnDialogInteractionListener() {
                                @Override
                                public void onDialogButtonPressed() {
                                    // No need to do anything here
                                }
                            });
                    dialog.show(getFragmentManager(), "tag");

                }

                newCloud();
                break;

            //TODO Camera
            case REQUEST_PHOTO: //this receives the photo requested.
                //if the request code was from the camera.
                //Show a snackbar notifying the user that their photo has been uploaded.
                Snackbar.make(getView(), "Your photo has been uploaded", Snackbar.LENGTH_LONG).show();
                mUser.increment("points", 20);
                mUser.saveInBackground();
                break;
        }

    }

    @Override
    public void networkLost() {
        //freeze the UI.
        disableInputUI();
        mNextButton.setEnabled(false);
        mMenuButton.setEnabled(false);
        mLeadershipButton.setEnabled(false);
        mProfileButton.setEnabled(false);
        mCameraButton.setEnabled(false);
        mCloudImageView.networkLost();
        //all buttons don't work.**/
    }

    @Override
    public void networkRegained() {
        //unfreeze UI.
        enableInputUI();
        mNextButton.setEnabled(true);
        mMenuButton.setEnabled(true);
        mProfileButton.setEnabled(true);
        mCameraButton.setEnabled(true);
        mLeadershipButton.setEnabled(true);
        mCloudImageView.networkRegained();

    }

    //================================================================================
    // Click Listeners
    //================================================================================

    @OnClick(R.id.menu_button)
    public void menuButtonClicked() {
        //unfold the menu.
        //photo icon credits go to www.flaticon.com

        //if the toolbar is not unfolded.
        //play sound.
        if (unFoldMenu == false) {
            //unfold the buttons down.
            unfoldMenu();
        } else {
            //if they are unfolded, fold them back up.
            foldMenu();
        }
    }


    @OnClick(R.id.reset_button)
    public void resetButtonClicked() {
        Toast.makeText(getActivity(), "Refreshing clouds...", Toast.LENGTH_SHORT).show();
        ArrayList<String> resetClouds = new ArrayList<String>();
        mUser.put("seenClouds", resetClouds); //resetting the seen clouds.
        mUser.saveInBackground();
    }

    @OnClick(R.id.reset_achievements_button)
    public void resetAchievementsButton() {
        AchievementQualifiers.clearAchievements(mUser);

        ParseQuery<ParseObject> query = new ParseQuery<>("UserAchievements");
        query.whereEqualTo("user", mUser);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> achievements, ParseException e) {
                if (e == null) {
                    for (ParseObject achievement : achievements) {
                        achievement.deleteInBackground();
                    }
                    Toast.makeText(getActivity(), "Resetting achievements...", Toast.LENGTH_SHORT).show();

                } else {
                    Log.d("UserAchievement", "Error: " + e.getMessage());
                }
            }
        });

    }

    @OnClick(R.id.submit_button)
    public void submitButtonClicked() {

        //closes menu when submit is pressed
        if (unFoldMenu) {
            foldMenu();
        }
        disableInputUI();
        mUserAnswer = formatAnswer(mInputEditText.getText().toString());
        if (isValidAnswer(mUserAnswer)) {

            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            String cloudId = mCloud.getCloudId(); //get the cloud Id.
            mUser.add("seenClouds", cloudId); //adds url to the seenClouds user array.
            //when the user hits submit butotn, if connection is lost, must freeze entire process.
            mCloud.getResults(mUserAnswer, this);
            loadResultsAnim();

        } else {
            Toast.makeText(getActivity(), "Invalid input", Toast.LENGTH_LONG).show();
            enableInputUI();
        }

    }

    //TODO Camera
    @OnClick(R.id.camera_button)
    public void cameraButtonClicked() {
        boolean cameraFree = mCamera.checkCameraHardware(getActivity());
        if (cameraFree) {
            Intent intent = new Intent(getActivity(), CameraActivity.class);
            startActivityForResult(intent, REQUEST_PHOTO);
        } else {
            showNoCameraDialog();
        }
    }


    @OnClick(R.id.leadership_button)
    public void mLeadershipButtonClicked() {
        Intent intent = new Intent(getActivity(), LeaderBoardActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.actual_profile_button)
    public void mProfileButtonClicked() {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.next_button)
    public void nextButtonClicked() {
        animateHideNextButton();
        animateHideResultsView();
        hideLoadCloud();
    }

    //================================================================================
    // OnCloudSearchListener Implementation
    //================================================================================

    @Override
    public void onCloudFound(Cloud cloud) {
        //mNextButton.clearAnimation();
        mCloud = cloud;
        mCloudImageView.loadCloudView(mCloud);
    }

    @Override
    public void onCloudNotFound() {
        //mNextButton.clearAnimation();
        mCloudImageView.showNoClouds();
    }

    //================================================================================
    // CloudImageViewListener Implementation
    //================================================================================

    @Override
    public void onCloudImageLoaded() {
        enableInputUI();
        showInputView();
    }

    @Override
    public void onCloudImageFailed() {
        newCloud();
    }

    @Override
    public void onRefreshClicked() {
        newCloud();
    }

    @Override
    public void onFlagClicked() {
        showReportDialog();
    }

    //================================================================================
    // OnPointsCalcListener Implementation
    //================================================================================

    @Override
    public void onPointsCalc(GameResults results) {
        mResults = results;
        mResultsLoaded = true;
        afterPointsLoaded();
    }

    //================================================================================
    // Private Methods
    //================================================================================

    // You can call this method with extra actions you know of
    private ArrayList<Achievement> checkAchievements() {
        // Get current user qualifiers
        AchievementQualifiers qualifiers = AchievementQualifiers.getQualifiers(mUser);

        // Build a list of update actions
        ArrayList<AchievementQualifiers.UpdateActions> actions = new ArrayList<>();

        // Update CloudsPlayed
        actions.add(AchievementQualifiers.UpdateActions.INCR_CLOUDS_PLAYED);

        // Update TopAnswers and TopAnswersStreak
        if (mResults.getUserRank() < 4) {
            actions.add(AchievementQualifiers.UpdateActions.INCR_TOP_ANSWERS);
            actions.add(AchievementQualifiers.UpdateActions.INCR_TOP_ANSWERS_STREAK);
        } else {
            actions.add(AchievementQualifiers.UpdateActions.RESET_TOP_ANSWERS_STREAK);
        }

        // Update days played and days played streak
        Date lastDate = mUser.getDate("lastPlayed");
        if (lastDate == null) {
            // First time
            actions.add(AchievementQualifiers.UpdateActions.INCR_DAYS_PLAYED);
            actions.add(AchievementQualifiers.UpdateActions.INCR_DAYS_PLAYED_STREAK);
        } else if (!DateUtils.isToday(lastDate.getTime())) {
            // Haven't played yet today
            if (!DateUtils.isToday(lastDate.getTime() + DateUtils.DAY_IN_MILLIS)) {
                actions.add(AchievementQualifiers.UpdateActions.INCR_DAYS_PLAYED);
                actions.add(AchievementQualifiers.UpdateActions.INCR_DAYS_PLAYED_STREAK);
            } else {
                actions.add(AchievementQualifiers.UpdateActions.INCR_DAYS_PLAYED);
                actions.add(AchievementQualifiers.UpdateActions.RESET_DAYS_PLAYED_STREAK);
            }
        }
        mUser.put("lastPlayed", new Date());
        mUser.saveEventually();

        // Perform updates
        qualifiers.updateQualifiers(actions);
        AchievementQualifiers.setQualifiers(mUser, qualifiers);

        // Get and return new achievements
        return AchievementHandler.get().fetchNewAchievements(qualifiers, mUser);

    }

    private void disableInputUI() {
        mInputEditText.setEnabled(false);
        mSubmitButton.setEnabled(false);
    }

    private void showInputView() {
        mResultsView.setVisibility(View.GONE);
        mInputView.setVisibility(View.VISIBLE);
    }

    private void enableInputUI() {
        mInputEditText.setEnabled(true);
        mSubmitButton.setEnabled(true);
    }

    private void showResultsView() {
        mResultsView.setVisibility(View.VISIBLE);
    }

    private void unfoldMenu() {
        Animator leadershipButtonMoveDown = ObjectAnimator.ofFloat(mLeadershipButton, "translationY", mStartLeadershipPosition, 0).setDuration(300);
        leadershipButtonMoveDown.start();
        Animator profileButtonMoveDown = ObjectAnimator.ofFloat(mProfileButton, "translationY", mStartProfilePosition, 0).setDuration(300);
        profileButtonMoveDown.start();
        Animator cameraButtonMoveDown = ObjectAnimator.ofFloat(mCameraButton, "translationY", mCameraButtonStartingPosition, 0).setDuration(300);
        cameraButtonMoveDown.start();
        unFoldMenu = true;
    }

    private void foldMenu() {
        Animator leadershipButtonMoveUp = ObjectAnimator.ofFloat(mLeadershipButton, "translationY", 0, mStartLeadershipPosition).setDuration(300);
        leadershipButtonMoveUp.start();
        Animator profileButtonMoveUp = ObjectAnimator.ofFloat(mProfileButton, "translationY", 0, mStartProfilePosition).setDuration(300);
        profileButtonMoveUp.start();
        Animator cameraButtonMoveUp = ObjectAnimator.ofFloat(mCameraButton, "translationY", 0, mCameraButtonStartingPosition).setDuration(300);
        cameraButtonMoveUp.start();
        unFoldMenu = false;
    }

    private void disappearMenu() {

        Animator leadershipButtonMoveUp = ObjectAnimator.ofFloat(mLeadershipButton, "translationY", 0, mStartLeadershipPosition).setDuration(1);
        leadershipButtonMoveUp.start();
        Animator profileButtonMoveUp = ObjectAnimator.ofFloat(mProfileButton, "translationY", 0, mStartProfilePosition).setDuration(1);
        profileButtonMoveUp.start();
        Animator cameraButtonMoveUp = ObjectAnimator.ofFloat(mCameraButton, "translationY", 0, mCameraButtonStartingPosition).setDuration(1);
        cameraButtonMoveUp.start();
        unFoldMenu = false;
    }

    private void animateShowResultsView() {

        ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(mResultsView, "translationY", mCloudImageView.getTop() - 20, mCloudImageView.getHeight() + 90);
        translateAnimator.setDuration(1000);
        translateAnimator.setInterpolator(new BounceInterpolator());

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mResultsView, "alpha", 0f, 1f);
        alphaAnimator.setDuration(500);


        showResultsView();
        translateAnimator.start();
        alphaAnimator.start();

        translateAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animateNextButton();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void animateNextButton() {

        //fades in the next button and makes it pulse
        final Animation pulse = AnimationUtils.loadAnimation(getActivity(), R.anim.pulse);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mNextButton, "alpha", 0f, 1f);
        alphaAnimator.setDuration(500);
        mNextButton.setVisibility(View.VISIBLE);
        alphaAnimator.start();
        mNextButton.startAnimation(pulse);
    }

    private void animateHideNextButton() {
        //fades out the next button
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mNextButton, "alpha", 1f, 0f);
        alphaAnimator.setDuration(500);
        alphaAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mNextButton.clearAnimation();
                mNextButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mNextButton.clearAnimation();
                mNextButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        alphaAnimator.start();
    }

    private void animateHideResultsView() {
        ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(
                mResultsView,
                "translationY",
                mResultsView.getTranslationY(),
                mCloudImageView.getTop());
        translateAnimator.setDuration(500);
        translateAnimator.setInterpolator(new AccelerateInterpolator());
        translateAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                newCloud();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mResultsView, "alpha", 1f, 0f);
        alphaAnimator.setDuration(500);
        alphaAnimator.setInterpolator(new AccelerateInterpolator());

        translateAnimator.start();
        alphaAnimator.start();

    }


    private String formatAnswer(String rawAnswer) {
        String newAnswer = rawAnswer.trim();
        newAnswer = newAnswer.toLowerCase();
        return newAnswer;
    }

    private boolean isValidAnswer(String answer) {
        return (answer != null && !answer.equals("") && answer.length() <15 && answer.toLowerCase() !="cloud");

    }


    private void newCloud() {
        mInputEditText.setText("");
        mInputEditText.setEnabled(false);
        mNextButton.setVisibility(View.GONE);
        mResultsLoaded = false;
        mCloudController = CloudController.get(getActivity());
        mCloudController.getCloud(this);

    }

    private void checkRank() {
        int tempIndex = mUser.getInt("points") / 1000;
        if (tempIndex != mRankIndex) {
            Resources res = getResources();
            mRankIndex = tempIndex;
            mUser.put("rank", RankText.updateRank(tempIndex, res));
            mUser.put("rankIndex", mRankIndex);
            // Also flag that user has achieved a new rank!
        }
    }

    private void afterPointsLoaded() {
        mResultsView.bindResults(mResults);
        mInputView.setVisibility(View.GONE);
        mUser.increment("points", mResults.getPointsScored());

        mCloud.saveAnswer(mUserAnswer);
        checkRank();
        ArrayList<Achievement> newAchievements = checkAchievements();
        mUser.saveInBackground();

        for (Achievement achievement : newAchievements) {
            AchievementDialog dialog = AchievementDialog.newInstance(
                    achievement,
                    new AchievementDialog.OnDialogInteractionListener() {
                        @Override
                        public void onDialogButtonPressed() {
                            // No need to do anything here
                        }
                    });
            dialog.show(getFragmentManager(), "tag");

        }
    }

    private void showReportDialog() {
        CloudAlertDialogFrag dialog = new CloudAlertDialogFrag();
        dialog.setDialogText("Report this image?", "REPORT", "CANCEL");
        dialog.setTargetFragment(this, REPORT_DIALOG_FRAGMENT);
        dialog.show(getActivity().getSupportFragmentManager(), "ReportDialogFrag");
    }

    private void showNoCameraDialog() {
        CloudAlertDialogFrag dialog = new CloudAlertDialogFrag();
        dialog.setDialogText("There is no camera on your device.", "", "OK");
        dialog.show(getActivity().getSupportFragmentManager(), "NoCameratDialogFrag");
    }

    private void loadResultsAnim() {

        //Anim to display the load cloud and wait for the results
        mLoadCloud.setVisibility(View.VISIBLE);
        mInputView.setVisibility(View.GONE);

        View parent = (View) mLoadCloud.getParent();
        int height = parent.getHeight();
        //int endHeight = mLoadCloud.getBottom()-650;

        //States the animation waits for/responds to
        final int STATE_RESULTS_LOADING = DEFAULT_STATE + 1; //Cloud gets bigger
        final int STATE_RESULTS_LOAD_CONT = DEFAULT_STATE + 2; //Cloud pulses
        final int STATE_RESULTS_LOADED = DEFAULT_STATE + 3; //Cloud collapses

        //defines the actions for the state
        final StateAnimator animator = new Builder()
                .addView(mLoadCloud)
                .defineState(STATE_RESULTS_LOADING)
                .scaleX(1.5f)
                .scaleY(1.5f)
                .defineState(STATE_RESULTS_LOAD_CONT)
                .scaleX(1f)
                .scaleY(1f)
                .defineState(STATE_RESULTS_LOADED)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .translationXPx(-110)
                .translationYPx(height / 2 - mLoadCloud.getHeight() / 2 + 15)
                .build();

        //Listener to respond to the end and beginning of the states
        animator.addStateChangeListener(new StateChangeListener() {
            @Override
            public void onStartEnteringState(int state) {
                switch (state) {
                    case STATE_RESULTS_LOADING:
                        mLoadCloudTextview.setText(mUserAnswer);
                        break;
                    case STATE_RESULTS_LOADED:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onStartLeavingState(int state) {

            }

            @Override
            public void onFinishEnteringState(int state) {
                switch (state) {
                    case STATE_RESULTS_LOADING:
                        if (mResultsLoaded) {
                            animator.animateToState(STATE_RESULTS_LOADED, 500);
                            mLoadCloudTextview.setText(mResults.getPointsScored() + " points");
                        } else {
                            animator.animateToState(STATE_RESULTS_LOAD_CONT, 1000);
                        }
                        break;

                    case STATE_RESULTS_LOAD_CONT:
                        if (mResultsLoaded) {
                            animator.animateToState(STATE_RESULTS_LOADED, 750);
                            mLoadCloudTextview.setText(mResults.getPointsScored() + " points");
                        } else {
                            animator.animateToState(STATE_RESULTS_LOADING, 1000);
                        }
                        break;

                    case STATE_RESULTS_LOADED:
                        animateShowResultsView();
                        break;

                    default:
                        break;
                }

            }

            @Override
            public void onFinishLeavingState(int state) {
            }
        });

        animator.animateToState(STATE_RESULTS_LOADING, 1500);
    }

    private void hideLoadCloud() {

        //Will reset the load could to its original position
        final int STATE_HIDE_CLOUD = DEFAULT_STATE + 5;
        final int STATE_RESET_CLOUD = DEFAULT_STATE + 6;
        mLoadCloud.setVisibility(View.GONE);

        StateAnimator animator = new StateAnimator.Builder()
                .addView(mLoadCloud)
                .defineState(STATE_HIDE_CLOUD)
                .alpha(0)
                .defineState(STATE_RESET_CLOUD)
                .scaleX(1f)
                .scaleY(1f)
                .translationXPx(0)
                .translationYPx(0)
                .build();

        animator.animateToState(STATE_HIDE_CLOUD, 500);
        animator.animateToState(STATE_RESET_CLOUD, 750);
    }

}
