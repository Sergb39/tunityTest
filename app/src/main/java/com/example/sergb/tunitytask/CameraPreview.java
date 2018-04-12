package com.example.sergb.tunitytask;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import static android.content.ContentValues.TAG;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    private FrameDecoder frameDecoder;
    private DecodeCallBack callBack;
    private Handler UIHandler;


    private CameraHandlerThread mThread = null;
    private boolean isRecordingData;

    interface DecodeCallBack {
        void call(int rgb);
    }

    public CameraPreview(Context context, DecodeCallBack callBack) {
        super(context);
        this.callBack = callBack;
        UIHandler = new Handler(Looper.getMainLooper());

        frameDecoder = new FrameDecoder();
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now we will initialize the camera
        openCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

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
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }


    // loading the camera in separate thread to avoid blocking the ui thread.
    private void openCamera() {
        if (mThread == null) {
            mThread = new CameraHandlerThread();
        }

        synchronized (mThread) {
            mThread.openCamera();
        }
    }


    private class CameraHandlerThread extends HandlerThread {
        Handler mHandler = null;

        CameraHandlerThread() {
            super("CameraHandlerThread");
            start();
            mHandler = new Handler(getLooper());
        }

        synchronized void notifyCameraOpened() {
            notify();
        }

        void openCamera() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    initCamera();
                    notifyCameraOpened();
                }
            });
            try {
                wait();
            } catch (InterruptedException e) {
                Log.w(TAG, "wait was interrupted");
            }
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private void initCamera() {
        mCamera = getCameraInstance();
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.setDisplayOrientation(270);
            mCamera.setPreviewCallback(this);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
        // Create our Preview view and set it as the content of our activity.
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (!isRecordingData) return;

        int height = camera.getParameters().getPreviewSize().height;
        int width = camera.getParameters().getPreviewSize().width;
        int[] RGB = frameDecoder.decodeYUV420SP(data, width, height);
        final int pixelColor = frameDecoder.getRGBColorFromCentralPixel(frameDecoder.createBitmapFromRGBArray(RGB, width, height));

        // post data to ui thread
        UIHandler.post(new Runnable() {
            @Override
            public void run() {

                callBack.call(pixelColor);
            }
        });
    }

    public void setRecordingData(boolean recordingData) {
        isRecordingData = recordingData;
    }

    public boolean isRecordingData() {
        return isRecordingData;
    }
}
