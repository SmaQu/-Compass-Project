package com.alastor.compassproject

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.SensorManager
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var mCurrentDegreeDirection = 0
    private var mAzimuth = 0f;
    private var mCurrentLocation: Location? = null
    private var mDesireLocation: Location? = null

    var mSelectedLatitude: Double? = null
        set(value) {
            field = value
            maybeEnableGPS()
        }
    var mSelectedLongitude: Double? = null
        set(value) {
            field = value
            maybeEnableGPS()
        }

    var isGPSEnabled = true
    private var mGPSModule: GPSModule? = null
        get() {
            if (field == null) {
                field = GPSModule(FusedLocationProviderClient(getApplication<Application>().baseContext),
                        GoogleApiAvailability.getInstance(), getGPSCallbacks())
            }
            return field
        }

    val mCompassModule = CompassModule((application.getSystemService(Context.SENSOR_SERVICE) as SensorManager),
            object : CompassCallback {
                override fun onSensorChanged(degree: Int, azimuth: Float) {
                    mCurrentDegreeDirection = degree
                    mAzimuth = azimuth

                    getArrowDegree()
                    Log.e("TAG", "onSensorChanged: $degree")
                }
            })

    override fun onCleared() {
        super.onCleared()
        mCompassModule.unregisterListener()
        mGPSModule!!.unregister()
    }

    public fun enableGPS(activity: Activity) {
        if (isGPSEnabled) {
            mGPSModule!!.register(activity)
        }
    }

    private fun maybeEnableGPS() {
        if (mSelectedLatitude != null && mSelectedLongitude != null) {
            mDesireLocation = Location("").apply {
                latitude = mSelectedLatitude as Double
                longitude = mSelectedLongitude as Double
            }
        }
    }

    private fun getGPSCallbacks(): GPSCallback {
        return object : GPSCallback {
            override fun onLocationDetect(location: Location) {
                mCurrentLocation = location

                //val arrowDegree = mCurrentLocation!!.bearingTo(mDesireLocation)
                //Log.e("TAG", "onLocationDetect: $arrowDegree")
                //TODO("Not yet implemented")
            }

            override fun onGooglePlayServicesOutDate(dialog: Dialog) {
                Log.e("TAG", "onGooglePlayServicesOutDate: ")
                //TODO("Not yet implemented")
            }

            override fun onLackOfLocationSettings() {
                Log.e("TAG", "onLackOfLocationSettings: ")
                //TODO("Not yet implemented")
            }
        }
    }

    private fun getArrowDegree() {
        if (mCurrentLocation != null) {
            val geomagneticField = GeomagneticField(mCurrentLocation!!.latitude.toFloat(),
                    mCurrentLocation!!.longitude.toFloat(),
                    mCurrentLocation!!.altitude.toFloat(),
                    System.currentTimeMillis())

            mAzimuth -= geomagneticField.declination
            mDesireLocation = Location("").apply {
                latitude = 50.148970
                longitude = 19.370210
            }
            var bearTo = mCurrentLocation!!.bearingTo(mDesireLocation)
            if (bearTo < 0) {
                bearTo += 360
            }

            var direction = bearTo - mAzimuth

            if (direction < 0 ) {
                direction += 360
            }

            Log.e("TAG", "direction: $direction")
        }
    }

}