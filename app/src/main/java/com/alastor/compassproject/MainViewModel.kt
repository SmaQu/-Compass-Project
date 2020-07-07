package com.alastor.compassproject

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.hardware.SensorManager

import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val mGPSModule = GPSModule(0f, 0f,
            FusedLocationProviderClient(application.baseContext), GoogleApiAvailability.getInstance(),
            object : GPSCallback {
                override fun showArrow(degree: Int) {
                    TODO("Not yet implemented")
                }

                override fun onGooglePlayServicesOutDate(dialog: Dialog) {
                    TODO("Not yet implemented")
                }

                override fun onLackOfLocationSettings() {
                    TODO("Not yet implemented")
                }
            })

    private val mCompassModule = CompassModule((application.getSystemService(Context.SENSOR_SERVICE) as SensorManager),
            object : CompassCallback {
                override fun onSensorChanged(degree: Int) {
                    TODO("Not yet implemented")
                }
            })

    public fun registerListeners(activity: Activity) {
        mGPSModule.register(activity)
        mCompassModule.registerListener()
    }

    public fun unregisterListeners() {
        mCompassModule.unregisterListener()
    }

    override fun onCleared() {
        super.onCleared()
        unregisterListeners()
    }
}