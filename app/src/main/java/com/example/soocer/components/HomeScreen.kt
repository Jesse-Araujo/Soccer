package com.example.soocer.components

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.soocer.R
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Switch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import com.example.soocer.auxiliary.Global
import com.example.soocer.data.FirebaseFunctions
import com.example.soocer.events.EventType
import com.example.soocer.location.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    appContext: Context,
    startService: (Intent) -> ComponentName?
) {
    val favSports = remember { mutableStateOf(hashSetOf<String>()) }
    LaunchedEffect(Unit) {
        FirebaseFunctions.getUserFavSports {
            Global.favSports.addAll(it)
            favSports.value = it
        }
    }
    LaunchedEffect(Unit) {
        FirebaseFunctions.getUserUpvotedGames() /*{
            Global.favSports.addAll(it)
            favSports.value = it
        }*/
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Home(favSports, appContext, startService)
        BottomNavigator(navController = navController)
    }
}


@Composable
fun Home(
    favSports: MutableState<HashSet<String>>,
    appContext: Context,
    startService: (Intent) -> ComponentName?
) {
    if (Global.favSports.isNotEmpty()) favSports.value = Global.favSports
    val size = 150
    val color = Color(0xFF06A00D)
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(color)
        ) {
            Text(
                text = "Sports Events",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(
                    Alignment.Center
                )
            )
        }
        Column(modifier = Modifier.padding(top = 100.dp)) {
            SportCard(
                size = size,
                img1 = EventType.FOOTBALL.type,
                img2 = EventType.HANDBALL.type,
                favSports = favSports,
            )
            SportCard(
                size = size,
                img1 = EventType.BASKETBALL.type,
                img2 = EventType.VOLLEYBALL.type,
                favSports = favSports,
            )
            SportCard(
                size = size,
                img1 = EventType.TENNIS.type,
                img2 = EventType.FUTSAL.type,
                favSports = favSports,
            )
            LocationServiceControls(appContext, startService)
        }
    }
}

@Composable
fun SportCard(
    size: Int,
    img1: String,
    img2: String,
    favSports: MutableState<HashSet<String>>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SportImage(size, img1, favSports)
        SportImage(size, img2, favSports)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SportImage(
    size: Int, sport: String, favSports: MutableState<HashSet<String>>,
) {
    var show by remember { mutableStateOf(false) }
    show = favSports.value.contains(sport)
    Box() {
        if (show) {
            Image(
                painter = painterResource(id = R.drawable.star),
                contentDescription = "${sport}_star",
                modifier = Modifier.size(15.dp)
            )
        }
        Image(
            modifier = Modifier
                .size(size.dp)
                .combinedClickable(
                    onClick = { onSportClick(sport) },
                    onLongClick = {
                        Log.d("show", show.toString())
                        show = !show
                        Log.d("show", show.toString())
                        onSportLongClick(sport, favSports)
                    }
                ),
            painter = painterResource(id = getId(sport)),
            contentDescription = "${sport}_img",
        )
    }
}

fun onSportClick(sport: String) {
    Log.d(sport, "")
}

fun onSportLongClick(
    sport: String, favSports: MutableState<HashSet<String>>,
) {
    if (favSports.value.contains(sport)) favSports.value.remove(sport) else favSports.value.add(
        sport
    )
    CoroutineScope(Dispatchers.IO).launch {
        FirebaseFunctions.saveUserFavSportsInFirebase(favSports.value)
    }
}

fun getId(eventType: String): Int {
    return when (eventType) {
        EventType.FOOTBALL.type -> R.drawable.fut_img
        EventType.FUTSAL.type -> R.drawable.futs_img
        EventType.HANDBALL.type -> R.drawable.andebol_img
        EventType.HOKEY.type -> R.drawable.hokey_img
        EventType.VOLLEYBALL.type -> R.drawable.volley_img
        EventType.BASKETBALL.type -> R.drawable.basket_img
        EventType.TENNIS.type -> R.drawable.tennis_img
        else -> R.drawable.judo_img
    }
}

@Composable
fun LocationServiceControls(appContext: Context, startService: (Intent) -> ComponentName?) {
    val isLocationServiceRunning = isServiceRunning(appContext, LocationService::class.java)
    var checked by remember {
        mutableStateOf(
            isServiceRunning(
                appContext,
                LocationService::class.java
            )
        )
    }
    Row (modifier = Modifier.fillMaxWidth().padding(end = 5.dp), horizontalArrangement = Arrangement.End){
        Text(text = "Notifications",modifier = Modifier.align(Alignment.CenterVertically).padding(end = 5.dp))
            Switch(
                checked = checked,
                onCheckedChange = {
                    checked = it
                    //Start service if switch is on and service is off
                    if (it && !isLocationServiceRunning) {
                        Intent(appContext, LocationService::class.java).apply {
                            action = LocationService.ACTION_START
                            startService(this)
                        }
                    }
                    //Stop service
                    if (!it) {
                        Intent(appContext, LocationService::class.java).apply {
                            action = LocationService.ACTION_STOP
                            startService(this)
                        }
                    }
                }
            )

    }
}

fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val services = manager.getRunningServices(Integer.MAX_VALUE)

    for (service in services) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}