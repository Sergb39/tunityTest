package com.example.sergb.tunitytask;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 501;

    //UI
    private CameraPreview mPreview;
    private FrameLayout preview;
    private TextView colorDataTV;

    private ArrayList<String> dataRecord = new ArrayList<>();

    interface PermissionResult {
        void result(boolean isApproved);
    }

    // callback for the camera permission result
    PermissionResult permissionResult = new PermissionResult() {
        @Override
        public void result(boolean isApproved) {
            if (isApproved) {
                initSurfaceView();
            } else {
                // check run time permission
                checkCameraPermissions();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize views
        initView();

        if (!checkCameraHardware(this)) {
            Toast.makeText(this, "This device not supporting camera use", Toast.LENGTH_LONG).show();
            return;
        }

        // check run time permission
        checkCameraPermissions();
    }

    private void initView() {
        preview = findViewById(R.id.camera_preview);
        colorDataTV = findViewById(R.id.colorData);
    }

    private void initSurfaceView() {
        mPreview = new CameraPreview(this, new CameraPreview.DecodeCallBack() {
            @Override
            public void call(int rgb) {
                // callback from CameraPreview class after decoding preview frame

                // update UI with new color
                colorDataTV.setBackgroundColor(rgb);
                colorDataTV.setText("#" + Integer.toHexString(rgb));

                //add color to color list
                saveData(Integer.toHexString(rgb));
            }
        });
        preview.addView(mPreview);
    }

    public void fabClick(View view) {
        FloatingActionButton fab = (FloatingActionButton) view;
        //first click. after first click the flag is raised telling the preview frame callback to decode the image
        if (!mPreview.isRecordingData()) {
            mPreview.setRecordingData(true);

            // set new icon tn fab
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_send, getTheme()));
            } else {
                fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_send));
            }
            return;
        }

        // rest fab button
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play, getTheme()));
        } else {
            fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
        }
        // stop preview frame decoding
        mPreview.setRecordingData(false);

        // open list activity to show the collected data
        Intent intent = new Intent(this, DataPreviewActivity.class);
        intent.putExtra("DataArray", dataRecord.toArray(new String[dataRecord.size()]));
        startActivity(intent);
    }

    /**
     * saves color to array list, max data count is set to 200 items.
     * saving working in fifo style, first in first out.
     * @param pixelColor color of the pixel to save to data set
     */
    private void saveData(String pixelColor) {
        dataRecord.add(pixelColor);
        if (dataRecord.size() > 200) {
            dataRecord.remove(0);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public void checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA
            );
        } else {
            initSurfaceView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionResult.result(true);
                } else {
                    permissionResult.result(false);
                }
                return;
            }
        }
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }


}
