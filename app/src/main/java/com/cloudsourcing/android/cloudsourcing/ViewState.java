package com.cloudsourcing.android.cloudsourcing;

import android.content.res.Resources;
import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * A set of values for a view including position, rotation, alpha, etc. used by StateAnimator
 */
public class ViewState {
    public static class Builder {
        int[] mIds;
        float mAlpha;
        float mTranslationX;
        float mTranslationY;
        float mScaleX;
        float mScaleY;
        float mRotationClockwiseDegrees;
        Resources mResources;

        private AnimatedView.Builder mParentBuilder;

        /* package */ Builder(AnimatedView.Builder parentBuilder, View view, int[] ids) {
            mResources = view.getResources();
            mAlpha = ViewCompat.getAlpha(view);
            mTranslationX = ViewCompat.getTranslationX(view);
            mTranslationY = ViewCompat.getTranslationY(view);
            mScaleX = ViewCompat.getScaleX(view);
            mScaleY = ViewCompat.getScaleY(view);
            mRotationClockwiseDegrees = ViewCompat.getRotation(view);
            mIds = ids;
            mParentBuilder = parentBuilder;
        }

        public StateAnimator build() {
            finish();
            return mParentBuilder.build();
        }

        public AnimatedView.Builder addView(View v) {
            finish();
            return mParentBuilder.addView(v);
        }

        public Builder defineState(int... id) {
            finish();
            return mParentBuilder.defineState(id);
        }

        void finish() {
            mParentBuilder.addCompletedState(this.mIds, new ViewState(this));
        }

        public Builder alpha(float alpha) {
            mAlpha = alpha;
            return this;
        }

        public Builder translationXDimen(int dimenId) {
            float valueInPx = mResources.getDimension(dimenId);
            mTranslationX = valueInPx;
            return this;
        }

        public Builder translationYDimen(int dimenId) {
            float valueInPx = mResources.getDimension(dimenId);
            mTranslationY = valueInPx;
            return this;
        }

        public Builder translationXPx(float translationX) {
            mTranslationX = translationX;
            return this;
        }

        public Builder translationYPx(float translationY) {
            mTranslationY = translationY;
            return this;
        }

        public Builder scaleX(float scaleX) {
            mScaleX = scaleX;
            return this;
        }

        public Builder scaleY(float scaleY) {
            mScaleY = scaleY;
            return this;
        }

        public Builder rotationClockwise(float degrees) {
            mRotationClockwiseDegrees = degrees;
            return this;
        }
    }

    float mAlpha;
    float mTranslationX;
    float mTranslationY;
    float mScaleX;
    float mScaleY;
    float mRotation;

    private ViewState(Builder builder) {
        this.mAlpha = builder.mAlpha;
        this.mTranslationX = builder.mTranslationX;
        this.mTranslationY = builder.mTranslationY;
        this.mScaleX = builder.mScaleX;
        this.mScaleY = builder.mScaleY;
        this.mRotation = builder.mRotationClockwiseDegrees;
    }
}
