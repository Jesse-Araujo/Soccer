package com.example.sports.listeners


import android.location.Location
import kotlinx.coroutines.flow.Flow

interface OnLocationChangedListener {

    fun onLocationChanged(latitude: Double, longitude: Double)

    fun getLocationUpdates(interval : Long) : Flow<Location>

    class LocationException(message : String) : Exception()

}