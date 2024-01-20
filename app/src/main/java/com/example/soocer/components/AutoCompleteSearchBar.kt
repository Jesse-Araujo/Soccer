package com.example.soocer.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.soocer.auxiliary.getDistanceBetweenTwoPoints
import com.example.soocer.events.Events
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoComplete(
    modifier: Modifier,
    events: MutableList<Events>?,
    showSearchBar: MutableState<Boolean>,
    showRecommendations: MutableState<Boolean>,
    filteredEvents: MutableList<Events>?,
    filteredDistance: MutableState<String>,
    currentSearch: MutableList<Events>?,
    userLoc: LatLng,
    onFinished: (Boolean, LatLng) -> Unit
) {
    val categories = getSearchTerms(events)

    var category by remember {
        mutableStateOf("")
    }

    val heightTextFields by remember {
        mutableStateOf(55.dp)
    }

    var textFieldSize by remember {
        mutableStateOf(Size.Zero)
    }
    val interactionSource = remember {
        MutableInteractionSource()
    }


    if(showSearchBar.value){
        // Category Field
        Column(
            modifier = modifier
                .padding(top = 15.dp, start = 30.dp, end = 30.dp, bottom = 10.dp)
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        showRecommendations.value = false
                    }
                )
        ) {

            Text(
                modifier = Modifier.padding(start = 3.dp, bottom = 2.dp),
                text = "Search",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )

            Column(modifier = Modifier.fillMaxWidth()) {

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(heightTextFields)
                            .border(
                                width = 1.8.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(15.dp)
                            )
                            .onGloballyPositioned { coordinates ->
                                textFieldSize = coordinates.size.toSize()
                            },
                        value = category,
                        onValueChange = {
                            category = it
                            Log.d("recomendaçoes","")
                            showRecommendations.value = true
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.White,
                            //backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        ),
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 16.sp
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                category = ""
                                showRecommendations.value = false
                                currentSearch?.clear()
                                onFinished(false,LatLng(0.0,0.0))
                            }) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "close",
                                    tint = Color.Red
                                )
                            }
                        }
                    )
                }

                AnimatedVisibility(visible = showRecommendations.value) {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .width(textFieldSize.width.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 150.dp),
                        ) {

                            if (category.isNotEmpty()) {
                                items(
                                    categories.filter {
                                        it.lowercase()
                                            .contains(category.lowercase()) || it.lowercase()
                                            .contains("others")
                                    }.sorted()
                                ) {
                                    CategoryItems(title = it,events,filteredEvents, currentSearch,filteredDistance,userLoc,onFinished) { title ->
                                        category = title
                                        showRecommendations.value = false
                                    }
                                }
                            } else {
                                items(
                                    categories.sorted()
                                ) {
                                    CategoryItems(title = it,events,filteredEvents,currentSearch,filteredDistance, userLoc,onFinished) { title ->
                                        category = title
                                        showRecommendations.value = false
                                    }
                                }
                            }
                        }
                    }
                }

            }

        }
    }

}

@Composable
fun CategoryItems(
    title: String,
    events: MutableList<Events>?,
    filteredEvents: MutableList<Events>?,
    currentSearch: MutableList<Events>?,
    distance : MutableState<String>,
    userLoc : LatLng,
    onFinished:  (Boolean,LatLng) -> Unit,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSelect(title)
                searchEvent(title, events, filteredEvents,currentSearch,distance,userLoc, onFinished)
            }
            .padding(10.dp)
    ) {
        Text(text = title, fontSize = 16.sp)
    }
}

fun searchEvent(title: String, events: MutableList<Events>?,filteredEvents: MutableList<Events>?,currentSearch: MutableList<Events>?,distance : MutableState<String>,userLoc : LatLng,onFinished:  (Boolean,LatLng) -> Unit) {
    Log.d("vou pesquisar",title)
    filteredEvents?.clear()
    when {
        title.startsWith("loc:") -> searchLoc(title.split("loc: ")[1],events,filteredEvents,currentSearch,onFinished)
        title.startsWith("club:") -> searchClub(title.split("club: ")[1],events,filteredEvents,currentSearch,onFinished)
        //title.startsWith("sport:") -> searchSport(title.split("sport: ")[1],events,filteredEvents,currentSearch,distance,userLoc,onFinished)
        else -> searchPlace(title.split(": ")[1],events,filteredEvents,currentSearch,onFinished)
    }
}

fun searchLoc(
    location: String,
    events: MutableList<Events>?,
    filteredEvents: MutableList<Events>?,
    currentSearch: MutableList<Events>?,
    onFinished: (Boolean, LatLng) -> Unit
) {
    Log.d("vou pesquisar location",location)
    events?.forEach { event ->
        if(event.markerLocations.city == location) currentSearch?.add(event)//filteredEvents?.add(event)
    }
    //Log.d("filtered list -> ",filteredEvents.value?.size.toString())
    onFinished(false, LatLng(0.0,0.0))
}

fun searchClub(
    club: String,
    events: MutableList<Events>?,
    filteredEvents: MutableList<Events>?,
    currentSearch: MutableList<Events>?,
    onFinished: (Boolean, LatLng) -> Unit
) {
    Log.d("vou pesquisar club",club)
    events?.forEach { event ->
        if(event.homeTeam == club || event.awayTeam == club) currentSearch?.add(event)//filteredEvents?.add(event)
    }
    onFinished(false,LatLng(0.0,0.0))
}

/*fun searchSport(
    sport: String,
    events: MutableList<Events>?,
    filteredEvents: MutableList<Events>?,
    currentSearch: MutableList<Events>?,
    distance: MutableState<String>,
    userLoc: LatLng,
    onFinished: (Boolean, LatLng) -> Unit
) {
    Log.d("vou pesquisar sport",sport)
    val dist = getDistance(distance.value)
    events?.forEach { event ->
        if(event.eventType.toString() == sport && getDistanceBetweenTwoPoints(event.markerLocations.latLng,userLoc) <= dist) currentSearch?.add(event)//filteredEvents?.add(event)
    }
    onFinished(false,LatLng(0.0,0.0))
}*/

fun searchPlace(
    place: String,
    events: MutableList<Events>?,
    filteredEvents: MutableList<Events>?,
    currentSearch: MutableList<Events>?,
    onFinished: (Boolean, LatLng) -> Unit
) {
    var loc : LatLng? = null
    events?.forEach { event ->
        if(event.markerLocations.title == place) {
            currentSearch?.add(event)
            //filteredEvents?.add(event)
            loc = event.markerLocations . latLng
        }
    }
    if(loc == null) onFinished(false,LatLng(0.0,0.0)) else onFinished(true,loc!!)
}


fun getSearchTerms(events: MutableList<Events>?) : List<String>{
    val list = hashSetOf<String>()
    events?.forEach{ event ->
        val loc = "loc: ${ event.markerLocations.city }"
        val clubH = "club: ${ event.homeTeam }"
        val clubA = "club: ${ event.awayTeam }"
        val sport = "sport: ${event.eventType.type}"
        val place = "${event.markerLocations.type}: ${event.markerLocations.title}"
        list.add(loc)
        list.add(clubH)
        list.add(clubA)
        list.add(sport)
        list.add(place)
    }
    return list.toList()
}

fun getDistance(dist : String) : Double{
    return when(dist) {
        "max" -> 9999999.0
        "0.5km" -> 0.5
        "1km" -> 1.0
        "5km" -> 5.0
        else -> 15.0
    }
}