package com.example.soocer.componets

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soocer.R
import com.example.soocer.location.DefaultLocationClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
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
){
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val alvalade = LatLng(38.76, -9.16)
        val cameraPositionState = rememberCameraPositionState()/*rememberCameraPositionState {
            //position = CameraPosition.fromLatLngZoom(alvalade, 15f)
        }*/

        var currentLocation by remember { mutableStateOf<Location?>(null) }
        val lat : Double = currentLocation?.latitude ?: 0.0
        val long : Double = currentLocation?.longitude ?: 0.0
        cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat,long),15f)

        LaunchedEffect(currentLocation) {
            currentLocation?.let { location ->
                val latitude = location.latitude
                val longitude = location.longitude
                Log.d("Loc","($latitude, $longitude)")
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
                        if(bol) {
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
                state = MarkerState(position = LatLng(lat,long)),
                title = "You are here",
            )
            Marker(
                state = MarkerState(position = alvalade),
                title = "Estádio José Alvalade",
                snippet = "Marker in Lisboa"
            )
        }
        Box (
            modifier = Modifier.fillMaxSize()
        ){
            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(.05f)
                    .fillMaxWidth(.3f),
                onClick = {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat, long), 15f)
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

@Preview
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController, LocalContext.current)
}