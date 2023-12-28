package com.example.soocer.componets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.soocer.R
import com.example.soocer.data.MarkerLocations
import com.example.soocer.data.Type
import com.example.soocer.events.EventType
import com.example.soocer.events.Events
import com.example.soocer.location.DefaultLocationClient
import com.example.soocer.location.GPSChecker
import com.example.soocer.weather.Weather
import com.example.soocer.weather.WeatherType
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
fun log(events: List<Events>?) {
    CoroutineScope(Dispatchers.Main).launch {
        val resultStringBuilder = StringBuilder()
        events?.forEach { event ->
            resultStringBuilder.append(event.toString())
        }
        Log.d("events", resultStringBuilder.toString())
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("VisibleForTests")
@Composable
fun HomeScreen(
    navController: NavController,
    appContext: Context
) {
    val cameraPositionState = rememberCameraPositionState()/*rememberCameraPositionState {
            //position = CameraPosition.fromLatLngZoom(alvalade, 15f)
        }*/
    var loading by remember { mutableStateOf(true) }
    //var events by remember { mutableStateOf<List<Events>?>(null) }
    var events by remember { mutableStateOf<MutableList<Events>?>(mutableListOf()) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    val gpsIsOnline = remember { mutableStateOf(false) }
    val e = Events(
        1, EventType.FOOTBALL, "", LocalDateTime.MAX, "", "", "", "", "", "",
        MarkerLocations("", LatLng(0.0, 0.0), Type.STADIUM, 1, "", 0), false
    )
    val showDialog = remember { mutableStateOf(false) }
    val eventForMarkerWindow = remember { mutableStateOf(e) }
    GPSChecker(appContext) { gpsIsOnline2, loc ->
       if (gpsIsOnline2) {
           if(!gpsIsOnline.value) {
               gpsIsOnline.value = gpsIsOnline2
               if(loc != null) currentLocation = loc else gpsIsOnline.value = false
           }else{
               gpsIsOnline.value = gpsIsOnline2
           }
       }else{
           gpsIsOnline.value = gpsIsOnline2
       }


    }

    LaunchedEffect(Unit) {
        Events.getFootballEvents { result ->
            if (result != null) events?.addAll(result)
            loading = false
            Log.d("tenho a info de football", "")
            log(result)
        }
    }
    LaunchedEffect(Unit) {
        Events.getHandballEvents { result ->
            if (result != null) events?.addAll(result)
            loading = false
            Log.d("tenho a info de handball", "")
            log(result)
        }
    }


    val lat: Double = currentLocation?.latitude ?: 0.0
    val long: Double = currentLocation?.longitude ?: 0.0

    cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat, long), 15f)

    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            val latitude = location.latitude
            val longitude = location.longitude
            //Log.d("Loc", "($latitude, $longitude)")
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
                    //Log.d("loc", location.toString())
                    // evita que a camera seja puxada para a nossa loc a cada x tempo
                    if (bol) {
                        currentLocation = location
                        gpsIsOnline.value = true
                        bol = false
                    }
                }.launchIn(this)
        }

        onDispose {
        }
    }


    Box(Modifier.fillMaxSize()) {
        if (showDialog.value) cameraPositionState.position =
            CameraPosition.fromLatLngZoom(eventForMarkerWindow.value.markerLocations.latLng, 15f)
        if (!showDialog.value && eventForMarkerWindow.value.markerLocations.city != "") cameraPositionState.position =
            CameraPosition.fromLatLngZoom(eventForMarkerWindow.value.markerLocations.latLng, 15f)
        GoogleMap(
            modifier = Modifier
                .fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                showDialog.value = false
            },
        ) {
            // remove marker window on map drag
            if (cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
                showDialog.value = false
            }
            Marker(
                state = MarkerState(position = LatLng(lat, long)),
                title = "You are here",
            )
            if (loading) {
                // Show a loading indicator
            } else {
                for (event in events.orEmpty()) {
                    //Log.d("vou meter eventos no mapa", event.markerLocations.title)
                    CustomMarker(
                        context = appContext,
                        modifier = Modifier.fillMaxSize(),
                        event = event,
                        showDialog,
                        eventForMarkerWindow,
                        cameraPositionState
                    )
                }
            }
        }
        if (showDialog.value) {
            alert(event = eventForMarkerWindow.value,
                onDismiss = { showDialog.value = false })
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if(!gpsIsOnline.value) Text(text = "Turn on GPS", modifier = Modifier.align(Alignment.TopCenter),style = TextStyle(background = Color.Red))
            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(.05f)
                    .fillMaxWidth(.3f),
                onClick = {
                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(LatLng(lat, long), 15f)
                    showDialog.value = false
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
    modifier: Modifier,
    event: Events,
    showDialog: MutableState<Boolean>,
    eventForMarkerWindow: MutableState<Events>,
    cameraPositionState: CameraPositionState,
) {
    val iconResourceId = when (event.markerLocations.type) {
        Type.STADIUM -> R.drawable.stadium
        else -> R.drawable.pavilion
    }
    val icon = bitmapDescriptorFromVector(
        context, iconResourceId, 100, 75
    )
    MarkerInfoWindow(
        state = MarkerState(position = event.markerLocations.latLng),
        title = event.markerLocations.title, //TODO make this disappear when the window is closed
        //snippet = "Marker in Lisboa",
        icon = icon,
        onClick = {
            eventForMarkerWindow.value = event
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(event.markerLocations.latLng, 15f)
            showDialog.value = true
            false
        })
}

fun getImage(eventType: EventType): Int {
    return when (eventType) {
        EventType.FOOTBALL -> R.drawable.football_img
        else -> R.drawable.football_img
    }
}

fun getPlaceImage(eventType: EventType): Int {
    return when (eventType) {
        EventType.FOOTBALL -> R.drawable.stadium
        else -> R.drawable.stadium
    }
}

@Composable
fun alert(
    event: Events,
    onDismiss: () -> Unit
) {

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .padding(top = 15.dp)
                .fillMaxWidth(.8f)
                .fillMaxHeight(.4f)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(35.dp, 35.dp, 35.dp, 35.dp)
                )
                .align(Alignment.TopCenter)
                .clickable { Log.d("click na box", "") },//to stop window from disappear on map drag
        ) {
            Column(Modifier.fillMaxSize()) {
                Spacer(
                    modifier = Modifier
                        .size(10.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AsyncImage(model = event.homeTeamLogo, contentDescription = "home_logo")
                    Text(
                        text = "${event.homeTeam} vs ${event.awayTeam}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    AsyncImage(model = event.awayTeamLogo, contentDescription = "away_logo")
                }
                Spacer(
                    modifier = Modifier
                        .size(10.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = getImage(event.eventType)),
                        contentDescription = "icon",
                        modifier = Modifier.size(39.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .padding(10.dp)
                    )
                    var d = event.date.toString().replace("T", " ")
                    d += "h"
                    Text(text = d, Modifier.padding(top = 5.dp))
                }
                Spacer(
                    modifier = Modifier
                        .size(10.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = getPlaceImage(event.eventType)),
                        contentDescription = "stadium_c",
                        modifier = Modifier.size(39.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .size(10.dp)
                    )
                    Text(
                        text = "Expected ${event.markerLocations.expectedCapacity} fans",
                        Modifier.padding(top = 5.dp)
                    )
                }
                Spacer(
                    modifier = Modifier
                        .size(10.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val weather =
                        remember { mutableStateOf(Weather(0.0, WeatherType.ERROR, 0.0, 0.0)) }
                    if(weather.value.lat == 0.0 && weather.value.lng == 0.0) {
                        Weather.getWeather(
                            event.date,
                            event.markerLocations.latLng.latitude,
                            event.markerLocations.latLng.longitude,
                            weather
                        )
                    }
                    Image(
                        painter = painterResource(id = getWeatherIcon(weather.value.main)),
                        contentDescription = "weather",
                        modifier = Modifier.size(39.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .size(10.dp)
                    )
                    val text =
                        if (weather.value.main != WeatherType.ERROR) "${weather.value.temp} C" else ""
                    Text(text = text, Modifier.padding(top = 5.dp))
                }
                Spacer(
                    modifier = Modifier
                        .size(10.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { //TODO open bet app
                        }, horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bet),
                        contentDescription = "odds",
                        modifier = Modifier.size(39.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .size(10.dp)
                    )
                    Text(text = "Benfica 1.35 - 1.40 Sporting", Modifier.padding(top = 5.dp))
                }
                Button(onClick = onDismiss/*{ Log.d("Clickey", "")  }*/) {
                    Text(text = "Dismiss")
                }
            }
        }
    }
}

fun getWeatherIcon(weatherType: WeatherType): Int {
    return when (weatherType) {
        WeatherType.SUNNY -> R.drawable.sun
        WeatherType.CLOUDY -> R.drawable.cloudy
        WeatherType.RAINY -> R.drawable.rainy
        else -> R.drawable.loading
    }
}

fun bitmapDescriptorFromVector(
    context: Context,
    vectorResId: Int,
    width: Int,
    height: Int
): BitmapDescriptor? {
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null

    drawable.setBounds(0, 0, width, height)

    val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val canvas = Canvas(bm)
    drawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bm)
}

@Preview
@Composable
fun alert() {

    Card(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Spacer(
                modifier = Modifier
                    .size(10.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                //AsyncImage(model = "https://media-4.api-sports.io/football/teams/211.png", contentDescription = "home_logo")
                Text(text = "Benfica vs Sporting", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                //AsyncImage(model = "https://media-4.api-sports.io/football/teams/228.png", contentDescription = "away_logo")
            }
            Spacer(
                modifier = Modifier
                    .size(10.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Image(
                    painter = painterResource(id = R.drawable.football_img),
                    contentDescription = "icon",
                    modifier = Modifier.size(39.dp)
                )
                Spacer(
                    modifier = Modifier
                        .padding(10.dp)
                )
                Text(text = "2023/10/10 20:30h", Modifier.padding(top = 5.dp))
            }
            Spacer(
                modifier = Modifier
                    .size(10.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Image(
                    painter = painterResource(id = R.drawable.stadium),
                    contentDescription = "stadium_c",
                    modifier = Modifier.size(39.dp)
                )
                Spacer(
                    modifier = Modifier
                        .size(10.dp)
                )
                Text(text = "Expected 65.000 fans", Modifier.padding(top = 5.dp))
            }
            Spacer(
                modifier = Modifier
                    .size(10.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Image(
                    painter = painterResource(id = R.drawable.cloudy),
                    contentDescription = "cloudy",
                    modifier = Modifier.size(39.dp)
                )
                Spacer(
                    modifier = Modifier
                        .size(10.dp)
                )
                Text(text = "25º C", Modifier.padding(top = 5.dp))
            }
            Spacer(
                modifier = Modifier
                    .size(10.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { //TODO open bet app
                    }, horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bet),
                    contentDescription = "odds",
                    modifier = Modifier.size(39.dp)
                )
                Spacer(
                    modifier = Modifier
                        .size(10.dp)
                )
                Text(text = "Benfica 1.35 - 1.40 Sporting", Modifier.padding(top = 5.dp))
            }

        }
    }

}

@Composable
@Preview
fun test() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(text = "Turn on GPS", modifier = Modifier.align(Alignment.TopCenter),style = TextStyle(background = Color.Red))
    }
}
