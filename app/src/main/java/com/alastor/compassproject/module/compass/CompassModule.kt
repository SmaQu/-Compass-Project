package com.alastor.compassproject.module.compass

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver

class CompassModule(private val sensorManager: SensorManager,
                    private val callback: CompassCallback) : SensorEventListener, LifecycleObserver {

    companion object {
        private const val FULL_CIRCLE_DEGREE = 360
    }

    private val accelerometer: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    private val magnetometer: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    // Current data from accelerometer & magnetometer.  The arrays hold values
    // for X, Y, and Z.
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    fun registerListener() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unregisterListener() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //no-op
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                gravity = it.values
                callback.onSensorAccelerometerAzimuth(it.values[0])
            }
            if (it.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = it.values
            }
            val rotationMatrix = FloatArray(9)

            if (gravity != null && geomagnetic != null) {
                val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
                if (success) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    var degree: Float = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    if (degree < 0) {
                        degree += FULL_CIRCLE_DEGREE
                    }

                    callback.onSensorDegree(degree)
                }
            }
        }
    }
}