package com.example.soocer.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.soocer.auxiliary.Global
import com.example.soocer.data.FirebaseFunctions
import com.example.soocer.data.MarkerLocations
import com.example.soocer.events.Events
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
fun Map(cameraPositionState : CameraPositionState,
        showSearchBarRecomendations : MutableState<Boolean>,
        showSearchBar : MutableState<Boolean>,
        showDialog : MutableState<Boolean>,
        lat: Double,
        long : Double,
        footballLoading : Boolean,
        handballLoading : Boolean,
        markers : MutableList<MarkerLocations>,
        existingMarkers : HashMap<String, MarkerLocations>,
        filteredEvents : MutableList<Events>?,
        appContext :Context,
        currentMarker : MutableState<MarkerLocations?>,
        eventsForMarkerWindow : MutableState<MutableList<Events>>,
        navController: NavController
        ) {
    GoogleMap(
        modifier = Modifier
            .fillMaxSize().padding(bottom = Global.size.dp),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            showSearchBarRecomendations.value = false
            showSearchBar.value = true
            showDialog.value = false
        },
    ) {
        // remove marker window on map drag
        if (cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
            showSearchBar.value = true
            showDialog.value = false
        }
        Marker(
            state = MarkerState(position = LatLng(lat, long)),
            title = "You are here",
        )
        if (!footballLoading && !handballLoading) {
            //Log.d("vou meter markers no mapa", filteredEvents.toString())
            markers.clear()
            existingMarkers.clear()
            Log.d("filtered events mapa", filteredEvents?.size.toString())
            filteredEvents?.forEach {
                val marker = it.markerLocations
                val key = "${marker.latLng.latitude} | ${marker.latLng.longitude}"
                if (existingMarkers.contains(key)) {
                    val markerValue = existingMarkers.get(key)
                    markerValue?.events?.add(it)
                    if (markerValue != null) {
                        existingMarkers.put(key, markerValue)
                    }
                } else {
                    if (it.markerLocations.events.isEmpty()) it.markerLocations.events.add(it)
                    existingMarkers.put(key, it.markerLocations)
                }
            }

            existingMarkers.values.toList().forEach {
                //Log.d("eventos do marker", it.events.toString())
                CustomMarker(
                    context = appContext,
                    modifier = Modifier.fillMaxSize(),
                    marker = it,
                    showDialog,
                    showSearchBar,
                    currentMarker,
                    //eventForMarkerWindow,
                    eventsForMarkerWindow,
                    cameraPositionState,
                    navController
                )
            }
        } else {
            Log.d("loading", "")
        }

    }
}