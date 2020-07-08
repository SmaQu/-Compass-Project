package com.alastor.compassproject

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlin.math.roundToInt

class CompassModule(private val mSensorManager: SensorManager,
                    private val mCallback: CompassCallback) : SensorEventListener, LifecycleObserver {

    private val mAccelerometer: Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    private val mMagnetometer: Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    // Current data from accelerometer & magnetometer.  The arrays hold values
    // for X, Y, and Z.
    private var mGravity: FloatArray? = null
    private var mGeomagnetic: FloatArray? = null

    private var mLifecycle: Lifecycle? = null;

    constructor(mSensorManager: SensorManager,
                mLifecycle: Lifecycle,
                callback: CompassCallback) : this(mSensorManager, callback) {
        this.mLifecycle = mLifecycle
        mLifecycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun registerListener() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun unregisterListener() {
        mSensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                mGravity = it.values
            }
            if (it.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic = it.values
            }

            val rotationMatrix = FloatArray(9)

            if (mGravity != null && mGeomagnetic != null) {
                val success = SensorManager.getRotationMatrix(rotationMatrix, null, mGravity, mGeomagnetic)
                if (success) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    var degree: Int = Math.toDegrees(orientation[0].toDouble()).roundToInt()
                    if (degree < 0) {
                        degree += 360
                    }
                    mCallback.onSensorChanged(degree, it.values[0])
                }
            }
        }
    }
}