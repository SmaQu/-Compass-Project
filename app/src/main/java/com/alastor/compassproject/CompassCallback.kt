package com.alastor.compassproject

interface CompassCallback {

    fun onSensorChanged(degree: Int, azimuth: Float)
}