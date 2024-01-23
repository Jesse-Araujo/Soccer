package com.example.sports.location

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log



class GPSChecker(
    private val appContext: Context,
    private val onGpsStatusChanged: (Boolean, Location?) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private val checkIntervalMillis = 5000L // Adjust the interval as needed

    private var locationManager: LocationManager? = null
    private var lastLocation: Location? = null

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lastLocation = location
            onGpsStatusChanged(true, location)
        }

        override fun onProviderDisabled(provider: String) {
            onGpsStatusChanged(false, null)
        }

        override fun onProviderEnabled(provider: String) {
            checkGpsStatus()
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }
    }

    private val checkGpsStatusRunnable = object : Runnable {
        override fun run() {
            checkGpsStatus()
            handler.postDelayed(this, checkIntervalMillis)
        }
    }

    init {
        startCheckingGpsStatus()
    }

    private fun startCheckingGpsStatus() {
        locationManager =
            appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        handler.postDelayed(checkGpsStatusRunnable, checkIntervalMillis)
    }

    private fun stopCheckingGpsStatus() {
        locationManager?.removeUpdates(locationListener)
        handler.removeCallbacks(checkGpsStatusRunnable)
    }

    private fun checkGpsStatus() {
        val isGpsEnable = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        val isNetworkEnable = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

        val isGpsAvailable = isGpsEnable || isNetworkEnable

        onGpsStatusChanged(isGpsAvailable, lastLocation)

        //Log.d("location now","(${lastLocation?.latitude}, ${lastLocation?.longitude})")

        if (!isGpsAvailable) {
            // GPS signal is not available, handle accordingly
            //Log.d("GpsChecker", "GPS signal not available")
        } else {
            // GPS signal is available, handle accordingly
            //Log.d("GpsChecker", "GPS signal available")
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        try {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            )
        } catch (e: SecurityException) {
            // Handle permission issues
            Log.e("GpsChecker", "Error requesting location updates: ${e.message}")
        }
    }
}

// Usage:
// Create an instance of GpsChecker in your activity or service
// val gpsChecker = GpsChecker(appContext) { isGpsAvailable, location ->
//    // Handle GPS availability, update marker, etc.
//    if (isGpsAvailable && location != null) {
//        val latitude = location.latitude
//        val longitude = location.longitude
//        Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
//    }
// }