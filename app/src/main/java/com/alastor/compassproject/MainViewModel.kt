package com.alastor.compassproject

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
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var mAzimuth = 0f;
    private var mCurrentLocation: Location? = null
    private var mDesireLocation: Location? = null

    val errorGoogleService: LiveData<Dialog>
        get() = errorGoogleServiceData
    private val errorGoogleServiceData = MutableLiveData<Dialog>()

    val errorLackOfSetting: LiveData<ResolvableApiException>
        get() = errorLackOfSettingData
    private val errorLackOfSettingData = MutableLiveData<ResolvableApiException>()

    val compassDirection: LiveData<Float>
        get() = compassDirectionData
    private val compassDirectionData = MutableLiveData<Float>()

    val desireLocationDirection: LiveData<Int>
        get() = desireLocationDirectionData
    private val desireLocationDirectionData = MutableLiveData<Int>()

    var mSelectedLatitude: Double? = null
    var mSelectedLongitude: Double? = null

    var isGPSEnabled = false
    private var mGPSModule: GPSModule? = null
        get() {
            if (field == null) {
                field = GPSModule(FusedLocationProviderClient(getApplication<Application>().baseContext),
                        GoogleApiAvailability.getInstance(), getGPSCallbacks())
            }
            return field
        }

    private val mCompassModule = CompassModule((application.getSystemService(Context.SENSOR_SERVICE) as SensorManager),
            object : CompassCallback {
                override fun onSensorChanged(degree: Float, azimuth: Float) {
                    mAzimuth = azimuth
                    getArrowDegree()

                    compassDirectionData.value = degree
                }
            })

    override fun onCleared() {
        super.onCleared()
        mCompassModule.unregisterListener()
        mGPSModule!!.unregister()
    }

    public fun registerCompass() {
        mCompassModule.registerListener()
    }

    public fun unRegisterCompass() {
        mCompassModule.unregisterListener()
    }

    public fun registerGPS(activity: Activity) {
        mGPSModule!!.register(activity)
    }

    public fun unRegisterGPS() {
        mGPSModule!!.unregister()
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
        if (mSelectedLatitude != null
                && mSelectedLongitude != null
                && mCurrentLocation != null) {
            val geomagneticField = GeomagneticField(mCurrentLocation!!.latitude.toFloat(),
                    mCurrentLocation!!.longitude.toFloat(),
                    mCurrentLocation!!.altitude.toFloat(),
                    System.currentTimeMillis())

            mAzimuth -= geomagneticField.declination
            mDesireLocation = Location("").apply {
                latitude = mSelectedLatitude as Double
                longitude = mSelectedLongitude as Double
            }
            var bearTo = mCurrentLocation!!.bearingTo(mDesireLocation)
            if (bearTo < 0) {
                bearTo += 360
            }

            var direction = bearTo - mAzimuth

            if (direction < 0) {
                direction += 360
            }

            desireLocationDirectionData.value = direction.toInt()
        }
    }
}