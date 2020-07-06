package com.alastor.compassproject

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class CompassModule(private val mSensorManager: SensorManager,
                    private val mLifecycle: Lifecycle,
                    private val callback: CompassCallback) : SensorEventListener, LifecycleObserver {

    private val mSensor: Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    init {
        mLifecycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun registerListener() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun unregisterListener() {
        mSensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (mLifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
            callback.onAccuracyChanged(sensor, accuracy)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (mLifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
            callback.onSensorChanged(event)
    }
}