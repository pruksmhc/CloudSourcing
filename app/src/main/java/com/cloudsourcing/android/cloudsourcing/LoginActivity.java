// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends InternetHandlingActivity
        implements AchievementHandler.OnAchievementsUpdatedListener,
        LogInCallback,
        GraphRequest.GraphJSONObjectCallback {
    //================================================================================
    // Properties
    //================================================================================

    private static final String DID_ANIMATE = "DID_ANIMATE";
    private boolean mUserInfoFetched;
    private boolean mAchievementsUpdated;
    private ParseUser mUser;
    private AchievementHandler mAchievementHandler;

    //================================================================================
    // View Binders
    //================================================================================

    @Bind(R.id.login_button)
    LoginButton mLoginButton;

    @Bind(R.id.login_view)
    LinearLayout mLoginView;

    @Bind(R.id.icon_image_view)
    ImageView mIconImageView;

    @Bind(R.id.login_loading_view)
    View mLoginLoadingView;


    //================================================================================
    // Lifecycle
    //================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        checkNetwork(); //check the network when user first logs in.
        mLoginButton.setToolTipMode(LoginButton.ToolTipMode.NEVER_DISPLAY);
        mAchievementHandler = AchievementHandler.get();

        mUserInfoFetched = false;
        mAchievementsUpdated = false;

        ParseUser currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
            mUser = currentUser;
            getUserInfo();
            mAchievementHandler.updateAchievements(mUser, this);
        } else {
            animateLoginButton();
        }
    }

    @Override
    public void onHandleConnectionRegained() {
        //If there is internet, make login button
        //clickable and visible.
        //override superclass method.
        for (InternetHandlingFragment fragment : getHostedFrags()){
            fragment.networkRegained();
        }

        mLoginButton.setVisibility(View.VISIBLE);//make the login button visible.
        mLoginButton.setEnabled(true);
    }
    @Override
    public void onHandleConnectionLost() {
        for (InternetHandlingFragment fragment : getHostedFrags()){
            fragment.networkLost();
        }
        //when connection lost, make button unclickable
        // override superclass method. .
        mLoginButton.setEnabled(false);
    }

    @Override
    protected View getRootView() {
        return findViewById(R.id.login_root_view);
    }

    @Override
    protected ArrayList<InternetHandlingFragment> getHostedFrags() {
        //return null because there are no fragments.
        return new ArrayList<>();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }


    //================================================================================
    // Click Listeners
    //================================================================================


    @OnClick(R.id.login_button)
    public void loginButtonClicked() {
        mLoginButton.setVisibility(View.INVISIBLE);
        login();
    }

    //================================================================================
    // Private Methods
    //================================================================================

    private void animateLoginButton() {
        int iconYStart = mIconImageView.getTop();
        int iconYEnd = iconYStart - 200;

        ObjectAnimator moveIconUp = ObjectAnimator
                .ofFloat(mIconImageView, "translationY", iconYStart, iconYEnd)
                .setDuration(1000);
        moveIconUp.setInterpolator(new AccelerateDecelerateInterpolator());
        moveIconUp.setStartDelay(250);

        ObjectAnimator showLoginButton = ObjectAnimator.ofFloat(mLoginView, "alpha", 0f, 1f);
        showLoginButton.setDuration(1000);
        showLoginButton.setStartDelay(500);

        showLoginButton.start();
        moveIconUp.start();
    }


    private synchronized void startMain() {
        if (mUserInfoFetched && mAchievementsUpdated) {
            Intent intent = GameActivity.newIntent(this);
            startActivity(intent);
            overridePendingTransition(R.anim.fab_in, R.anim.fab_out);
            finish();
        }
    }

    private void login() {
        mLoginLoadingView.setVisibility(View.VISIBLE);
        final List<String> permissions = Arrays.asList("public_profile", "user_friends");
        //giving permissions to access user friend's list.
        ParseFacebookUtils.logInWithReadPermissionsInBackground(
                LoginActivity.this,
                permissions,
                this);
    }


    private void getUserInfo() {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                this);
        request.executeAsync();
    }

    // Achievement Callback
    @Override
    public void onAchievementsUpdated() {
        mAchievementsUpdated = true;
        startMain();
    }

    // Login Callback
    @Override
    public void done(ParseUser parseUser, ParseException e) {
        if (parseUser != null) {
            mUser = parseUser;
            getUserInfo();
            mAchievementHandler.updateAchievements(mUser, this);
        } else {
            checkNetwork();
            //check if it's a network problem.
            mLoginLoadingView.setVisibility(View.INVISIBLE);
            Toast.makeText(
                    LoginActivity.this,
                    "An error occurred: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
            mLoginButton.setVisibility(View.VISIBLE);
        }
    }

    // Graph Callback
    @Override
    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
        if (jsonObject != null) {
            try {
                // Save the user profile info in a user property
                mUser.put("facebookId", jsonObject.getString("id"));
                mUser.put("facebookName", jsonObject.getString("name"));
                // Initialize Rank if New User
                if (mUser.getInt("points") == 0) {
                    Resources res = getResources();
                    mUser.put("rank", res.getString(R.string.rank_00));
                }
                mUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        mUserInfoFetched = true;
                        startMain();
                    }
                });

            } catch (Exception e) {
                Toast.makeText(
                        LoginActivity.this,
                        "An error occurred: " + e.getMessage(),
                        Toast.LENGTH_LONG)
                        .show();
            }
        }
    }



}
