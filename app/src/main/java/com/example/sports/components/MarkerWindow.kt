package com.example.sports.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sports.R
import com.example.sports.Screens
import com.example.sports.auxiliary.Global
import com.example.sports.data.FirebaseFunctions
import com.example.sports.data.MarkerLocations
import com.example.sports.data.Type
import com.example.sports.data.Events
import com.example.sports.apis.Weather
import com.example.sports.apis.WeatherType
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import kotlin.math.ceil

@Composable
fun CustomMarker(
    context: Context,
    modifier: Modifier,
    marker: MarkerLocations,
    showDialog: MutableState<Boolean>,
    showSearchBar: MutableState<Boolean>,
    currentMarker: MutableState<MarkerLocations?>,
    //eventForMarkerWindow: MutableState<Events>,
    eventsForMarkerWindow: MutableState<MutableList<Events>>,
    cameraPositionState: CameraPositionState,
    navController: NavController
) {
    var thereIsBigGame = false
    var cupGame = false
    marker.events.forEach {
        //Log.d("event","${it.homeTeam} vs ${it.awayTeam}")
        //Log.d("city", it.city)
        //Log.d("cup",it.isCupGame().toString())
        if (it.importantGame) thereIsBigGame = true
        if(it.isCupGame()) cupGame = true
    }
    val iconResourceId = when {
        marker.type == Type.STADIUM && cupGame -> R.drawable.stadium_allianz_cup
        marker.type == Type.STADIUM && thereIsBigGame -> R.drawable.stadium_big_game
        marker.type == Type.PAVILION && thereIsBigGame -> R.drawable.pavilion_big_game
        marker.type == Type.STADIUM -> R.drawable.stadium
        else -> R.drawable.pavilion
    }
    var size = Pair(100, 75)
    if (thereIsBigGame) size = Pair(280, 200)
    if (cupGame) size = Pair(210, 200)
    val icon = bitmapDescriptorFromVector(
        context, iconResourceId, size.first, size.second
    )
    MarkerInfoWindow(
        state = MarkerState(position = marker.latLng),
        title = marker.title, //TODO make this disappear when the window is closed
        icon = icon,
        onClick = {
            //if(marker.events.size == 1) eventForMarkerWindow.value = marker.events[0]
            eventsForMarkerWindow.value = marker.events.toMutableList()
            Log.d("pos do marker", marker.latLng.toString())
            currentMarker.value = marker
            cameraPositionState.position = CameraPosition.fromLatLngZoom(marker.latLng, 15f)
            showSearchBar.value = false
            showDialog.value = true
            false
        })
}

@Composable
fun ShowEventList(
    context: Context,
    eventsForMarkerWindow: MutableState<MutableList<Events>>,
    navController: NavController
) {
    val showOneEvent = remember() { mutableStateOf(false) }
    val selectedEvent = remember { mutableStateOf<Events?>(null) }
    eventsForMarkerWindow.value.sortBy { it.date }
    var cupGame = false
    eventsForMarkerWindow.value.forEach { if(it.isCupGame()) cupGame = true }
    if (!showOneEvent.value) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .padding(top = 15.dp)
                    .fillMaxWidth(.9f)
                    .fillMaxHeight(.4f)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(35.dp, 35.dp, 35.dp, 35.dp)
                    )
                    .align(Alignment.TopCenter)
            ) {
                Row {
                    Spacer(modifier = Modifier.weight(.4f))
                    if(!cupGame) {
                        Text(
                            text = eventsForMarkerWindow.value[0].homeTeam,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.weight(.4f))
                }
                LazyColumn(
                    Modifier.padding(top = 15.dp),
                    contentPadding = PaddingValues(top = 20.dp)
                ) {
                    items(eventsForMarkerWindow.value.toList()) { event ->
                        EventListItem(
                            event = event,
                            eventsForMarkerWindow,
                            showOneEvent,
                            selectedEvent
                        )
                    }
                }
            }
        }
    } else selectedEvent.value?.let {
        WindowMarkerDetails(event = it, context = context, showBackButton = true, navController) {
            showOneEvent.value = false
        }
    }


}

@Composable
fun EventListItem(
    event: Events,
    eventsForMarkerWindow: MutableState<MutableList<Events>>,
    showOneEvent: MutableState<Boolean>,
    selectedEvent: MutableState<Events?>
) {
    var color = Color.White
    if (event.importantGame) color = Color.Red
    val cupGame = event.isCupGame()
    Row(
        Modifier
            .fillMaxWidth()
            //.padding(top = 20.dp)
            .background(color)
            .clickable {
                selectedEvent.value = event
                showOneEvent.value = true
            }) {
        Image(
            painter = painterResource(id = getImage(event.eventType)),
            contentDescription = "icon",
            modifier = Modifier.size(39.dp)
        )
        Spacer(Modifier.size(10.dp))
        val textToShow = if(!cupGame) "vs ${event.awayTeam}" else {
            "${event.homeTeam} vs ${event.awayTeam}"
        }
        val size = if (textToShow.length < 35) 14 else 10
        Text(text = textToShow, fontWeight = FontWeight.Bold, fontSize = size.sp)
        Spacer(Modifier.size(10.dp))
        var d = event.date.toString().replace("T", " ")
        d = d.split(" ")[0]
        Text(text = d)
    }
}


@Composable
fun WindowMarkerDetails(
    event: Events,
    context: Context,
    showBackButton: Boolean = false,
    navController: NavController,
    onDismiss: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .padding(top = 15.dp)
                .fillMaxWidth(.9f)
                .fillMaxHeight(.4f)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(35.dp, 35.dp, 35.dp, 35.dp)
                )
                .align(Alignment.TopCenter)
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null
                ) { Log.d("click na box", "") },//to stop window from disappear on map drag
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(
                    modifier = Modifier
                        .size(10.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, top = 10.dp, end = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = event.homeTeamLogo,
                        contentDescription = "home_logo",
                        modifier = Modifier.size(50.dp)
                    )
                    val textSize = getTextSize("${event.homeTeam} vs ${event.awayTeam}".length)
                    Text(
                        text = "${event.homeTeam} vs ${event.awayTeam}",
                        fontSize = textSize.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 5.dp, top = 10.dp, end = 5.dp)
                    )
                    AsyncImage(
                        model = event.awayTeamLogo,
                        contentDescription = "away_logo",
                        modifier = Modifier.size(50.dp)
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
                    if (weather.value.lat == 0.0 && weather.value.lng == 0.0) {
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
                        if (weather.value.main != WeatherType.ERROR) "${ceil(weather.value.temp).toInt()}º C" else ""
                    Text(text = text, Modifier.padding(top = 5.dp))
                }
                Spacer(
                    modifier = Modifier
                        .size(10.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                    /*.clickable {
                        openBetclicApp(context)
                    }*/,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val odds = remember { mutableStateOf(Pair("0", "0")) }
                    if (event.homeOdd != "" && event.awayOdd != "") {
                        Log.d("vou usar as odds guardadas", "")
                        odds.value = Pair(event.homeOdd, event.awayOdd)
                    } else {
                        Log.d("vou buscar as odds", "")
                        getOddsForEvent(odds, event)
                    }
                    Image(
                        painter = painterResource(id = R.drawable.bet),
                        contentDescription = "odds",
                        modifier = Modifier
                            .size(50.dp)
                            .padding(top = 10.dp)
                            .clickable(
                                interactionSource = MutableInteractionSource(),
                                indication = null
                            ) {
                                openBetclicApp(context)
                            },//tava size 39.dp na img
                        alignment = Alignment.Center,
                    )
                    Spacer(Modifier.size(10.dp))
                    val size =
                        getTextSize("${event.homeTeam} ${odds.value.first} - ${odds.value.second} ${event.awayTeam}".length)
                    Text(
                        text = "${event.homeTeam} ${odds.value.first} - ${odds.value.second} ${event.awayTeam}",
                        fontSize = size.sp,
                        modifier = Modifier//.padding(top = 5.dp)
                    )
                }
                UpvoteOption(eventID = event.id.toString())
                TicketBuyOption(context, event)
                ShareOrRateOption(context, event, navController)
                //showReviewsOfMarker(event, navController)
                if (showBackButton) {
                    Row(horizontalArrangement = Arrangement.Center) {
                        Spacer(modifier = Modifier.weight(.4f)) // This creates a flexible space to push the button to the center
                        Button(onClick = onDismiss) { Text(text = "Back") }
                        Spacer(modifier = Modifier.weight(.4f))
                    }
                }
            }
        }
    }
}

@Composable
fun showReviewsOfMarker(event: Events, navController: NavController) {
    Button(onClick = {
        navController.navigate(
            Screens.Review.route.replace(
                oldValue = "{markerName}",
                newValue = event.markerLocations.title
            )
        )
    }) {
        Text(text = "Review ${event.markerLocations.type}")
    }
}

@Composable
fun UpvoteOption(eventID: String) {
    var image by remember {
        mutableStateOf(if (Global.upvotes.contains(eventID)) R.drawable.ic_upvoted else R.drawable.ic_upvote)
    }
    val size = 50
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(size.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Is this a big game?",
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp)
        )
        Spacer(Modifier.size(10.dp))
        Image(
            modifier = Modifier
                .size(size.dp)
                .clickable(interactionSource = MutableInteractionSource(), indication = null) {
                    image =
                        if (image == R.drawable.ic_upvoted) R.drawable.ic_upvote else R.drawable.ic_upvoted
                    upvoteClick(eventID)
                },
            painter = painterResource(id = image),
            contentDescription = "buy_ticket_img",
        )
    }
}

fun upvoteClick(eventID: String) {
    if (Global.upvotes.contains(eventID)) Global.upvotes.remove(eventID) else Global.upvotes.add(
        eventID
    )
    FirebaseFunctions.saveUserUpvotesInFirebase()
    FirebaseFunctions.changeEventUpvote(eventID, Global.upvotes.contains(eventID))
}

@Composable
fun TicketBuyOption(appContext: Context, event: Events) {
    val date = event.date.toString().split("T")[0]
    val linkUrl =
        "https://www.google.com/search?&q=comprar+bilhetes+${event.homeTeam}+${event.awayTeam}+$date"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .size(50.dp)
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null
            ) { openLink(appContext, linkUrl) }, horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Buy Tickets",
            fontWeight = FontWeight.Bold,
            color = Color.Blue,
            modifier = Modifier.padding(top = 5.dp)
        )
        Spacer(Modifier.size(10.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_link),
            contentDescription = "buy_ticket_img",
            modifier = Modifier.size(35.dp)
        )
    }
}

@Composable
fun ShareOrRateOption(appContext: Context, event: Events, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp), horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_share),
            contentDescription = "share_img",
            modifier = Modifier.size(35.dp).clickable {
                val date = event.date
                    .toString()
                    .replace("T", " ") + "h"
                shareContent(
                    context = appContext,
                    subject = "${event.eventType.type} game",
                    text = "${event.homeTeam} vs ${event.awayTeam} on $date",
                    latLng = event.markerLocations.latLng
                )
            })
        Spacer(modifier = Modifier.size(35.dp))
        Image(painter = painterResource(id = R.drawable.rating), contentDescription = "rate_button", modifier = Modifier.size(35.dp).clickable {
            navController.navigate(
                Screens.Review.route.replace(
                    oldValue = "{markerName}",
                    newValue = event.markerLocations.title
                )
            )
        })
    }
}

fun openLink(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

fun shareContent(context: Context, subject: String, text: String, latLng: LatLng) {
    //val locationUri = "geo:${latLng.latitude},${latLng.longitude}"
    val mapUrl = "https://www.google.com/maps?q=${latLng.latitude},${latLng.longitude}"

    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, "$text\n$mapUrl")

    val chooserIntent = Intent.createChooser(intent, "Share via")
    chooserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(chooserIntent)
}

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

fun getTextSize(size: Int, maxSize: Int = 25): Int = if (size > maxSize) 14 else 18