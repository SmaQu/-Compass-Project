package com.alastor.compassproject.module.gps

import android.app.Dialog
import android.location.Location
import com.google.android.gms.common.api.ResolvableApiException

interface GPSCallback {
    fun onLocationDetect(location: Location)

    fun onGooglePlayServicesOutDate(dialog : Dialog)

    fun onLackOfLocationSettings(resolvableApiException: ResolvableApiException)
}