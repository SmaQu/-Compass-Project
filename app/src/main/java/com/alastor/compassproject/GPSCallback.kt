package com.alastor.compassproject

import android.app.Dialog

interface GPSCallback {
    fun showArrow(degree: Int)

    fun onGooglePlayServicesOutDate(dialog : Dialog)

    fun onLackOfLocationSettings()
}