package com.cloudsourcing.android.cloudsourcing;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by angelmaredia on 7/23/15.
 * <p/>
 * This class wires up the CameraPreview to a view. Here, we get an instance of a camera
 * and then create a surface for it in CameraPreview. This class also handles the UI
 * of the camera and tells the CameraPreview how to respond to device configuration changes.
 */

public class CameraActivity extends AppCompatActivity
        implements AchievementDialog.OnDialogInteractionListener {

    //================================================================================
    // Properties
    //================================================================================

    Camera mCamera;
    CameraPreview mPreview;
    public ImageView mCapturedImage;
    Bitmap mUploadedPic;
    boolean mPause;
    boolean mPictureTaken;
    private int mDialogCount;

    //================================================================================
    // View Binders
    //================================================================================

    @Bind(R.id.camera_preview)
    FrameLayout mCameraLayout;

    @Bind(R.id.button_capture)
    Button mCaptureButton;

    @Bind(R.id.confirmation_bar)
    LinearLayout mConfirmationBar;

    @Bind(R.id.retry_button)
    Button mRetryButton;

    @Bind(R.id.submit_button)
    Button mSubmitButton;

    @Bind(R.id.toolbar_view)
    FrameLayout mToolbarView;

    @Bind(R.id.rules_text)
    TextView mRulesText;

    //================================================================================
    // Lifecycle
    //================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        setCameraDisplayOrientation(CameraActivity.this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);

        // Create preview and store it in the activity
        mPreview = new CameraPreview(this, mCamera);
        mCameraLayout.addView(mPreview);

        textboxAttributes();
        mPause = false;
        mPictureTaken = false;
        mUploadedPic= null;
        mCapturedImage=null;
    }

    //Camera must always be released so that it can be used in other applications
    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        mPause = true;

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPause && !mPictureTaken) {
            mCameraLayout.removeView(mPreview);
            mCamera = getCameraInstance();
            setCameraDisplayOrientation(CameraActivity.this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
            mPreview = new CameraPreview(this, mCamera);
            mCameraLayout.addView(mPreview);
            mPause = false;
        } else if (mPause && mPictureTaken) {
            mCamera = getCameraInstance();
            setCameraDisplayOrientation(CameraActivity.this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
            mPreview = new CameraPreview(this, mCamera);
            mPause = false;
        }
    }

    //================================================================================
    // Click Listeners
    //================================================================================

    @OnClick(R.id.button_capture)
    public void mCaptureButtonClicked() {
        mCamera.takePicture(null, null, mPicture);
        mPictureTaken = true;
        mCaptureButton.setEnabled(false);
    }

    @OnClick(R.id.submit_button)
    public void mSubmitButtonClicked() {
        //This should upload the picture onto parse, but I'm not enabling this until everything
        //else works

        mRetryButton.setEnabled(false);
        new Thread(new Runnable() {
            public void run() {
                int toolbarHeight = mToolbarView.getHeight();
                int imageHeight = mUploadedPic.getWidth() * mUploadedPic.getWidth() / mUploadedPic.getHeight();
                mPictureTaken = false;

                mUploadedPic = Bitmap.createBitmap(mUploadedPic, 0, toolbarHeight, mUploadedPic.getWidth(), imageHeight);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mUploadedPic.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] data = stream.toByteArray();
                CloudPictureUpload.getScaledBitmapB64(data);
            }
        }).start();

        ParseUser user = ParseUser.getCurrentUser();
        AchievementQualifiers qualifiers = AchievementQualifiers.getQualifiers(user);
        List<AchievementQualifiers.UpdateActions> updateActions = new ArrayList<>();
        updateActions.add(AchievementQualifiers.UpdateActions.INCR_CLOUDS_UPLOADED);
        qualifiers.updateQualifiers(updateActions);
        AchievementQualifiers.setQualifiers(user, qualifiers);
        List<Achievement> newAchievements = AchievementHandler.get().fetchNewAchievements(qualifiers, user);
        mDialogCount = newAchievements.size();

        for (Achievement achievement : newAchievements) {
            AchievementDialog dialog = AchievementDialog.newInstance(achievement, this);
            dialog.show(getSupportFragmentManager(), achievement.getName());
        }
        // If there are no dialogs created for achievements
        if (mDialogCount == 0) {
            setResult(RESULT_OK);
            finish();
        }
    }

    @OnClick(R.id.retry_button)
    public void mRetryButtonClicked() {
        mUploadedPic = null;
        mSubmitButton.setEnabled(false);
        mConfirmationBar.setVisibility(View.GONE);
        mCapturedImage.setImageBitmap(null);
        mCameraLayout.removeView(mCapturedImage);
        mCameraLayout.addView(mPreview);
        mCaptureButton.setVisibility(View.VISIBLE);
        mPictureTaken = false;
        mCaptureButton.setEnabled(true);
    }

    //================================================================================
    // Callback for Picture Taken
    //================================================================================

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        //When a picture is taken it'll replace the camera view
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mCapturedImage = displayImage(data);
            mCaptureButton.setVisibility(View.VISIBLE);
            mConfirmationBar.setVisibility(View.VISIBLE);
            mCameraLayout.removeView(mPreview);
            mCameraLayout.addView(mCapturedImage);
            mRetryButton.setEnabled(true);
            mSubmitButton.setEnabled(true);
        }
    };

    //================================================================================
    // Private Methods
    //================================================================================

    //decodes the data to a bitmap and creates an imageview with that bitmap
    public ImageView displayImage(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        //handles rotation of image after picture is taken
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            android.graphics.Matrix mtx = new android.graphics.Matrix();
            mtx.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mtx, true);
        }
        ImageView capturedImage = new ImageView(this);
        capturedImage.setImageBitmap(bitmap);
        mUploadedPic = bitmap;
        capturedImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return capturedImage;
    }


    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    public boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {

        }
        return c; // returns null if camera is unavailable
    }

    //orients the camera frame according to the phone orientation
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void textboxAttributes() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        int captureHeight = screenWidth * screenWidth / screenHeight;

        Resources r = getResources();
        int toolbarHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, r.getDisplayMetrics());
        int captureButtonHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, r.getDisplayMetrics());

        int rulesHeight = screenHeight - toolbarHeight - captureButtonHeight - captureHeight;
        mRulesText.setHeight(rulesHeight - 100);

        mRulesText.setText(Html.fromHtml(
                "<b><h1>" + "Want more points?" + "</h1></b>" +
                        "<b><h4>" + "Upload a Cloud Picture" + "</h4></b>" +
                        "<body>" + "Frame your cloud in the box above" + "</body>" +
                        "<br />" + "<body>" + "Click 'Capture Cloud'" + "</body>" +
                        "<br />" + "<body>" + "And your cloud is ready to be gazed!" + "</body>" +
                        "<br />" + "<h4>" + "Please only upload pictures of clouds" + "</h4>" +
                        "<body>" + "Don't be that guy who ruins cloud gazing." + "</body>" +
                        "<br />" + "<body>" + "We WILL ban you." + "</body>"));
    }

    @Override
    public void onDialogButtonPressed() {
        mDialogCount--;
        if (mDialogCount == 0) {
            setResult(RESULT_OK);
            finish();
        }

    }
}
