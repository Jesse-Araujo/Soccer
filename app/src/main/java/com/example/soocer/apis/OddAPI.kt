package com.example.soocer.apis

import android.util.Log
import androidx.compose.runtime.MutableState
import com.example.soocer.data.Events
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

        fun getOdds(uiVal: MutableState<Pair<String, String>>, event: Events, apiUrl : String, fullUrl:String) {

            //TODO tirar isto
            Log.d("odds", "poupar api")
            uiVal.value = Pair("xiu","xiu")
            event.homeOdd = "xiu"
            event.awayOdd = "xiu"
            return

            val bookmaker = 6

            val apiKey = "d0e33784e246dddf42f91ba3633549b8"

            val client = OkHttpClient()

            val request = Request.Builder()
                .url(fullUrl)
                //.url("$apiUrl/odds?fixture=$eventId&bookmaker=$bookmaker&season=$season")
                .header("x-rapidapi-host", apiUrl)
                .header("x-rapidapi-key", apiKey)
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                val response = client.newCall(request).execute()
                val odds = getOddFromJSONResponse(response.body?.string())
                Log.d("getting odds","${event.homeTeam} vs ${event.awayTeam}")
                withContext(Dispatchers.Main) {
                    Log.d("odds", odds.toString())
                    uiVal.value = odds
                    event.homeOdd = odds.first
                    event.awayOdd = odds.second
                }
            }

        }

        fun getOddFromJSONResponse(body: String?): Pair<String, String> {
            if (body == null) {
                return Pair("no info", "no info")
            }
            Log.d("odds", body)
            val output = JSONObject(body)
            if (output.getInt("results") == 0) return Pair("no info", "no info")
            val jsonArray = output.getJSONArray("response") as JSONArray
            val bookmakers = jsonArray.getJSONObject(0).getJSONArray("bookmakers")
            val bets = bookmakers.getJSONObject(0).getJSONArray("bets")
            val values = bets.getJSONObject(0).getJSONArray("values")
            val homeOdd = values.getJSONObject(0).getString("odd")
            val awayOdd = values.getJSONObject(2).getString("odd")
            Log.d("homeOdd",homeOdd)
            Log.d("awayOdd",awayOdd)
            return Pair(homeOdd, awayOdd)
        }
    }
}