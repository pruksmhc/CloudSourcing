// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.util.SparseArray;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class StateAnimator {

    public static final int DEFAULT_STATE = 0;

    public interface StateChangeListener {
        public void onStartEnteringState(int state);
        public void onStartLeavingState(int state);
        public void onFinishEnteringState(int state);
        public void onFinishLeavingState(int state);
    }

    public static abstract class AbstractStateChangeListener implements StateChangeListener {

        @Override
        public void onStartEnteringState(int state) {}

        @Override
        public void onStartLeavingState(int state) {}

        @Override
        public void onFinishEnteringState(int state) {}

        @Override
        public void onFinishLeavingState(int state) {}
    }

    public static class Builder {
        private List<AnimatedView> mAnimatedViews = new ArrayList<>();

        public AnimatedView.Builder addView(View v) {
            return new AnimatedView.Builder(this, v);
        }

        /* package */ void addCompletedView(AnimatedView animatedView) {
            mAnimatedViews.add(animatedView);
        }

        public StateAnimator build() {
            return new StateAnimator(this);
        }
    }

    private List<AnimatedView> mAnimatedViews;
    private final List<StateChangeListener> mStateChangeListeners
            = new ArrayList<>();
    private int mState = DEFAULT_STATE;
    private boolean mCurrentlyAnimating = false;

    private StateAnimator(Builder builder) {
        mAnimatedViews = builder.mAnimatedViews;
    }

    public void addStateChangeListener(StateChangeListener stateChangeListener) {
        mStateChangeListeners.add(stateChangeListener);
    }

    public void removeStateChangeListener(StateChangeListener stateChangeListener) {
        mStateChangeListeners.remove(stateChangeListener);
    }

    /**
     * Set all views to the specified state immediately.
     *
     * No animation will be performed, but StateChangeListeners will be notified of the change.
     */
    public void jumpToState(final int newState) {
        for (StateChangeListener stateChangeListener : mStateChangeListeners) {
            // If we are in the middle of animating from state A -> B and we get a new
            // animateToState() command, we don't want to do the callbacks for leaving state A
            // since we already did them.
            if (!mCurrentlyAnimating) {
                stateChangeListener.onStartLeavingState(mState);
            }
            stateChangeListener.onStartEnteringState(newState);
        }

        for (AnimatedView animatedView : mAnimatedViews) {
            SparseArray<ViewState> viewStates = animatedView.getViewStates();
            ViewState defaultState = viewStates.get(DEFAULT_STATE);
            ViewState goalState = viewStates.get(newState, defaultState);
            View v = animatedView.getView();

            ViewCompat.setTranslationX(v, goalState.mTranslationX);
            ViewCompat.setTranslationY(v, goalState.mTranslationY);
            ViewCompat.setAlpha(v, goalState.mAlpha);
            ViewCompat.setScaleX(v, goalState.mScaleX);
            ViewCompat.setScaleY(v, goalState.mScaleY);
            ViewCompat.setRotation(v, goalState.mRotation);
        }

        for (StateChangeListener stateChangeListener : mStateChangeListeners) {
            stateChangeListener.onFinishLeavingState(mState);
            stateChangeListener.onFinishEnteringState(newState);
        }

        mCurrentlyAnimating = false;
        mState = newState;
    }

    /**
     * Animate all views to the specified state over the next X milliseconds.
     *
     * Any properties not set for the desired state will instead use the values from the default state
     */
    public void animateToState(final int newState, int durationMs) {
        ViewPropertyAnimatorListenerAdapter listener =
                new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(View view) {
                        for (StateChangeListener stateChangeListener : mStateChangeListeners) {
                            // If we are in the middle of animating from state A -> B and we get a new
                            // animateToState() command, we don't want to do the callbacks for leaving state A
                            // since we already did them.
                            if (!mCurrentlyAnimating) {
                                stateChangeListener.onStartLeavingState(mState);
                            }
                            stateChangeListener.onStartEnteringState(newState);
                        }
                        mCurrentlyAnimating = true;
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        for (StateChangeListener stateChangeListener : mStateChangeListeners) {
                            stateChangeListener.onFinishLeavingState(mState);
                            stateChangeListener.onFinishEnteringState(newState);
                        }
                        mCurrentlyAnimating = false;
                        mState = newState;
                    }
                };

        boolean first = true;

        for (AnimatedView animatedView : mAnimatedViews) {
            SparseArray<ViewState> viewStates = animatedView.getViewStates();
            ViewState defaultState = viewStates.get(DEFAULT_STATE);
            ViewState goalState = viewStates.get(newState, defaultState);

            ViewPropertyAnimatorCompat animator = ViewCompat.animate(animatedView.getView());

            animator.translationX(goalState.mTranslationX);
            animator.translationY(goalState.mTranslationY);
            animator.alpha(goalState.mAlpha);
            animator.scaleX(goalState.mScaleX);
            animator.scaleY(goalState.mScaleY);
            animator.rotation(goalState.mRotation);
            animator.setDuration(durationMs);

            if (first) {
                first = false;
                animator.setListener(listener);
            }

            animator.start();
        }
    }
}
