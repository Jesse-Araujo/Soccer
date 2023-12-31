package com.example.soocer.componets

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.soocer.R
import com.example.soocer.data.MarkerLocations
import com.example.soocer.data.Type
import com.example.soocer.events.EventType
import com.example.soocer.events.Events
import com.example.soocer.events.OddAPI
import com.example.soocer.location.DefaultLocationClient
import com.example.soocer.location.GPSChecker
import com.example.soocer.weather.WeatherType
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
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
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
fun log(events: List<Events>?) {
    CoroutineScope(Dispatchers.Main).launch {
        val resultStringBuilder = StringBuilder()
        events?.forEach { event ->
            resultStringBuilder.append(event.toString())
        }
        Log.d("events from api", resultStringBuilder.toString())
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
    var footballLoading by remember { mutableStateOf(true) }
    var handballLoading by remember { mutableStateOf(true) }
    val allEvents by remember { mutableStateOf<MutableList<Events>?>(mutableListOf()) }
    val filteredEvents by remember { mutableStateOf<MutableList<Events>?>(mutableListOf()) }
    //val filteredEvents = remember { mutableStateOf<HashSet<Int>>(hashSetOf()) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    val gpsIsOnline = remember { mutableStateOf(false) }
    val e = Events(
        1, EventType.FOOTBALL, "", LocalDateTime.MAX, "", "", "", "", "", "",
        MarkerLocations("", LatLng(0.0, 0.0), Type.STADIUM, 1, "", 0, hashSetOf()), false,
        mutableListOf()
    )
    val showDialog = remember { mutableStateOf(false) }
    val showSearchBar = remember { mutableStateOf(true) }
    val showSearchBarRecomendations = remember { mutableStateOf(true) }
    val currentMarker = remember { mutableStateOf<MarkerLocations?>(null) }
    val eventsForMarkerWindow = remember { mutableStateOf<MutableList<Events>>(mutableListOf()) }
    val markers by remember { mutableStateOf<MutableList<MarkerLocations>>(mutableListOf()) }
    val existingMarkers by remember { mutableStateOf<HashMap<String, MarkerLocations>>(hashMapOf()) }
    GPSChecker(appContext) { gpsIsOnline2, loc ->
        if (gpsIsOnline2) {
            if (!gpsIsOnline.value) {
                gpsIsOnline.value = gpsIsOnline2
                if (loc != null) currentLocation = loc else gpsIsOnline.value = false
            } else {
                gpsIsOnline.value = gpsIsOnline2
            }
        } else {
            gpsIsOnline.value = gpsIsOnline2
        }


    }
    val receivedDate = remember { mutableStateOf(false)}
    if(Events.events.isEmpty()) {
        LaunchedEffect(Unit) {
            Events.getDataFromFirebase (receivedDate){result ->
                if(receivedDate.value) {
                    Log.d("ja li da firebase",allEvents.toString())
                    Events.events.addAll(result)
                    allEvents?.addAll(result)
                    filteredEvents?.addAll(result)
                    footballLoading = false
                    handballLoading = false
                }else{
                    CoroutineScope(Dispatchers.IO).launch {
                        //if (Events.events.isEmpty()) {
                            Events.getFootballEvents { result ->
                                if (!result.isNullOrEmpty()) {
                                    Events.events.addAll(result)
                                    allEvents?.addAll(result)
                                    filteredEvents?.addAll(result)
                                }
                                footballLoading = false
                                log(result)
                            }
                        /*} else {
                            allEvents?.addAll(Events.events)
                            filteredEvents?.addAll(Events.events)
                            footballLoading = false
                        }*/

                    }
                    CoroutineScope(Dispatchers.IO).launch {
                       // if (Events.events.isEmpty()) {
                            Events.getHandballEvents { result ->
                                if (!result.isNullOrEmpty()) {
                                    Events.events.addAll(result)
                                    allEvents?.addAll(result)
                                    filteredEvents?.addAll(result)
                                }
                                Log.d("tenho a info de handball", "")
                                log(result)
                                handballLoading = false
                            }
                        //} else handballLoading = false
                    }
                }
            }
        }
    } else{
        allEvents?.addAll(Events.events)
        filteredEvents?.addAll(Events.events)
        footballLoading = false
        handballLoading = false
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
            CameraPosition.fromLatLngZoom(
                eventsForMarkerWindow.value[0].markerLocations.latLng,
                15f
            )
        if (!showDialog.value && currentMarker.value != null) cameraPositionState.position =
            CameraPosition.fromLatLngZoom(
                eventsForMarkerWindow.value[0].markerLocations.latLng,
                15f
            )
        GoogleMap(
            modifier = Modifier
                .fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                showSearchBarRecomendations.value = false
                showSearchBar.value = true
                showDialog.value = false
                //eventsForMarkerWindow.value = mutableListOf()
                //currentMarker.value = null
            },
        ) {
            // remove marker window on map drag
            if (cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
                showSearchBar.value = true
                showDialog.value = false
                //eventsForMarkerWindow.value = mutableListOf()
                //currentMarker.value = null
            }
            Marker(
                state = MarkerState(position = LatLng(lat, long)),
                title = "You are here",
            )
            if (!footballLoading && !handballLoading) {
                if (Events.saveInFirebase) {
                    //TODO this is saving everytime
                    Events.saveDataInFirebase()
                    Events.saveInFirebase = false
                }
                Log.d("vou meter markers no mapa", filteredEvents.toString())
                markers.clear()
                existingMarkers.clear()
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
                    Log.d("eventos do marker", it.events.toString())
                    CustomMarker(
                        context = appContext,
                        modifier = Modifier.fillMaxSize(),
                        marker = it,
                        showDialog,
                        showSearchBar,
                        currentMarker,
                        //eventForMarkerWindow,
                        eventsForMarkerWindow,
                        cameraPositionState
                    )
                }
            } else {
                Log.d("loading", "")
            }

        }
        if (showDialog.value) {
            if (eventsForMarkerWindow.value.size > 1) {
                ShowEventList(appContext,eventsForMarkerWindow)
            } else {
                alert(event = eventsForMarkerWindow.value[0],
                    appContext,
                    onDismiss = { showDialog.value = false })
                //eventsForMarkerWindow.value = mutableListOf()
            }
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box {
                AutoComplete(
                    modifier = Modifier.align(Alignment.TopCenter),
                    allEvents,
                    showSearchBar,
                    showSearchBarRecomendations,
                    filteredEvents
                ) { bol,loc ->
                    if(bol) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            loc,
                            15f
                        )
                    } else {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            cameraPositionState.position.target,
                            cameraPositionState.position.zoom
                        )
                    }
                }
            }
            if (!gpsIsOnline.value) Text(
                text = "Turn on GPS",
                modifier = Modifier.align(Alignment.TopCenter),
                style = TextStyle(background = Color.Red)
            )
            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(.05f)
                    .fillMaxWidth(.3f),
                onClick = {
                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(LatLng(lat, long), 15f)
                    showSearchBar.value = true
                    showDialog.value = false
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_gps_fixed),
                    contentDescription = "Camera",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

fun getImage(eventType: EventType): Int {
    return when (eventType) {
        EventType.FOOTBALL -> R.drawable.football_img
        else -> R.drawable.handball_img
    }
}

fun getPlaceImage(eventType: EventType): Int {
    return when (eventType) {
        EventType.FOOTBALL -> R.drawable.stadium
        else -> R.drawable.pavilion
    }
}



fun getOddsForEvent(odds: MutableState<Pair<String, String>>, event: Events) {
    when (event.eventType) {
        EventType.FOOTBALL -> OddAPI.getFootballOdd(event.id, odds, event)
        else -> {
            odds.value = Pair("Erro", "Erro")
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
/*
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

        Text(
            text = "Turn on GPS",
            modifier = Modifier.align(Alignment.TopCenter),
            style = TextStyle(background = Color.Red)
        )
    }
}
*/
fun openBetclicApp(context: Context) {
    val packageName = "sport.android.betclic.pt"

    Intent(Intent.ACTION_MAIN).also {
        it.`package` = packageName
        it.addCategory(Intent.CATEGORY_LAUNCHER)
        try {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(it)
        } catch (e: ActivityNotFoundException) {
            val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(playStoreIntent)
        }
    }
}

