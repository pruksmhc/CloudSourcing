package com.cloudsourcing.android.cloudsourcing;

import android.util.SparseArray;
import android.view.View;

import com.google.common.base.Preconditions;

/**
 * A View that's controlled by this StateAnimator.
 *
 * Includes a map of {stateId : ViewState} which defines all the states that this view can be
 * animated between. All views automatically have a DEFAULT_STATE defined which uses the values
 * at the time that the StateAnimator was created.
 */
public class AnimatedView {

    public static class Builder {
        private StateAnimator.Builder mParentBuilder;
        private View mView;
        private SparseArray<ViewState> mViewStates = new SparseArray<>();

        /* package */ Builder(StateAnimator.Builder parentBuilder, View view) {
            mParentBuilder = parentBuilder;
            mView = view;

            // When a new view is added, automatically build and add the default state based on the
            // current values of all of these properties.
            // (finish() also adds the state to the state list)
            new ViewState.Builder(this, mView, new int[]{0}).finish();
        }

        public Builder addView(View v) {
            Preconditions.checkNotNull(v, "Cannot animate a null view");
            finish();
            return mParentBuilder.addView(v);
        }

        private void finish() {
            Preconditions.checkState(
                    mViewStates.size() > 1,
                    "Must define at least one state for animated views other than the default.");

            mParentBuilder.addCompletedView(new AnimatedView(this));
        }

        public StateAnimator build() {
            finish();
            return mParentBuilder.build();
        }

        public ViewState.Builder defineState(int... id) {
            return new ViewState.Builder(this, mView, id);
        }

        /* package */ void addCompletedState(int[] ids, ViewState viewState) {
            for (int id : ids) {
                if (mViewStates.get(id) != null) {
                    throw new IllegalStateException("Cannot define a state twice.");
                }
                mViewStates.put(id, viewState);
            }
        }
    }

    private View mView;
    private SparseArray<ViewState> mViewStates;

    public AnimatedView(Builder builder) {
        mView = builder.mView;
        mViewStates = builder.mViewStates;
    }

    public SparseArray<ViewState> getViewStates() {
        return mViewStates;
    }

    public View getView() {
        return mView;
    }
}
