package com.alastor.compassproject;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView zAxis = findViewById(R.id.tv_x_axis);

        final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        final CompassModule compassModule = new CompassModule(sensorManager, getLifecycle(), degree -> {
            zAxis.setText("Z: " + degree);
        });
    }
}