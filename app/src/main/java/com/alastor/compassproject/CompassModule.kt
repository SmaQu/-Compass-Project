package com.alastor.compassproject

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class CompassModule(private val mSensorManager: SensorManager,
                    private val mLifecycle: Lifecycle,
                    private val callback: CompassCallback) : SensorEventListener, LifecycleObserver {

    private val mAccelerometer: Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    private val mMagnetometer: Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    init {
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
        if (mLifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            sensor?.apply {
                callback.onAccuracyChanged(this, accuracy)
            }
        }
    }

     private var mGravity: FloatArray? = null
     private var mGeomagnetic: FloatArray? = null

    override fun onSensorChanged(event: SensorEvent?) {
        if (mLifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            event?.also {
                callback.onSensorChanged(it)

                if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    mGravity = it.values
                }
                if (it.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    mGeomagnetic = it.values
                }

                val R = FloatArray(9)
                val I = FloatArray(9)

                if (mGravity != null && mGeomagnetic != null) {
                    val success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)
                    if (success) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(R, orientation)
                        Log.e("TAG", "onSensorChanged: ${orientation[0]}")
                    }
                }

            }
        }
    }
}