package com.alastor.compassproject;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.alastor.compassproject.dialog.LocationPickDialog;
import com.alastor.compassproject.viewmodel.MainViewModel;
import com.google.android.gms.common.api.ResolvableApiException;

import org.jetbrains.annotations.NotNull;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements LocationPickDialog.NotifyDialogListener {

    private static final String KEY_LATITUDE = "key_latitude";
    private static final String KEY_LONGITUDE = "key_longitude";
    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private MainViewModel mMainViewModel;
    private float currentNeedleDegree = 0f;
    private float currentDirectionDegree = 0f;
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

        findViewById(R.id.button_latitude).setOnClickListener(v -> {
            openLocationDialog(KEY_LATITUDE, R.string.latitude);
        });
        findViewById(R.id.button_longitude).setOnClickListener(v -> {
            openLocationDialog(KEY_LONGITUDE, R.string.longitude);

        });

        mMainViewModel.getCompassDirection().observe(this, this::updateCompassDirection);
        mMainViewModel.getDesiredLocationDirection().observe(this, this::updateDesiredDirection);
        mMainViewModel.getErrorGoogleService().observe(this, this::showGoogleServiceErrorDialog);
        mMainViewModel.getErrorLackOfSetting().observe(this, this::showLacOfSettingsErrorDialog);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainViewModel.registerCompass();
        if (mMainViewModel.isGPSEnabled())
            registerGPSIfAllowed();
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
            registerGPSIfAllowed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == -1) {
            registerGPSIfAllowed();
        }
    }

    @Override
    public void onDialogResponse(@NotNull String requestKey, double locationValue) {
        switch (requestKey) {
            case KEY_LATITUDE:
                mMainViewModel.setMSelectedLatitude(locationValue);
                break;
            case KEY_LONGITUDE:
                mMainViewModel.setMSelectedLongitude(locationValue);
                break;
        }
        registerGPSIfAllowed();
    }

    private void openLocationDialog(String key, @StringRes int title) {
        LocationPickDialog locationPickDialog = LocationPickDialog.create(getString(title), key);
        locationPickDialog.show(getSupportFragmentManager(), LocationPickDialog.TAG);
    }

    private void registerGPSIfAllowed() {
        if (mMainViewModel.isDestinationValid()
                && arePermissionsGranted()) {
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
        float revertedDirection = -direction;
        currentNeedleDegree = revertedDirection;
        compassIv.startAnimation(getRotateAnimation(currentNeedleDegree, revertedDirection));
    }

    private void updateDesiredDirection(float direction) {
        float realDirection = currentNeedleDegree + direction;
        destinationArrowIv.setVisibility(View.VISIBLE);
        currentDirectionDegree = realDirection;

        ConstraintLayout.LayoutParams layoutParams
                = (ConstraintLayout.LayoutParams) destinationArrowIv.getLayoutParams();
        layoutParams.circleAngle = realDirection;
        destinationArrowIv.setLayoutParams(layoutParams);


        destinationArrowIv.startAnimation(getRotateAnimation(currentDirectionDegree, realDirection));
    }

    private RotateAnimation getRotateAnimation(float currentDirection, float direction) {
        RotateAnimation rotateAnimation = new RotateAnimation(currentDirection, direction,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(500);
        rotateAnimation.setFillAfter(true);
        return rotateAnimation;
    }
}