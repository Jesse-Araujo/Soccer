package com.example.soocer.componets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soocer.R
import com.example.soocer.data.MarkerLocations
import com.example.soocer.data.Type
import com.example.soocer.location.DefaultLocationClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@SuppressLint("VisibleForTests")
@Composable
fun HomeScreen(
    navController: NavController,
    appContext: Context
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val cameraPositionState = rememberCameraPositionState()/*rememberCameraPositionState {
            //position = CameraPosition.fromLatLngZoom(alvalade, 15f)
        }*/

        var currentLocation by remember { mutableStateOf<Location?>(null) }
        val lat: Double = currentLocation?.latitude ?: 0.0
        val long: Double = currentLocation?.longitude ?: 0.0
        cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat, long), 15f)
        //if(lat == 0.0 && long == 0.0) Toast.makeText(appContext,"Turn on GPS",Toast.LENGTH_SHORT).show()

        LaunchedEffect(currentLocation) {
            currentLocation?.let { location ->
                val latitude = location.latitude
                val longitude = location.longitude
                Log.d("Loc", "($latitude, $longitude)")
            }

        }

        DisposableEffect(Unit) {
            val locationClient = DefaultLocationClient(
                appContext,
                LocationServices.getFusedLocationProviderClient(appContext)
            )

            /*val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.locations.lastOrNull()?.let { location ->
                        currentLocation = location
                    }
                }
            }*/

            CoroutineScope(Dispatchers.IO).launch {
                var bol = true
                locationClient.getLocationUpdates(100L)
                    .catch { it.printStackTrace() }
                    .onEach { location ->
                        // evita que a camera seja puxada para a nossa loc a cada x tempo
                        if (bol) {
                            currentLocation = location
                            bol = false
                        }

                    }
                    .launchIn(this)
            }

            onDispose {

            }
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = LatLng(lat, long)),
                title = "You are here",
            )
            for (marker in MarkerLocations.markers) {
                CustomMarker(
                    context = appContext,
                    place = marker
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(.05f)
                    .fillMaxWidth(.3f),
                onClick = {
                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(LatLng(lat, long), 15f)
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_gps_fixed),
                    contentDescription = "Camera",
                    modifier = Modifier.size(24.dp) // Adjust size as needed
                )
            }
        }
    }
}

@Composable
fun CustomMarker(
    context: Context,
    place : MarkerLocations,
    //@DrawableRes iconResourceId: Int
) {
    var iconResourceId = when (place.type) {
        Type.STADIUM -> R.drawable.stadium
        else -> R.drawable.pavilion
    }
    val icon = bitmapDescriptorFromVector(
        context, iconResourceId,100,75
    )
    Marker(
        state = MarkerState(position = place.latLng),
        title = place.location,
        //snippet = "Marker in Lisboa",
        icon = icon
    )
}

fun bitmapDescriptorFromVector(
    context: Context,
    vectorResId: Int,
    width: Int,
    height: Int
): BitmapDescriptor? {
    // Retrieve the actual drawable
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null

    // Set the bounds to match the desired width and height
    drawable.setBounds(0, 0, width, height)

    // Create a Bitmap with the specified width and height
    val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    // Draw the drawable onto the Bitmap
    val canvas = Canvas(bm)
    drawable.draw(canvas)

    // Return the BitmapDescriptor
    return BitmapDescriptorFactory.fromBitmap(bm)

    /*// retrieve the actual drawable
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    val bm = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    // draw it onto the bitmap
    val canvas = android.graphics.Canvas(bm)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bm)*/
}

@Preview
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController, LocalContext.current)
}