package com.alastor.compassproject

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import androidx.lifecycle.LifecycleObserver
import com.google.android.gms.common.ConnectionResult.*
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

class GPSModule(val mSelectedLatitude: Float,
                val mSelectedLongitude: Float,
                private val fusedLocationProviderClient: FusedLocationProviderClient,
                private val googleApiAvailability: GoogleApiAvailability,
                private val mGPSCallback: GPSCallback) : LifecycleObserver {

    private var isGoogleServiceChecked: Boolean = false
    private var isLocationSettingsChecked: Boolean = false

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 0
    }

    private val locationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {

            }
        }
    }

    public fun register(activity: Activity) {
        checkLocationSetting(activity)
        checkGooglePlayService(activity)
    }

    public fun unregister() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun checkGooglePlayService(activity: Activity): Dialog? {
        val code = googleApiAvailability.isGooglePlayServicesAvailable(activity.baseContext)
        if (!isGooglePlayServicesAvailable(code)) {
            val dialog = googleApiAvailability.getErrorDialog(activity, code, 0)
            isLocationSettingsChecked = false
            mGPSCallback.onGooglePlayServicesOutDate(dialog)
            return dialog
        }
        isGoogleServiceChecked = true
        return null
    }

    private fun checkLocationSetting(activity: Activity) {
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(activity.baseContext)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.apply {
            addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    isLocationSettingsChecked = false
                    mGPSCallback.onLackOfLocationSettings()
                }
            }
            addOnSuccessListener {
                isLocationSettingsChecked = true
                maybeNotifySuccessRegister()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun maybeNotifySuccessRegister() {
        if (isGoogleServiceChecked && isLocationSettingsChecked) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun isGooglePlayServicesAvailable(code: Int): Boolean {
        return when (code) {
            SUCCESS -> true
            SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED, SERVICE_DISABLED -> false
            else -> false
        }
    }
}