package com.example.soocer.events

import android.util.Log
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class OddAPI {

    companion object {

        fun getFootballOdd(eventId: Int, uiVal: MutableState<Pair<String, String>>, event: Events) {
            val apiUrl = "https://v3.football.api-sports.io"
            val season = "2023"
            val bookmaker = 6

            val apiKey = "d0e33784e246dddf42f91ba3633549b8"

            val client = OkHttpClient()

            val request = Request.Builder()
                .url("$apiUrl/odds?fixture=$eventId&bookmaker=$bookmaker&season=$season")
                .header("x-rapidapi-host", apiUrl)
                .header("x-rapidapi-key", apiKey)
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                val response = client.newCall(request).execute()
                val odds = getFootballOdd(response.body?.string())
                withContext(Dispatchers.Main) {
                    uiVal.value = odds
                    event.homeOdd = odds.first
                    event.awayOdd = odds.second
                }
            }

        }

        fun getFootballOdd(body: String?): Pair<String, String> {
            if (body == null) {
                return Pair("erro", "erro")
            }
            Log.d("odds", body)
            val output = JSONObject(body)
            if (output.getInt("results") == 0) return Pair("erro", "erro")
            val jsonArray = output.getJSONArray("response") as JSONArray
            val bookmakers = jsonArray.getJSONObject(0).getJSONArray("bookmakers")
            val bets = bookmakers.getJSONObject(0).getJSONArray("bets")
            val values = bets.getJSONObject(0).getJSONArray("values")
            val homeOdd = values.getJSONObject(0).getString("odd")
            val awayOdd = values.getJSONObject(2).getString("odd")
            return Pair(homeOdd, awayOdd)
        }
    }
}

//fun main() { OddAPI.getFootballOdd(1063602) }

/*1063602
                                                                                                    1063603
                                                                                                    1063604
                                                                                                    1063606
                                                                                                    1063607
                                                                                                    1063609
                                                                                                    1063610*/