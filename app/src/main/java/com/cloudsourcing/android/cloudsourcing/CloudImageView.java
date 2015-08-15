// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by arbass on 7/21/15.
 *
 * This class encapsulates the functionality of display clouds and handling button presses
 * in the top card view. The view is passes a Cloud to load.  This class should NEVER perform
 * transformer operations directly on the cloud but will instead pass back the flag button press back to
 * CloudFrag through the listener methods.
 */
public class CloudImageView extends CardView {


    //================================================================================
    // Properties
    //================================================================================

    private Context mContext;
    private Cloud mCloud;
    private CloudImageViewListener mListener;


    //================================================================================
    // View Binders
    //================================================================================

    @Bind(R.id.cloud_pic_image_view)
    ImageView mCloudImage;

    @Bind(R.id.flag_button)
    ImageButton mFlagButton;

    @Bind(R.id.cloud_loading_panel)
    ProgressBar mLoadPanel;

    @Bind(R.id.not_found_panel)
    View mNotFoundPanel;

    @Bind(R.id.refresh_button)
    Button mRefreshButton;

    //================================================================================
    // Constructors
    //================================================================================

    public CloudImageView(Context context) {
        this(context, null);
    }

    public CloudImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CloudImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        LayoutInflater.from(mContext)
            .inflate(R.layout.cloud_image_view_layout, this, true);

        ButterKnife.bind(this);

    }

    //================================================================================
    // Interfaces
    //================================================================================


    public interface CloudImageViewListener {
        void onCloudImageLoaded();
        void onCloudImageFailed();
        void onRefreshClicked();
        void onFlagClicked();

    }


    //================================================================================
    // Public Methods
    //================================================================================

    public void loadCloudView(Cloud cloud) {
        mCloud = cloud;
        showLoadingView();

        if (mCloud.getUrl() != null) {
            loadImageFromURL(mCloudImage);
        } else {
            loadBMPImg(mCloudImage);
        }
    }

    public void showNoClouds() {
        mNotFoundPanel.setVisibility(VISIBLE);
        mFlagButton.setVisibility(GONE);
        mLoadPanel.setVisibility(View.GONE);
        mCloudImage.setVisibility(View.GONE);
    }

    public void setListener(CloudImageViewListener listener) {
        mListener = listener;
    }

    //================================================================================
    // Private Methods
    //================================================================================

    private void showLoadingView() {
        mNotFoundPanel.setVisibility(View.GONE);
        mCloudImage.setVisibility(View.GONE);
        mLoadPanel.setVisibility(View.VISIBLE);
        mFlagButton.setVisibility(GONE);
    }

    private void showCloudView() {
        mLoadPanel.setVisibility(View.GONE);
        mNotFoundPanel.setVisibility(View.GONE);
        mCloudImage.setVisibility(View.VISIBLE);
        mFlagButton.setVisibility(VISIBLE);
    }

    public void networkLost(){
        mFlagButton.setEnabled(false);
    }
    public void networkRegained(){
        mFlagButton.setEnabled(true);
    }

    private void loadImageFromURL(ImageView imageView) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(mCloud.getUrl(), imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mListener.onCloudImageFailed();
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                done();

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
    }

    private void loadBMPImg(ImageView imageView) {
        String encodedImage = mCloud.getCloudPic();
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        imageView.setImageBitmap(decodedByte);

        done();

    }

    private void done() {
        showCloudView();
        mListener.onCloudImageLoaded();
    }



    //================================================================================
    // Click Listener
    //================================================================================

    @OnClick (R.id.refresh_button)
    public void refreshButtonClicked() {
        mListener.onRefreshClicked();
    }

    @OnClick (R.id.flag_button)
    public void flagButtonClicked() {
        mListener.onFlagClicked();
    }

}
