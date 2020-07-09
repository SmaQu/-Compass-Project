package com.alastor.compassproject.module.compass;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;

import static org.junit.Assert.fail;

public class CompassModuleTest {


    @Test
    public void name() {
        SensorManager sensorManager = (SensorManager) ApplicationProvider.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        CompassModule compassModule = new CompassModule(sensorManager, new CompassCallback() {
            @Override
            public void onSensorDegree(float degree) {

            }

            @Override
            public void onSensorAccelerometerAzimuth(float azimuth) {

            }
        });
        compassModule.registerListener();
    }
}
