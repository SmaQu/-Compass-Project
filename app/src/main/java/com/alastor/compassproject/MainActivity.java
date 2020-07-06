package com.alastor.compassproject;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        final CompassModule compassModule = new CompassModule(sensorManager, getLifecycle(), getCompassCallback());
    }

    private CompassCallback getCompassCallback() {
        return new CompassCallback() {
            @Override
            public void onAccuracyChanged(@org.jetbrains.annotations.Nullable Sensor sensor, int accuracy) {
                Log.e("TAG", "onAccuracyChanged: " );
            }

            @Override
            public void onSensorChanged(@org.jetbrains.annotations.Nullable SensorEvent event) {
                Log.e("TAG", "onSensorChanged: " );
            }
        };
    }
}