package com.alastor.compassproject

import android.annotation.SuppressLint
import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.common.ConnectionResult.*
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.lang.ref.WeakReference

class GPSModule : LifecycleObserver {

    private var isGoogleServiceChecked: Boolean = false
    private var isLocationSettingsChecked: Boolean = false
    private var mLifecycle: Lifecycle? = null;

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mGoogleApiAvailability: GoogleApiAvailability
    private lateinit var mGPSCallbackWeakReference: WeakReference<GPSCallback>

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
                    it.onLocationDetect(location) }
            }
        }
    }

    constructor(fusedLocationProviderClient: FusedLocationProviderClient,
                googleApiAvailability: GoogleApiAvailability,
                gpsCallback: GPSCallback) {
        mFusedLocationProviderClient = fusedLocationProviderClient
        mGoogleApiAvailability = googleApiAvailability
        mGPSCallbackWeakReference = WeakReference(gpsCallback)
    }

    constructor(mFusedLocationProviderClient: FusedLocationProviderClient,
                mGoogleApiAvailability: GoogleApiAvailability,
                mLifecycle: Lifecycle,
                mGPSCallback: GPSCallback)
            : this(mFusedLocationProviderClient, mGoogleApiAvailability, mGPSCallback) {
        this.mLifecycle = mLifecycle
        mLifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public fun register(activity: Activity) {
        checkLocationSetting(activity)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public fun unregister() {
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun checkGooglePlayService(activity: Activity?) {
        activity?.let {
            val code = mGoogleApiAvailability.isGooglePlayServicesAvailable(activity.baseContext)
            if (!isGooglePlayServicesAvailable(code)) {
                val dialog = mGoogleApiAvailability.getErrorDialog(activity, code, 0)
                isLocationSettingsChecked = false
                mGPSCallbackWeakReference.get()?.let {
                    it.onGooglePlayServicesOutDate(dialog) }
            }
            isGoogleServiceChecked = true
        }
    }

    private fun checkLocationSetting(activity: Activity) {
        //Safety implementation of Activity
        val weakReference: WeakReference<Activity> = WeakReference(activity)
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        weakReference.get()?.let { weakReferenceActivity ->
            val client: SettingsClient = LocationServices.getSettingsClient(weakReferenceActivity)
            val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
            task.run {
                addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        isLocationSettingsChecked = false
                        mGPSCallbackWeakReference.get()?.let {
                            it.onLackOfLocationSettings() }
                    }
                }
                addOnSuccessListener {
                    isLocationSettingsChecked = true
                    checkGooglePlayService(weakReferenceActivity)
                    maybeNotifySuccessRegister()
                }
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