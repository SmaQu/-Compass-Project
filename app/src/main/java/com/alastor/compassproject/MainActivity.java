package com.alastor.compassproject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

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
            public void onAccuracyChanged(@Nullable Sensor sensor, int accuracy) {
                Log.e("TAG", "onAccuracyChanged: ");
            }

            @Override
            public void onSensorChanged(@Nullable SensorEvent event) {
            }
        };
    }
}