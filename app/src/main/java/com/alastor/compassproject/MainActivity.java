package com.alastor.compassproject;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.alastor.compassproject.viewmodel.MainViewModel;
import com.google.android.gms.common.api.ResolvableApiException;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private MainViewModel mMainViewModel;
    private float currentNeedleDegree = 0f;
    private int currentDirectionDegree = 0;
    private ImageView compassIv;
    private ImageView destinationArrowIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compassIv = findViewById(R.id.image_compass);
        destinationArrowIv = findViewById(R.id.image_destination_arrow);

        mMainViewModel = new ViewModelProvider(this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(MainViewModel.class);

        mMainViewModel.getCompassDirection().observe(this, this::updateCompassDirection);
        mMainViewModel.getDesireLocationDirection().observe(this, this::updateDesireDirection);
        mMainViewModel.getErrorGoogleService().observe(this, this::showGoogleServiceErrorDialog);
        mMainViewModel.getErrorLackOfSetting().observe(this, this::showLacOfSettingsErrorDialog);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainViewModel.registerCompass();
        enableGPS();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMainViewModel.unRegisterCompass();
        mMainViewModel.unRegisterGPS();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mMainViewModel.registerGPS(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == -1) {
                    mMainViewModel.registerGPS(this);
                }
        }
    }

    private void enableGPS() {
        if (mMainViewModel.isGPSEnabled() && arePermissionsGranted()) {
            mMainViewModel.registerGPS(this);
        }
    }

    private boolean arePermissionsGranted() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void showGoogleServiceErrorDialog(Dialog dialog) {
        if (dialog != null)
            dialog.show();
    }

    private void showLacOfSettingsErrorDialog(ResolvableApiException resolvableApiException) {
        if (resolvableApiException != null) {
            try {
                resolvableApiException.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
            } catch (IntentSender.SendIntentException ignored) {
            }
        }
    }

    private void updateCompassDirection(float direction) {
        currentNeedleDegree = -direction;
        RotateAnimation rotateAnimation = new RotateAnimation(currentNeedleDegree, -direction,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        compassIv.startAnimation(rotateAnimation);
    }

    private void updateDesireDirection(int direction) {
        destinationArrowIv.setVisibility(View.VISIBLE);
        currentDirectionDegree = direction;
        ConstraintLayout.LayoutParams layoutParams
                = (ConstraintLayout.LayoutParams) destinationArrowIv.getLayoutParams();
        layoutParams.circleAngle = direction;
        destinationArrowIv.setLayoutParams(layoutParams);

        RotateAnimation rotateAnimation = new RotateAnimation(currentDirectionDegree, direction,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        destinationArrowIv.startAnimation(rotateAnimation);
    }
}