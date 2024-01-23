package com.example.soocer.apis

import android.os.Build
import com.example.soocer.data.EventType
import com.example.soocer.data.Events
import com.example.soocer.data.MarkerLocations
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime

class FootballApi {

    companion object {

        fun getFootballEvents(onFinished: (List<Events>?) -> Unit): List<Events>? {
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

            /*CoroutineScope(Dispatchers.IO).launch {
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
            }*/
            val response = client.newCall(request).execute()
            return createFootballEvents(response.body?.string())
        }
        fun getFootballCupGames(): List<Events>? {
            val apiUrl = "https://v3.football.api-sports.io/fixtures"
            val leagueId = 97
            val season = "2023"

            val apiKey = "d0e33784e246dddf42f91ba3633549b8"

            val client = OkHttpClient()

            val request = Request.Builder()
                .url("$apiUrl?league=$leagueId&season=$season")
                .header("x-rapidapi-host", "v3.football.api-sports.io")
                .header("x-rapidapi-key", apiKey)
                .build()

            val response = client.newCall(request).execute()
            return createFootballEvents(response.body?.string(),false)
        }

        fun createFootballEvents(body: String?,leagueGame: Boolean = true): List<Events>? {
            val events = mutableListOf<Events>()
            if (body == null) {
                return null
            }
            val output = JSONObject(body)
            val jsonArray = output.getJSONArray("response") as JSONArray
            for (i in 0 until jsonArray.length()) {
                val jsonObject1 = jsonArray.getJSONObject(i)
                val stadium = if(leagueGame) MarkerLocations.getClubStadium(
                    jsonObject1.getJSONObject("teams").getJSONObject("home").getString("name")
                ) else MarkerLocations.getStadiumForFootballCup(
                    jsonObject1.getJSONObject("fixture").getJSONObject("venue").getString("name")
                )
                val event = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Events(
                        jsonObject1.getJSONObject("fixture").getInt("id"),
                        EventType.FOOTBALL,
                        jsonObject1.getJSONObject("league").getString("name"),
                        Events.convertTimestampToTime(
                            jsonObject1.getJSONObject("fixture").getLong("timestamp")
                        ),
                        jsonObject1.getJSONObject("league").getString("round"),//TODO mudar para meia final ou final etc
                        jsonObject1.getJSONObject("league").getString("logo"),
                        jsonObject1.getJSONObject("teams").getJSONObject("home").getString("name"),
                        jsonObject1.getJSONObject("teams").getJSONObject("home").getString("logo"),
                        jsonObject1.getJSONObject("teams").getJSONObject("away").getString("name"),
                        jsonObject1.getJSONObject("teams").getJSONObject("away").getString("logo"),
                        stadium,
                        Events.isBigGame(
                            jsonObject1.getJSONObject("teams").getJSONObject("home").getString("name"),
                            jsonObject1.getJSONObject("teams").getJSONObject("away").getString("name")
                        ), 0, mutableListOf()
                    )
                } else {
                    TODO("VERSION.SDK_INT < O")
                }
                events.add(event)
            }
            val currentDate = LocalDateTime.now()
            val endDate = currentDate.plusDays(16)

            return events.filter { event ->
                val eventDate = event.date
                eventDate.isEqual(currentDate) || (eventDate.isAfter(currentDate) && eventDate.isBefore(
                    endDate
                ))
            }
        }
    }
}

fun main() {
    FootballApi.getFootballCupGames()
}