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

    private var azimuth = 0f;
    private var currentLocation: Location? = null
    private var desiredLocation: Location? = null

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

    var selectedLatitude: Double? = null
    var selectedLongitude: Double? = null

    var isGPSEnabled = false
    private val gpsModule: GPSModule by lazy {
        GPSModule(FusedLocationProviderClient(getApplication<Application>().baseContext),
                GoogleApiAvailability.getInstance(), getGPSCallbacks())
    }

    private val compassModule = CompassModule((application.getSystemService(Context.SENSOR_SERVICE) as SensorManager),
            object : CompassCallback {
                override fun onSensorDegree(degree: Float) {
                    compassDirectionData.value = degree
                    getArrowDegree()
                }

                override fun onSensorAccelerometerAzimuth(azimuth: Float) {
                    this@MainViewModel.azimuth = azimuth
                }
            })

    override fun onCleared() {
        unRegisterCompass()
        unRegisterGPS()
        super.onCleared()
    }

    public fun registerCompass() {
        compassModule.registerListener()
    }

    public fun unRegisterCompass() {
        compassModule.unregisterListener()
    }

    public fun registerGPS(activity: Activity) {
        gpsModule.register(activity)
    }

    public fun unRegisterGPS() {
        gpsModule.unregister()
    }

    public fun isDestinationValid(): Boolean {
        return selectedLatitude != null && selectedLongitude != null
    }

    private fun getGPSCallbacks(): GPSCallback {
        return object : GPSCallback {
            override fun onLocationDetect(location: Location) {
                isGPSEnabled = true
                currentLocation = location
                errorGoogleServiceData.value = null
                errorLackOfSettingData.value = null
            }

            override fun onGooglePlayServicesOutDate(dialog: Dialog) {
                currentLocation = null
                errorGoogleServiceData.value = dialog
            }

            override fun onLackOfLocationSettings(resolvableApiException: ResolvableApiException) {
                currentLocation = null
                errorLackOfSettingData.value = resolvableApiException
            }
        }
    }

    private fun getArrowDegree() {
        if (isDestinationValid() && currentLocation != null) {
            val geomagneticField = GeomagneticField(currentLocation!!.latitude.toFloat(),
                    currentLocation!!.longitude.toFloat(),
                    currentLocation!!.altitude.toFloat(),
                    System.currentTimeMillis())

            azimuth -= geomagneticField.declination
            desiredLocation = Location("").apply {
                latitude = selectedLatitude as Double
                longitude = selectedLongitude as Double
            }
            var bearTo = currentLocation!!.bearingTo(desiredLocation)

            if (bearTo < 0) {
                bearTo += FULL_CIRCLE_DEGREE
            }

            var direction = bearTo - azimuth


            if (direction < 0) {
                direction += 360
            }

            desiredLocationDirectionData.value = direction
        }
    }
}