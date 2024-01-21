package com.example.soocer.events

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import com.example.soocer.auxiliary.dateStringToLocalDateTime
import com.example.soocer.auxiliary.getDistanceBetweenTwoPoints
import com.example.soocer.auxiliary.getTimeFilterValue
import com.example.soocer.data.MarkerLocations
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Calendar.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class EventType(val type: String) {
    FOOTBALL("Football"),
    HANDBALL("Handball"),
    BASKETBALL("Basketball"),
    FUTSAL("Futsal"),
    VOLLEYBALL("Volleyball"),
    HOKEY("Hokey"),
    TENNIS("Tennis"),
}

class Events(
    val id: Int,
    val eventType: EventType,
    val league: String,
    val date: LocalDateTime,
    val city: String,
    val logo: String,
    val homeTeam: String,
    val homeTeamLogo: String,
    val awayTeam: String,
    val awayTeamLogo: String,
    val markerLocations: MarkerLocations,
    var importantGame: Boolean,
    var upvotes: Int,
    val comments: MutableList<String>,
) {

    var homeOdd = ""
    var awayOdd = ""


    @RequiresApi(Build.VERSION_CODES.O)
    override fun toString(): String {
        return "($homeTeam vs $awayTeam) on ${convertDateToString(date)} in $city, id: $id, big game > $importantGame\n"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertDateToString(date: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return date.format(formatter)
    }

    companion object {

        val events = mutableListOf<Events>()

        fun getHandballEvents(onFinished: (List<Events>?) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val events = HandballAPI.getHandballEvents()
                if (events != null) {
                    withContext(Dispatchers.Main) {
                        onFinished(events)
                    }
                }
            }
        }

        fun getFutsalEvents(onFinished: (List<Events>?) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val events = FutsalApi.getFutsalEvents()
                if (events != null) {
                    withContext(Dispatchers.Main) {
                        onFinished(events)
                    }
                }
            }
        }


        fun getBasketballEvents(onFinished: (List<Events>?) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val events = BasketballApi.getBasketballEvents()
                if (events != null) {
                    withContext(Dispatchers.Main) {
                        onFinished(events)
                    }
                }
            }
        }

        fun getVolleyballEvents(onFinished: (List<Events>?) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val events = VolleyballApi.getVolleyballEvents()
                if (events != null) {
                    withContext(Dispatchers.Main) {
                        onFinished(events)
                    }
                }
            }
        }

        //TODO adicionar champions, liga europa, taça da liga, taça de Portugal
        @RequiresApi(Build.VERSION_CODES.O)
        fun getFootballEvents(onFinished: (List<Events>?) -> Unit) {
            val apiUrl = "https://v3.football.api-sports.io/fixtures"
            val leagueId = 94
            val season = "2023"

            val apiKey = "d0e33784e246dddf42f91ba3633549b8"

            val client = OkHttpClient()

            val request = Request.Builder()
                .url("$apiUrl?league=$leagueId&season=$season")
                .header("x-rapidapi-host", "v3.football.api-sports.io")
                .header("x-rapidapi-key", apiKey)
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = client.newCall(request).execute()
                    val events = createFootballEvents(response.body?.string())
                    withContext(Dispatchers.Main) {
                        onFinished(events)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        onFinished(null)
                    }
                }
            }

        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun convertTimestampToTime(timestamp: Long): LocalDateTime {
            return Instant.ofEpochSecond(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun createFootballEvents(body: String?): List<Events>? {
            val events = mutableListOf<Events>()
            if (body == null) {
                return null
            }
            val output = JSONObject(body)
            val jsonArray = output.getJSONArray("response") as JSONArray
            for (i in 0 until jsonArray.length()) {
                val jsonObject1 = jsonArray.getJSONObject(i)
                val event = Events(
                    jsonObject1.getJSONObject("fixture").getInt("id"),
                    EventType.FOOTBALL,
                    jsonObject1.getJSONObject("league").getString("name"),
                    convertTimestampToTime(
                        jsonObject1.getJSONObject("fixture").getLong("timestamp")
                    ),
                    jsonObject1.getJSONObject("fixture").getJSONObject("venue").getString("city"),
                    jsonObject1.getJSONObject("league").getString("logo"),
                    jsonObject1.getJSONObject("teams").getJSONObject("home").getString("name"),
                    jsonObject1.getJSONObject("teams").getJSONObject("home").getString("logo"),
                    jsonObject1.getJSONObject("teams").getJSONObject("away").getString("name"),
                    jsonObject1.getJSONObject("teams").getJSONObject("away").getString("logo"),
                    MarkerLocations.getClubStadium(
                        jsonObject1.getJSONObject("teams").getJSONObject("home").getString("name")
                    ),
                    isBigGame(
                        jsonObject1.getJSONObject("teams").getJSONObject("home").getString("name"),
                        jsonObject1.getJSONObject("teams").getJSONObject("away").getString("name")
                    ), 0, mutableListOf()
                )
                events.add(event)
            }
            val currentDate = LocalDateTime.now()
            val endDate = currentDate.plusDays(60)

            return events.filter { event ->
                val eventDate = event.date
                eventDate.isEqual(currentDate) || (eventDate.isAfter(currentDate) && eventDate.isBefore(
                    endDate
                ))
            }
        }

        fun isBigGame(homeTeam: String, awayTeam: String): Boolean {
            val ht = homeTeam.lowercase()
            val at = awayTeam.lowercase()
            var team1BigTeam = false
            var team2BigTeam = false
            if (ht.contains("benfica") || ht.contains("sporting") || ht.contains("porto") || ht.contains(
                    "braga"
                ) || ht.contains("guima")
            ) {
                team1BigTeam = true
            }
            if (at.contains("benfica") || at.contains("sporting") || at.contains("porto") || at.contains(
                    "braga"
                ) || at.contains("guima")
            ) {
                team2BigTeam = true
            }
            return team1BigTeam && team2BigTeam
        }

        fun getEventsInDistance(
            events: MutableList<Events>?,
            filteredEvents: MutableList<Events>?,
            distance: Float,
            userLoc: LatLng,
            onFinished: (LatLng) -> Unit
        ) {
            Log.d("fi ls", filteredEvents?.size.toString())
            filteredEvents?.clear()
            Log.d("total events", events?.size.toString())
            Log.d("filtro por dist", distance.toString())
            if (distance == 0f) events?.forEach { filteredEvents?.add(it) }
            else {
                events?.forEach { event ->
                    if (getDistanceBetweenTwoPoints(
                            event.markerLocations.latLng,
                            userLoc
                        ) <= distance
                    ) {
                        filteredEvents?.add(event)
                    }
                }
                Log.d("filtered list", filteredEvents?.size.toString())
            }
            onFinished(LatLng(0.0, 0.0))
        }


        fun getEventsWithTimeFilter(
            allEvents: MutableList<Events>?,
            filteredEvents: MutableList<Events>?,
            timeFilter: MutableState<String>,
            currentSearch: MutableList<Events>?,
            filterDistance: MutableState<String>,
            userLoc: LatLng,
            sportsToShow: HashSet<String>
        ) {

            //Log.d("filtered events 1", filteredEvents?.size.toString())
            val distance = getDistance(filterDistance)
            val today = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dateStringToLocalDateTime(LocalDateTime.now().toString().substring(0, 16))
            } else {
                TODO("VERSION.SDK_INT < O")
            }

            val time = getTimeFilterValue(timeFilter.value)

            val maxTime = today.plus(time, ChronoUnit.DAYS)


            val ids = hashSetOf<String>()
            if (currentSearch?.isEmpty() == true) allEvents?.forEach { ids.add("${it.id}+${it.eventType.type}") }
            else currentSearch?.forEach { ids.add("${it.id}+${it.eventType.type}") }
            val newList = mutableListOf<Events>()
            allEvents?.forEach { event ->
                val id = "${event.id}+${event.eventType.type}"
                if (ids.contains(id)) {
                    if (event.eventType.type == "Basketball" && (/*event.homeTeam == "Vizela" ||*/ event.homeTeam.contains("Sporting"))) {
                        Log.d("event", event.toString())
                        Log.d("event date", event.date.toString())
                        Log.d("time", time.toString())
                        Log.d("maxTime", maxTime.toString())
                        Log.d("date hoje", today.toString())
                        Log.d(
                            "hoje?",
                            event.date.toLocalDate().isEqual(today.toLocalDate()).toString()
                        )
                        Log.d(
                            "depois de hoje?",
                            event.date.toLocalDate().isAfter(today.toLocalDate()).toString()
                        )
                        Log.d(
                            "antes do maxTime?",
                            event.date.toLocalDate().isBefore(maxTime.toLocalDate()).toString()
                        )
                    }
                    if (event.date.toLocalDate()
                            .isEqual(today.toLocalDate()) || (event.date.toLocalDate()
                            .isAfter(today.toLocalDate()) && (event.date.toLocalDate().isBefore(
                            maxTime.toLocalDate()) || event.date.toLocalDate().isEqual(maxTime.toLocalDate())
                        ))
                    ) {
                        if (getDistanceBetweenTwoPoints(
                                event.markerLocations.latLng,
                                userLoc
                            ) <= distance
                        ) {
                            if (sportsToShow.contains(event.eventType.type)) newList.add(event)
                        }
                    }
                }
            }
            filteredEvents?.clear()
            newList.forEach { filteredEvents?.add(it) }
            //Log.d("filtered events 2", filteredEvents?.size.toString())
        }

        fun getDistance(filterDistance: MutableState<String>): Float {
            return when (filterDistance.value) {
                "0.5km" -> .5f
                "1km" -> 1f
                "5km" -> 5f
                "15km" -> 15f
                else -> 999999f
            }
        }

    }
}