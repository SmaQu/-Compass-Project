package com.alastor.compassproject.module.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowSensor;
import org.robolectric.shadows.ShadowSensorManager;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@SmallTest
public class CompassModuleTest {

    private static final float EXPECTED_AZIMUTH = 1f;
    private static final float EXPECTED_DEGREE = 294.3132f;

    private SensorManager sensorManager;
    private ShadowSensorManager shadowSensorManager;

    @Before
    public void setUp() {
        sensorManager = (SensorManager) ApplicationProvider
                .getApplicationContext()
                .getSystemService(Context.SENSOR_SERVICE);
        shadowSensorManager = shadowOf(sensorManager);
        shadowSensorManager.addSensor(ShadowSensor.newInstance(Sensor.TYPE_ACCELEROMETER));
        shadowSensorManager.addSensor(ShadowSensor.newInstance(Sensor.TYPE_MAGNETIC_FIELD));
    }

    @Test
    public void isAzimuth_return_from_callback() throws Exception {
        final CompassCallback compassCallback = new CompassCallback() {
            @Override
            public void onSensorDegree(float degree) {
            }

            @Override
            public void onSensorAccelerometerAzimuth(float azimuth) {
                assertEquals(EXPECTED_AZIMUTH, azimuth, 0);
            }
        };

        CompassModule compassModule = new CompassModule(sensorManager, compassCallback);
        compassModule.registerListener();
        shadowSensorManager.sendSensorEventToListeners(getAccelerometerEventWithValues());
    }

    @Test
    public void getDegree() throws Exception {
        final CompassCallback compassCallback = new CompassCallback() {
            @Override
            public void onSensorDegree(float degree) {
                assertEquals(EXPECTED_DEGREE, degree,0);
            }

            @Override
            public void onSensorAccelerometerAzimuth(float azimuth) {
            }
        };
        CompassModule compassModule = new CompassModule(sensorManager, compassCallback);
        compassModule.registerListener();
        shadowSensorManager.sendSensorEventToListeners(getAccelerometerEventWithValues());
        shadowSensorManager.sendSensorEventToListeners(getMagneticFieldEventWithValues());
    }

    private SensorEvent getAccelerometerEventWithValues() throws Exception {
        SensorEvent sensorEvent = Mockito.mock(SensorEvent.class);

        Field sensorField = SensorEvent.class.getField("sensor");
        sensorField.setAccessible(true);
        Sensor sensor = Mockito.mock(Sensor.class);
        when(sensor.getType()).thenReturn(Sensor.TYPE_ACCELEROMETER);
        sensorField.set(sensorEvent, sensor);

        Field valuesField = SensorEvent.class.getField("values");
        valuesField.setAccessible(true);
        float[] desiredValues = new float[]{EXPECTED_AZIMUTH, 100f,200f};
        valuesField.set(sensorEvent, desiredValues);

        return sensorEvent;
    }

    private SensorEvent getMagneticFieldEventWithValues() throws Exception {
        SensorEvent sensorEvent = Mockito.mock(SensorEvent.class);

        Field sensorField = SensorEvent.class.getField("sensor");
        sensorField.setAccessible(true);
        Sensor sensor = Mockito.mock(Sensor.class);
        when(sensor.getType()).thenReturn(Sensor.TYPE_MAGNETIC_FIELD);
        sensorField.set(sensorEvent, sensor);

        Field valuesField = SensorEvent.class.getField("values");
        valuesField.setAccessible(true);
        float[] desiredValues = new float[]{100f, 200f, 300f};
        valuesField.set(sensorEvent, desiredValues);

        return sensorEvent;
    }
}
