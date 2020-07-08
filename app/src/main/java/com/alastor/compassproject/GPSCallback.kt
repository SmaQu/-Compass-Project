package com.alastor.compassproject

import android.app.Dialog
import android.location.Location

interface GPSCallback {
    fun onLocationDetect(location: Location)

    fun onGooglePlayServicesOutDate(dialog : Dialog)

    fun onLackOfLocationSettings()
}