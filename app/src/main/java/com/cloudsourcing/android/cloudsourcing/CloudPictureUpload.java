// Copyright 2004-present Facebook. All Rights Reserved.

package com.cloudsourcing.android.cloudsourcing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by angelmaredia on 7/8/15.
 */
public class CloudPictureUpload {

    public static void getScaledBitmapB64(byte[] data) {
        // Read in the dimensions of the image on disk
        int scaleRatio = 4;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data,0,data.length,options);
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        float destHeight = srcHeight / scaleRatio;
        float destWidth = srcWidth / scaleRatio;
        //Cloud cloud = new Cloud();

        // Figure out how much to scale down by
        int inSampleSize = 1;
        if (srcHeight > destHeight || srcWidth > destWidth) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round(srcHeight / destHeight);
            } else {
                inSampleSize = Math.round(srcWidth / destWidth);
            }
        }
        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        // Read in and create final bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(data,0, data.length,options);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        CloudController.uploadCloud(base64);
    }
}
