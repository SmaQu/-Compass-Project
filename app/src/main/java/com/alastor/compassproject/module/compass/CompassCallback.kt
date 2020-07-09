package com.alastor.compassproject.module.compass

interface CompassCallback {

    fun onSensorDegree(degree: Float)

    fun onSensorAccelerometerAzimuth(azimuth: Float)
}