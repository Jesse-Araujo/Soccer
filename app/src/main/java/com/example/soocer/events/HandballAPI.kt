package com.example.soocer.events

import android.os.Build
import com.example.soocer.data.MarkerLocations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime

class HandballAPI {
    companion object {
        //@RequiresApi(Build.VERSION_CODES.O)
        fun getHandballEvents(): List<Events>? {
            val apiUrl = "https://v1.handball.api-sports.io"
            val leagueId = 84
            val season = "2023"

            val apiKey = "d0e33784e246dddf42f91ba3633549b8"

            val client = OkHttpClient()

            val request = Request.Builder()
                .url("$apiUrl/games?league=$leagueId&season=$season")
                .header("x-rapidapi-host", apiUrl)
                .header("x-rapidapi-key", apiKey)
                .build()

            val response = client.newCall(request).execute()
            val events = getLeagueGames(response.body?.string())
            return events
        }

        fun getLeagueGames(body: String?): List<Events>? {
            val events = mutableListOf<Events>()
            if (body == null) {
                return null
            }
            val output = JSONObject(body)
            val jsonArray = output.getJSONArray("response") as JSONArray
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val event = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Events(
                        jsonObject.getInt("id"),
                        EventType.HANDBALL,
                        jsonObject.getJSONObject("league").getString("name"),
                        Events.convertTimestampToTime(
                            jsonObject.getLong("timestamp")
                        ),
                        "",
                        jsonObject.getJSONObject("league").getString("logo"),
                        jsonObject.getJSONObject("teams").getJSONObject("home").getString("name"),
                        jsonObject.getJSONObject("teams").getJSONObject("home").getString("logo"),
                        jsonObject.getJSONObject("teams").getJSONObject("away").getString("name"),
                        jsonObject.getJSONObject("teams").getJSONObject("away").getString("logo"),
                        MarkerLocations.getClubPavilion(
                            jsonObject.getJSONObject("teams").getJSONObject("home")
                                .getString("name")
                        ),
                        Events.isBigGame(
                            jsonObject.getJSONObject("teams").getJSONObject("home")
                                .getString("name"),
                            jsonObject.getJSONObject("teams").getJSONObject("away")
                                .getString("name")
                        ), mutableListOf()
                    )
                } else {
                    return null
                }
                events.add(event)
            }
            val currentDate = LocalDateTime.now()
            val endDate = currentDate.plusDays(60)//default is 8 para 1 semana

            return events.filter { event ->
                val eventDate = event.date
                eventDate.isEqual(currentDate) || (eventDate.isAfter(currentDate) && eventDate.isBefore(
                    endDate
                ))
            }
        }
    }

}