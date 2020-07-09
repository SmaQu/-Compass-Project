package com.alastor.compassproject.module.compass

interface CompassCallback {

    fun onSensorChanged(degree: Float, azimuth: Float)
}