package com.example.soocer.events

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.soocer.data.MarkerLocations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Calendar.*
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject
import java.security.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

enum class EventType() {
    FOOTBALL,
    HANDBALL,
    BASKETBALL,
    FUTSAL,
    VOLLEYBALL,
    HOKEY,
}

class Events(
    val id: Int,
    val eventType: EventType,
    val league: String,
    val date: LocalDateTime,
    val city: String,
    val logo: String,
    val homeTeam : String,
    val homeTeamLogo : String,
    val awayTeam : String,
    val awayTeamLogo : String,
    val markerLocations: MarkerLocations,
    val importantGame : Boolean

) {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun toString(): String {
        return "($homeTeam vs $awayTeam) on ${convertDateToString(date)} in $city\n"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertDateToString(date: LocalDateTime) :String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return date.format(formatter)
    }

    companion object {

        var x = "called"

        //val events = mutableListOf<Events>()

        @RequiresApi(Build.VERSION_CODES.O)
        fun getFootballEvents(onFinished: (List<Events>?) -> Unit) {
            val apiUrl = "https://v3.football.api-sports.io/fixtures"
            val leagueId = 94
            val season = "2023"

            // Replace "YOUR_API_KEY" with your actual API key
            val apiKey = "d0e33784e246dddf42f91ba3633549b8"

            // Set up an OkHttpClient
            val client = OkHttpClient()

            // Build the request with headers and parameters
            val request = Request.Builder()
                .url("$apiUrl?league=$leagueId&season=$season")
                .header("x-rapidapi-host", "v3.football.api-sports.io")
                .header("x-rapidapi-key", apiKey)
                .build()

            /*CoroutineScope(Dispatchers.IO).launch {
                val response = client.newCall(request).execute()
                onFinished(createFootballEvents(response.body?.string()))
            }*/

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
        private fun convertTimestampToTime(timestamp: Long): LocalDateTime {
            return Instant.ofEpochSecond(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun createFootballEvents(body: String?) : List<Events>? {
            val events = mutableListOf<Events>()
            if (body == null) {
                return null
            }
            //val jsonObject = JSONObject(body)
            val output = JSONObject(body)
            val jsonArray = output.getJSONArray("response") as JSONArray
            for (i in 0 until jsonArray.length()) {
                val jsonObject1 = jsonArray.getJSONObject(i)
                val event = Events(
                    jsonObject1.getJSONObject("fixture").getInt("id"),
                    EventType.FOOTBALL,
                    jsonObject1.getJSONObject("league").getString("name"),
                    convertTimestampToTime(jsonObject1.getJSONObject("fixture").getLong("timestamp")),
                    jsonObject1.getJSONObject("fixture").getJSONObject("venue").getString("city"),
                    jsonObject1.getJSONObject("league").getString("logo"),
                    jsonObject1.getJSONObject("teams").getJSONObject("home").getString("name"),
                    jsonObject1.getJSONObject("teams").getJSONObject("home").getString("logo"),
                    jsonObject1.getJSONObject("teams").getJSONObject("away").getString("name"),
                    jsonObject1.getJSONObject("teams").getJSONObject("away").getString("logo"),
                    MarkerLocations.getClubStadium(jsonObject1.getJSONObject("teams").getJSONObject("home").getString("name")),
                    false//TODO implement this
                )
                events.add(event)
            }
            val currentDate = LocalDateTime.now()
            val endDate = currentDate.plusDays(8)

            return events.filter { event ->
                val eventDate = event.date
                eventDate.isEqual(currentDate) || (eventDate.isAfter(currentDate) && eventDate.isBefore(endDate))
            }
        }


    }
}