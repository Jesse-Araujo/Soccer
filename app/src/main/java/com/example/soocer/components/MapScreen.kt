package com.example.soocer.components

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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.soocer.R
import com.example.soocer.auxiliary.Global
import com.example.soocer.data.FirebaseFunctions
import com.example.soocer.data.MarkerLocations
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
    appContext: Context
) {
    val cameraPositionState = rememberCameraPositionState()
    var footballLoading by remember { mutableStateOf(true) }
    var handballLoading by remember { mutableStateOf(true) }
    val allEvents by remember { mutableStateOf<MutableList<Events>?>(mutableListOf()) }
    val filteredEvents by remember { mutableStateOf<MutableList<Events>?>(mutableListOf()) }
    var everythingLoaded by remember { mutableStateOf(false) }
    everythingLoaded = !footballLoading && !handballLoading
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
            if (!footballLoading && !handballLoading) {
                markers.clear()
                existingMarkers.clear()
                Log.d("filtered events mapa", filteredEvents?.size.toString())
                Events.getEventsWithTimeFilter(allEvents,filteredEvents,selectedTimeFilter,currentSearch)
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
                        cameraPositionState
                    )
                }
            } else {
                Log.d("loading", "")
            }

        }
        if (showDialog.value) {
            if (eventsForMarkerWindow.value.size > 1) {
                ShowEventList(appContext, eventsForMarkerWindow)
            } else {
                WindowMarkerDetails(event = eventsForMarkerWindow.value[0],
                    appContext,
                    onDismiss = { showDialog.value = false })
                //eventsForMarkerWindow.value = mutableListOf()
            }
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
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
                TimeFilterOptions(selectedTimeFilter,showSearchBar)
            }
            if (!gpsIsOnline.value) Text(
                text = "Turn on GPS",
                modifier = Modifier.align(Alignment.TopCenter),
                style = TextStyle(background = Color.Red)
            )
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
        }
        BottomNavigator(navController = navController)
    }
}

@Composable
fun TimeFilterOptions(
    selectedTimeFilter: MutableState<String>,
    showSearchBar: MutableState<Boolean>
) {
    if(showSearchBar.value) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.size(10.dp))
            TimeFilterButtons("today",selectedTimeFilter){}
            Spacer(modifier = Modifier.size(10.dp))
            TimeFilterButtons("1 day",selectedTimeFilter){}
            Spacer(modifier = Modifier.size(10.dp))
            TimeFilterButtons("3 days",selectedTimeFilter){}
            Spacer(modifier = Modifier.size(10.dp))
            TimeFilterButtons("5 days",selectedTimeFilter){}
            Spacer(modifier = Modifier.size(10.dp))
            TimeFilterButtons("1 week",selectedTimeFilter){}
            Spacer(modifier = Modifier.size(10.dp))
            TimeFilterButtons("2 weeks",selectedTimeFilter){}
            Spacer(modifier = Modifier.size(10.dp))
        }
    }
}

@Composable
fun TimeFilterButtons(text: String, selectedFilter: MutableState<String>,onFinished: () -> Unit) {
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
            .size(75.dp)
            .background(lightBlueColor, shape = RoundedCornerShape(16.dp))
            .clickable {
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
    val lightBlueColor = Color(0xFF4A89f3)//Color(0xFF038FEC)
    Box(Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = 140.dp, end = 10.dp)
            .size(75.dp)
            .background(lightBlueColor, shape = RoundedCornerShape(16.dp))
            .clickable {
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
        "0.5km" -> {
            Events.getEventsInDistance(events, filteredEvents, 1f, userLoc, onFinished)
            filterDistance.value = "1km"
        }

        "1km" -> {
            Events.getEventsInDistance(events, filteredEvents, 5f, userLoc, onFinished)
            filterDistance.value = "5km"
        }

        "5km" -> {
            Events.getEventsInDistance(events, filteredEvents, 15f, userLoc, onFinished)
            filterDistance.value = "15km"
        }

        "15km" -> {
            Events.getEventsInDistance(events, filteredEvents, 0f, userLoc, onFinished)
            filterDistance.value = "max"
        }

        "max" -> {
            Events.getEventsInDistance(events, filteredEvents, 0.5f, userLoc, onFinished)
            filterDistance.value = "0.5km"
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
            odds.value = Pair("No info", "No info")
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
}*/
/*
@Composable
@Preview
fun test() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val sports = hashSetOf(
            EventType.FOOTBALL,
            EventType.FUTSAL,
            EventType.VOLLEYBALL,
            EventType.HANDBALL,
            EventType.HOKEY,
            EventType.BASKETBALL
        )
        var footballCheck = remember { mutableStateOf(false) }
        var futsalCheck = remember { mutableStateOf(false) }
        var handballCheck = remember { mutableStateOf(false) }
        var volleyballCheck = remember { mutableStateOf(false) }
        var hokeyCheck = remember { mutableStateOf(false) }
        var basketballCheck = remember { mutableStateOf(false) }
        val checkboxSize = 1.5f
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = "Advanced search",
                    Modifier
                        .align(Alignment.CenterVertically)
                        .padding(top = 10.dp)
                )
            }
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column {
                    Text(text = "Football",Modifier.align(Alignment.CenterHorizontally), fontSize = 20.sp)
                    Text(text = "Handball",Modifier.align(Alignment.CenterHorizontally), fontSize = 20.sp)
                    Text(text = "Volleyball",Modifier.align(Alignment.CenterHorizontally), fontSize = 20.sp)
                }
                Column {
                    Text(text = "xiu")
                    Text(text = "xiu")
                    Text(text = "xiu")
                }
            }

            AdvancedSearchRow(
                checked1Bol = footballCheck,
                text1 = "Football",
                textSize1 = 20,
                text2 = "Futsal",
                textSize2 = 20,
                checked2Bol = futsalCheck,
                checkboxSize = checkboxSize
            )
            AdvancedSearchRow(
                checked1Bol = handballCheck,
                text1 = "Handball",
                textSize1 = 20,
                text2 = "Basketball",
                textSize2 = 20,
                checked2Bol = basketballCheck,
                checkboxSize = checkboxSize
            )
            AdvancedSearchRow(
                checked1Bol = volleyballCheck,
                text1 = "Volleyball",
                textSize1 = 20,
                text2 = "Hokey",
                textSize2 = 20,
                checked2Bol = hokeyCheck,
                checkboxSize = checkboxSize
            )
        }
    }
}

@Composable
fun AdvancedSearchRow(checked1Bol : MutableState<Boolean>,text1:String,textSize1:Int,text2:String,textSize2:Int,checked2Bol : MutableState<Boolean>,checkboxSize:Float) {
    Row(Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly) {
        Text(text = text1,Modifier.align(Alignment.CenterVertically), fontSize = textSize1.sp)
        Checkbox(
            checked = checked1Bol.value,
            onCheckedChange = { checked1Bol.value = !checked1Bol.value },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .scale(checkboxSize)
        )
        Spacer(modifier = Modifier)
        Text(text = text2,Modifier.align(Alignment.CenterVertically), fontSize = textSize2.sp)
        Checkbox(
            checked = checked2Bol.value,
            onCheckedChange = { checked2Bol.value = !checked2Bol.value },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .scale(checkboxSize)
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

