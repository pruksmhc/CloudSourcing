// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.Bitmap;

import com.facebook.FacebookSdk;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Resources res = getResources();
        final String APPLICATION_ID = res.getString(R.string.parse_app_id);
        final String CLIENT_KEY = res.getString(R.string.parse_client_key);

        FacebookSdk.sdkInitialize(getApplicationContext());
        ParseCrashReporting.enable(this);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, APPLICATION_ID, CLIENT_KEY);
        ParseFacebookUtils.initialize(this);
        ParseObject.registerSubclass(Cloud.class);
        ParseObject.registerSubclass(Achievement.class);
        setupImageLoader();
    }

    public void setupImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true).bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext()).threadPriority(Thread.MAX_PRIORITY).threadPoolSize(5)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache()).denyCacheImageMultipleSizesInMemory().build();
        ImageLoader.getInstance().init(config);
    }
}
