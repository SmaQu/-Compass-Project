package com.alastor.compassproject.viewmodel

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alastor.compassproject.module.compass.CompassCallback
import com.alastor.compassproject.module.compass.CompassModule
import com.alastor.compassproject.module.gps.GPSCallback
import com.alastor.compassproject.module.gps.GPSModule
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val FULL_CIRCLE_DEGREE = 360
    }

    private var mAzimuth = 0f;
    private var mCurrentLocation: Location? = null
    private var mDesiredLocation: Location? = null

    val errorGoogleService: LiveData<Dialog>
        get() = errorGoogleServiceData
    private val errorGoogleServiceData = MutableLiveData<Dialog>()

    val errorLackOfSetting: LiveData<ResolvableApiException>
        get() = errorLackOfSettingData
    private val errorLackOfSettingData = MutableLiveData<ResolvableApiException>()

    val compassDirection: LiveData<Float>
        get() = compassDirectionData
    private val compassDirectionData = MutableLiveData<Float>()

    val desiredLocationDirection: LiveData<Float>
        get() = desiredLocationDirectionData
    private val desiredLocationDirectionData = MutableLiveData<Float>()

    var mSelectedLatitude: Double? = null
    var mSelectedLongitude: Double? = null

    var isGPSEnabled = false
    private val mGPSModule: GPSModule by lazy {
        GPSModule(FusedLocationProviderClient(getApplication<Application>().baseContext),
                GoogleApiAvailability.getInstance(), getGPSCallbacks())
    }

    private val mCompassModule = CompassModule((application.getSystemService(Context.SENSOR_SERVICE) as SensorManager),
            object : CompassCallback {
                override fun onSensorDegree(degree: Float) {
                    compassDirectionData.value = degree
                    getArrowDegree()
                }

                override fun onSensorAccelerometerAzimuth(azimuth: Float) {
                    mAzimuth = azimuth
                }
            })

    override fun onCleared() {
        unRegisterCompass()
        unRegisterGPS()
        super.onCleared()
    }

    public fun registerCompass() {
        mCompassModule.registerListener()
    }

    public fun unRegisterCompass() {
        mCompassModule.unregisterListener()
    }

    public fun registerGPS(activity: Activity) {
        mGPSModule.register(activity)
    }

    public fun unRegisterGPS() {
        mGPSModule.unregister()
    }

    public fun isDestinationValid(): Boolean {
        return mSelectedLatitude != null && mSelectedLongitude != null
    }

    private fun getGPSCallbacks(): GPSCallback {
        return object : GPSCallback {
            override fun onLocationDetect(location: Location) {
                isGPSEnabled = true
                mCurrentLocation = location
                errorGoogleServiceData.value = null
                errorLackOfSettingData.value = null
            }

            override fun onGooglePlayServicesOutDate(dialog: Dialog) {
                mCurrentLocation = null
                errorGoogleServiceData.value = dialog
            }

            override fun onLackOfLocationSettings(resolvableApiException: ResolvableApiException) {
                mCurrentLocation = null
                errorLackOfSettingData.value = resolvableApiException
            }
        }
    }

    private fun getArrowDegree() {
        if (isDestinationValid() && mCurrentLocation != null) {
            val geomagneticField = GeomagneticField(mCurrentLocation!!.latitude.toFloat(),
                    mCurrentLocation!!.longitude.toFloat(),
                    mCurrentLocation!!.altitude.toFloat(),
                    System.currentTimeMillis())

            mAzimuth -= geomagneticField.declination
            mDesiredLocation = Location("").apply {
                latitude = mSelectedLatitude as Double
                longitude = mSelectedLongitude as Double
            }
            var bearTo = mCurrentLocation!!.bearingTo(mDesiredLocation)

            if (bearTo < 0) {
                bearTo += FULL_CIRCLE_DEGREE
            }

            var direction = bearTo - mAzimuth


            if (direction < 0) {
                direction += 360
            }

            desiredLocationDirectionData.value = direction
        }
    }
}