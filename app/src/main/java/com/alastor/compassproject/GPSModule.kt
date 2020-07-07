package com.alastor.compassproject

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.IntentSender
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.common.ConnectionResult.*
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

class GPSModule(val mSelectedLatitude: Float,
                val mSelectedLongitude: Float,
                private val mLifecycle: Lifecycle,
                private val mGPSCallback: GPSCallback) : LifecycleObserver {

    private var isGoogleServiceChecked: Boolean = false

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
                // Update UI with location data
            }
        }
    }

    init {
        mLifecycle.addObserver(this);
    }

    @SuppressLint("MissingPermission")
    public fun enable(context: Context) {
        val fusedLocationProviderClient = FusedLocationProviderClient(context)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun registerListener() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun unregisterListener() {
        //fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public fun checkGooglePlayService(activity: Activity,
                                      googleApiAvailability: GoogleApiAvailability): Dialog? {
        isGoogleServiceChecked = true
        val code = googleApiAvailability.isGooglePlayServicesAvailable(activity.baseContext)
        if (!isGooglePlayServicesAvailable(code)) {
            return googleApiAvailability.getErrorDialog(activity, code, 0)
        }
        return null
    }

    public fun checkLocationSetting(activity: Activity) {
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(activity.baseContext)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                    //TODO add onActivityResult()
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
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