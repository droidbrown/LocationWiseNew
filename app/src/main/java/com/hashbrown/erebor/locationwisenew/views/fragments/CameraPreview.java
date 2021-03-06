package com.hashbrown.erebor.locationwisenew.views.fragments;

/**
 * Created by Erebor on 06/06/17.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;

/**
 * A basic Camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "camera";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    Context context;
    int cameraId;

    public CameraPreview(Context context, Camera camera, int camid) {
        super(context);
        this.context = context;
        mCamera = camera;
        cameraId = camid;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.

        // mCamera.setPreviewDisplay(holder);
        if(mCamera!=null)
        mCamera.startPreview();

    }


    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        this.setCameraDisplayOrientation(this.cameraId, this.mCamera);
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {

            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {

            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            mCamera.startPreview();
            mCamera.release();
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    /**
     * Calling this method makes the camera image show in the same orientation as the display.
     * NOTE: This method is not allowed to be called during preview.
     *
     * @param cameraId
     * @param camera
     */
    @SuppressLint("NewApi")
    public void setCameraDisplayOrientation(int cameraId, android.hardware.Camera camera) {
        int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            android.hardware.Camera.CameraInfo info =
                    new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(cameraId, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
        } else {
            // on API 8 and lower devices
            if (context.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                result = 90;
            } else {
                result = 0;
            }
        }
        try {
            camera.setDisplayOrientation(result);
        } catch (Exception e) {
            // may fail on old OS versions. ignore it.
            e.printStackTrace();
        }
    }

}
