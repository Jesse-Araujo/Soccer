package com.example.soocer.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.soocer.R
import com.example.soocer.auxiliary.Global
import com.example.soocer.data.FirebaseFunctions
import com.example.soocer.data.MarkerLocations
import com.example.soocer.data.EventType
import com.example.soocer.data.Events
import com.example.soocer.apis.OddAPI
import com.example.soocer.location.DefaultLocationClient
import com.example.soocer.location.GPSChecker
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
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
fun log(events: List<Events>?) {
    CoroutineScope(Dispatchers.Main).launch {
        val resultStringBuilder = StringBuilder()
        events?.forEach { event ->
            resultStringBuilder.append(event.toString())
        }
        //Log.d("events from api", resultStringBuilder.toString())
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("VisibleForTests")
@Composable
fun MapScreen(
    navController: NavController,
    appContext: Context,
    sportsToShow: HashSet<String> = Global.favSports.toHashSet()
) {
    val cameraPositionState = rememberCameraPositionState()
    var footballLoading by remember { mutableStateOf(true) }
    var handballLoading by remember { mutableStateOf(true) }
    var basketballLoading by remember { mutableStateOf(true) }
    var volleyballLoading by remember { mutableStateOf(true) }
    var futsalLoading by remember { mutableStateOf(true) }
    val allEvents by remember { mutableStateOf<MutableList<Events>?>(mutableListOf()) }
    val filteredEvents by remember { mutableStateOf<MutableList<Events>?>(mutableListOf()) }
    var everythingLoaded by remember { mutableStateOf(false) }
    everythingLoaded =
        !footballLoading && !handballLoading && !basketballLoading && !volleyballLoading && !futsalLoading
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    val gpsIsOnline = remember { mutableStateOf(false) }
    val filterDistance = remember { mutableStateOf("max") }
    val showDialog = remember { mutableStateOf(false) }
    val showSearchBar = remember { mutableStateOf(true) }
    val showSearchBarRecomendations = remember { mutableStateOf(false) }
    val currentMarker = remember { mutableStateOf<MarkerLocations?>(null) }
    val eventsForMarkerWindow = remember { mutableStateOf<MutableList<Events>>(mutableListOf()) }
    val markers by remember { mutableStateOf<MutableList<MarkerLocations>>(mutableListOf()) }
    val existingMarkers by remember { mutableStateOf<HashMap<String, MarkerLocations>>(hashMapOf()) }
    val selectedTimeFilter = remember { mutableStateOf("today") }
    val currentSearch by remember { mutableStateOf<MutableList<Events>?>(mutableListOf()) }
    val sportsToShow by remember { mutableStateOf(sportsToShow.toHashSet()/*Global.favSports.toHashSet()*/) }
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
    val receivedDate = remember { mutableStateOf(false) }
    if (Events.events.isEmpty()) {
        LaunchedEffect(Unit) {
            FirebaseFunctions.getDataFromFirebase(receivedDate) { result ->
                if (result.isNotEmpty()) {
                    Log.d("ja li da firebase", "")
                    Events.events.addAll(result)
                    allEvents?.addAll(result)
                    filteredEvents?.addAll(result)
                    footballLoading = false
                    handballLoading = false
                    basketballLoading = false
                    volleyballLoading = false
                    futsalLoading = false
                } else {
                    Log.d("vou ler da API", "")
                    CoroutineScope(Dispatchers.IO).launch {
                        Events.getFootballEvents { result ->
                            if (!result.isNullOrEmpty()) {
                                Events.events.addAll(result)
                                allEvents?.addAll(result)
                                filteredEvents?.addAll(result)
                            }
                            Log.d("tenho a info de football", Events.events.toString())
                            footballLoading = false
                            log(result)
                        }

                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        Events.getHandballEvents { result ->
                            if (!result.isNullOrEmpty()) {
                                Events.events.addAll(result)
                                allEvents?.addAll(result)
                                filteredEvents?.addAll(result)
                            }
                            Log.d("tenho a info de handball", Events.events.toString())
                            log(result)
                            handballLoading = false
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        Events.getFutsalEvents { result ->
                            if (!result.isNullOrEmpty()) {
                                Events.events.addAll(result)
                                allEvents?.addAll(result)
                                filteredEvents?.addAll(result)
                            }
                            Log.d("tenho a info de futsal", Events.events.toString())
                            log(result)
                            futsalLoading = false
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        Events.getBasketballEvents { result ->
                            if (!result.isNullOrEmpty()) {
                                Events.events.addAll(result)
                                allEvents?.addAll(result)
                                filteredEvents?.addAll(result)
                            }
                            Log.d("tenho a info de basket", Events.events.toString())
                            log(result)
                            basketballLoading = false
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        Events.getVolleyballEvents { result ->
                            if (!result.isNullOrEmpty()) {
                                Events.events.addAll(result)
                                allEvents?.addAll(result)
                                filteredEvents?.addAll(result)
                            }
                            Log.d("tenho a info de basket", Events.events.toString())
                            log(result)
                            volleyballLoading = false
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        while (!everythingLoaded) {
                        }
                        Log.d("Posso salvar", "")
                        if (FirebaseFunctions.saveInFirebase) {
                            FirebaseFunctions.saveDataInFirebase()
                            FirebaseFunctions.saveInFirebase = false
                        }

                    }
                }
            }
        }
    } else {

        if (allEvents == null || allEvents!!.isEmpty()) allEvents?.addAll(Events.events)
        if (filteredEvents == null || filteredEvents!!.isEmpty()) filteredEvents?.addAll(Events.events)
        footballLoading = false
        handballLoading = false
        basketballLoading = false
        volleyballLoading = false
        futsalLoading = false

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
        /*Map(
            cameraPositionState = cameraPositionState,
            showSearchBarRecomendations = showSearchBarRecomendations,
            showSearchBar = showSearchBar,
            showDialog = showDialog,
            lat = lat,
            long = long,
            footballLoading = footballLoading,
            handballLoading = handballLoading,
            markers = markers,
            existingMarkers = existingMarkers,
            filteredEvents = filteredEvents,
            appContext = appContext,
            currentMarker = currentMarker,
            eventsForMarkerWindow = eventsForMarkerWindow
        )*/
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = Global.size.dp),
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
            if (!footballLoading && !handballLoading && !basketballLoading && !volleyballLoading && !futsalLoading) {
                markers.clear()
                existingMarkers.clear()
                Log.d("filtered events mapa", filteredEvents?.size.toString())
                Events.getEventsWithTimeFilter(
                    allEvents,
                    filteredEvents,
                    selectedTimeFilter,
                    currentSearch,
                    filterDistance,
                    LatLng(lat, long),
                    sportsToShow
                )
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
        if (showDialog.value) {
            if (eventsForMarkerWindow.value.size > 1) {
                ShowEventList(appContext, eventsForMarkerWindow,navController)
            } else {
                WindowMarkerDetails(event = eventsForMarkerWindow.value[0],
                    appContext,
                    navController = navController,
                    onDismiss = { showDialog.value = false })
                //eventsForMarkerWindow.value = mutableListOf()
            }
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            DistanceBox(
                allEvents = allEvents,
                filteredEvents = filteredEvents,
                filterDistance = filterDistance,
                cameraPositionState = cameraPositionState,
                lat = lat,
                long = long,
            )
            CenterUserPositionBox(
                cameraPositionState = cameraPositionState,
                lat = lat,
                long = long,
                showDialog = showDialog,
                showSearchBar = showSearchBar
            )
            Column {
                AutoComplete(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    allEvents,
                    showSearchBar,
                    showSearchBarRecomendations,
                    filteredEvents,
                    filterDistance,
                    currentSearch,
                    LatLng(lat, long)
                ) { bol, loc ->
                    if (bol) {
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
                TimeFilterOptions(selectedTimeFilter, showSearchBar)
                if(showSearchBar.value) {
                    SportsFilterButton(
                        sportsToShow,
                        cameraPositionState
                    )
                }
            }
            if (!gpsIsOnline.value) Text(
                text = "Turn on GPS",
                modifier = Modifier.align(Alignment.TopCenter),
                style = TextStyle(background = Color.Red)
            )
            /*if(!showSportFilterOptions.value) {
                DistanceBox(
                    allEvents = allEvents,
                    filteredEvents = filteredEvents,
                    filterDistance = filterDistance,
                    cameraPositionState = cameraPositionState,
                    lat = lat,
                    long = long,
                )
                CenterUserPositionBox(
                    cameraPositionState = cameraPositionState,
                    lat = lat,
                    long = long,
                    showDialog = showDialog,
                    showSearchBar = showSearchBar
                )
            }*/
        }
        BottomNavigator(navController = navController)
    }
}

@Composable
fun SportsFilterButton(
    sportsToShow: HashSet<String>,
    cameraPositionState: CameraPositionState
) {
    val showSportFilterOptions = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color.White, shape = CircleShape)
                .clickable(interactionSource = MutableInteractionSource(), indication = null) {
                    showSportFilterOptions.value = !showSportFilterOptions.value
                },
            contentAlignment = Alignment.CenterEnd
        ) {
            Image(
                painter = painterResource(id = R.drawable.filter),
                contentDescription = "Camera",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
            )
        }
    }
    SportsFilterOptions(showSportFilterOptions, sportsToShow, cameraPositionState)
}

@Composable
fun SportsFilterOptions(
    showSportFilterOptions: MutableState<Boolean>,
    sportsToShow: HashSet<String>,
    cameraPositionState: CameraPositionState
) {
    if (showSportFilterOptions.value) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(.5f)
                    .background(Color.White)
                    .align(Alignment.BottomCenter)
                    .clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = null
                    ) { }
            ) {
                Column(
                    Modifier
                        .fillMaxHeight()
                        .padding(top = 15.dp, start = 15.dp)
                ) {
                    Button(
                        onClick = {
                            showSportFilterOptions.value = false
                            cameraPositionState.position =
                                CameraPosition.fromLatLngZoom(cameraPositionState.position.target, cameraPositionState.position.zoom)
                        },
                        colors = ButtonDefaults.buttonColors(Color.Transparent),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "close_sports_filter_box",
                            modifier = Modifier.size(25.dp)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                        CheckSportBox(sportsToShow, EventType.FOOTBALL.type, 23)
                        CheckSportBox(sportsToShow, EventType.HANDBALL.type, 23)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                        CheckSportBox(sportsToShow, EventType.BASKETBALL.type, 23)
                        CheckSportBox(sportsToShow, EventType.VOLLEYBALL.type, 23)
                    }
                    CheckSportBox(sportsToShow, EventType.FUTSAL.type, 23)
                }
            }
        }

    }
}

@Composable
fun CheckSportBox(sportsToShow: HashSet<String>, sport: String, size: Int) {
    val blueColor = Color(0xFF007BFF)
    var sportSelected by remember { mutableStateOf(sportsToShow.contains(sport)) }
    Button(
        modifier = Modifier.width(175.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (sportSelected) blueColor else Color.White,
            contentColor = if (sportSelected) Color.White else Color.Black
        ),
        onClick = {
            sportSelected = !sportSelected
            if (sportSelected) sportsToShow.add(sport) else sportsToShow.remove(sport)
        }) {
        Text(
            text = sport,
            fontSize = size.sp,
            modifier = Modifier.align(Alignment.CenterVertically),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TimeFilterOptions(
    selectedTimeFilter: MutableState<String>,
    showSearchBar: MutableState<Boolean>
) {
    if (showSearchBar.value) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.size(10.dp))
            TimeFilterButtons("today", selectedTimeFilter) {}
            Spacer(modifier = Modifier.size(10.dp))
            TimeFilterButtons("1 day", selectedTimeFilter) {}
            Spacer(modifier = Modifier.size(10.dp))
            TimeFilterButtons("3 days", selectedTimeFilter) {}
            Spacer(modifier = Modifier.size(10.dp))
            TimeFilterButtons("5 days", selectedTimeFilter) {}
            Spacer(modifier = Modifier.size(10.dp))
            TimeFilterButtons("1 week", selectedTimeFilter) {}
            Spacer(modifier = Modifier.size(10.dp))
            TimeFilterButtons("2 weeks", selectedTimeFilter) {}
            Spacer(modifier = Modifier.size(10.dp))
        }
    }
}

@Composable
fun TimeFilterButtons(text: String, selectedFilter: MutableState<String>, onFinished: () -> Unit) {
    val blueColor = Color(0xFF007BFF)
    val isSelected = selectedFilter.value == text
    Button(
        modifier = Modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) blueColor else Color.White,
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        onClick = {
            selectedFilter.value = text
            onFinished()
        }) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.CenterVertically),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CenterUserPositionBox(
    cameraPositionState: CameraPositionState,
    lat: Double,
    long: Double,
    showDialog: MutableState<Boolean>,
    showSearchBar: MutableState<Boolean>
) {
    val lightBlueColor = Color(0xFF4A89f3)
    Box(Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = 220.dp, end = 10.dp)
            .size(65.dp)
            .background(lightBlueColor, shape = CircleShape)
            .clickable(interactionSource = MutableInteractionSource(), indication = null) {
                cameraPositionState.position =
                    CameraPosition.fromLatLngZoom(LatLng(lat, long), 15f)
                showSearchBar.value = true
                showDialog.value = false
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_gps_fixed),
                contentDescription = "Camera",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun DistanceBox(
    allEvents: MutableList<Events>?,
    filteredEvents: MutableList<Events>?,
    filterDistance: MutableState<String>,
    cameraPositionState: CameraPositionState,
    lat: Double,
    long: Double
) {
    val lightBlueColor = Color(0xFF4A89f3)
    Box(Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = 140.dp, end = 10.dp)
            .size(65.dp)
            .background(lightBlueColor, shape = CircleShape)
            .clickable(interactionSource = MutableInteractionSource(), indication = null) {
                changeDistanceFilter(
                    allEvents, filteredEvents, filterDistance, LatLng(lat, long)
                ) { loc ->
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        cameraPositionState.position.target,
                        cameraPositionState.position.zoom
                    )
                }
            }
        ) {
            Text(
                text = filterDistance.value,
                //text = "",
                modifier = Modifier.align(Alignment.Center),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun changeDistanceFilter(
    events: MutableList<Events>?,
    filteredEvents: MutableList<Events>?,
    filterDistance: MutableState<String>,
    userLoc: LatLng,
    onFinished: (LatLng) -> Unit
) {
    when (filterDistance.value) {
        "0.5km" -> filterDistance.value = "1km"
        "1km" -> filterDistance.value = "5km"
        "5km" -> filterDistance.value = "15km"
        "15km" -> filterDistance.value = "max"
        "max" -> filterDistance.value = "0.5km"
    }
}

fun getImage(eventType: EventType): Int {
    return when (eventType) {
        EventType.FOOTBALL -> R.drawable.football_img
        EventType.BASKETBALL -> R.drawable.basketball_marker_window
        EventType.VOLLEYBALL -> R.drawable.volleyball_marker_window
        EventType.FUTSAL -> R.drawable.futsal_marker_window
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
    when(event.eventType){
        EventType.FOOTBALL -> OddAPI.getOdds(odds, event,"https://v3.football.api-sports.io" ,"https://v3.football.api-sports.io/odds?fixture=${event.id}&season=2023")
        EventType.HANDBALL -> OddAPI.getOdds(odds, event, "https://v1.handball.api-sports.io","https://v1.handball.api-sports.io/odds?game=${event.id}")
        EventType.BASKETBALL -> OddAPI.getOdds(
            odds,
            event,
            "https://v1.basketball.api-sports.io",
            "https://v1.basketball.api-sports.io/odds?game=${event.id}&season=2023-2024&league=74"
        )
        EventType.FUTSAL -> TODO()
        EventType.VOLLEYBALL -> OddAPI.getOdds(odds, event,"https://v1.volleyball.api-sports.io" ,
            "https://v1.volleyball.api-sports.io/odds?game=${event.id}")
        else -> return
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

