package com.alastor.compassproject.module.gps

import android.annotation.SuppressLint
import android.app.Activity
import com.google.android.gms.common.ConnectionResult.*
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.lang.ref.WeakReference

class GPSModule(private val mFusedLocationProviderClient: FusedLocationProviderClient,
                private val mGoogleApiAvailability: GoogleApiAvailability,
                gpsCallback: GPSCallback) {

    private var isGoogleServiceChecked: Boolean = false
    private var isLocationSettingsChecked: Boolean = false

    private var mGPSCallbackWeakReference: WeakReference<GPSCallback> = WeakReference(gpsCallback)

    private val locationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                mGPSCallbackWeakReference.get()?.let {
                    it.onLocationDetect(location)
                }
            }
        }
    }

    public fun register(activity: Activity) {
        checkGooglePlayService(activity)
        checkLocationSetting(activity)
    }

    public fun unregister() {
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun checkGooglePlayService(activity: Activity) {
        val code = mGoogleApiAvailability.isGooglePlayServicesAvailable(activity.baseContext)
        if (!isGooglePlayServicesAvailable(code)) {
            val dialog = mGoogleApiAvailability.getErrorDialog(activity, code, 0)
            isLocationSettingsChecked = false
            mGPSCallbackWeakReference.get()?.let {
                it.onGooglePlayServicesOutDate(dialog)
            }
        }
        isGoogleServiceChecked = true
        maybeNotifySuccessRegister()
    }

    private fun checkLocationSetting(activity: Activity) {
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.run {
            addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    isLocationSettingsChecked = false
                    mGPSCallbackWeakReference.get()?.let {
                        it.onLackOfLocationSettings(exception)
                    }
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
            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
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