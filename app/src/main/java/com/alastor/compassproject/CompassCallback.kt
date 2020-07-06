package com.alastor.compassproject

import android.hardware.Sensor
import android.hardware.SensorEvent

interface CompassCallback {

    fun onAccuracyChanged(sensor: Sensor, accuracy: Int)

    fun onSensorChanged(event: SensorEvent)
}