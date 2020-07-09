package com.alastor.compassproject

interface CompassCallback {

    fun onSensorChanged(degree: Float, azimuth: Float)
}