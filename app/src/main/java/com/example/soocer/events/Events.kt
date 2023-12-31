package com.example.soocer.events

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import com.example.soocer.auxiliary.convertStringToBoolean
import com.example.soocer.auxiliary.dateStringToLocalDateTime
import com.example.soocer.auxiliary.getEventType
import com.example.soocer.auxiliary.getMarker
import com.example.soocer.auxiliary.isYesterday
import com.example.soocer.data.MarkerLocations
import com.example.soocer.data.Type
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

enum class EventType(val type: String) {
    FOOTBALL("Football"),
    HANDBALL("Handball"),
    BASKETBALL("Basketball"),
    FUTSAL("Futsal"),
    VOLLEYBALL("Volleyball"),
    HOKEY("Hokey"),
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
    val importantGame: Boolean,
    val comments: MutableList<String>,
) {

    @RequiresApi(Build.VERSION_CODES.O)
    constructor() : this(
        0, EventType.FOOTBALL, "",
        LocalDateTime.now(), "", "", "", "", "", "", MarkerLocations(
            "", LatLng(0.0, 0.0), Type.STADIUM, 0, "", 0,
            hashSetOf()
        ), false, mutableListOf()
    )

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

        val firebaseRTDB =
            "https://sports-app-24a12-default-rtdb.europe-west1.firebasedatabase.app/"

        val events = mutableListOf<Events>()
        var saveInFirebase = true

        fun getDataFromFirebase(
            receivedData: MutableState<Boolean>,
            onFinished: (List<Events>) -> Unit
        ) {
            val db = FirebaseDatabase.getInstance(firebaseRTDB)
            val eventsDb = db.reference.child("events")
            db.reference.child("timestamp")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            dateStringToLocalDateTime(dataSnapshot.value.toString())
                        } else {
                            TODO("VERSION.SDK_INT < O")
                        }
                        Log.d("timestamp", date.toString())
                        if (isYesterday(date)) {
                            Log.d("info de ontem", "nao quero")
                            receivedData.value = false
                            onFinished(mutableListOf())
                        } else {
                            readFromDB(eventsDb, receivedData, onFinished)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("Error getting events:", databaseError.toException())
                    }
                })
            saveInFirebase = true
        }

        fun readFromDB(
            eventsDb: DatabaseReference, receivedData: MutableState<Boolean>,
            onFinished: (List<Events>) -> Unit
        ) {
            eventsDb.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val eventsList = mutableListOf<Events>()
                    Log.d("info de hoje", "quero")
                    for (snapshot in dataSnapshot.children) {
                        //Log.d("snapshot", snapshot.toString())
                        val id = snapshot.child("id").getValue(Long::class.java)!!
                        val eventType =
                            getEventType(snapshot.child("eventType").getValue(String::class.java))
                        val league = snapshot.child("league").getValue(String::class.java)!!
                        val date = snapshot.child("date").getValue(String::class.java)!!
                        val city = snapshot.child("city").getValue(String::class.java)!!
                        val logo = snapshot.child("logo").getValue(String::class.java)!!
                        val homeTeam = snapshot.child("homeTeam").getValue(String::class.java)!!
                        val homeTeamLogo =
                            snapshot.child("homeTeamLogo").getValue(String::class.java)!!
                        val awayTeam = snapshot.child("awayTeam").getValue(String::class.java)!!
                        val awayTeamLogo =
                            snapshot.child("awayTeamLogo").getValue(String::class.java)!!
                        val importantGame =
                            snapshot.child("importantGame").getValue(String::class.java)!!
                        val comments = snapshot.child("comments").getValue(String::class.java)
                        //TODO handle comments
                        val e = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Events(
                                id.toInt(),
                                eventType,
                                league,
                                dateStringToLocalDateTime(date),
                                city,
                                logo,
                                homeTeam,
                                homeTeamLogo,
                                awayTeam,
                                awayTeamLogo,
                                getMarker(homeTeam, eventType),
                                convertStringToBoolean(importantGame),
                                mutableListOf()
                            )
                        } else {
                            TODO("VERSION.SDK_INT < O")
                        }
                        eventsList.add(e)
                    }
                    Log.d("li da firebase", "")
                    receivedData.value = false
                    onFinished(eventsList)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("Error getting events:", databaseError.toException())
                    receivedData.value = false
                }
            })
        }

        fun saveDataInFirebase() {
            val db = FirebaseDatabase.getInstance(firebaseRTDB)
            db.reference.child("events").setValue(null)
            db.reference.child("timestamp").setValue(null)
            events.forEach {
                val eventsDb = db.reference.child("events").push()
                eventsDb.child("id").setValue(it.id)
                eventsDb.child("eventType").setValue(it.eventType.type)
                eventsDb.child("league").setValue(it.league)
                eventsDb.child("date").setValue(it.date.toString())
                eventsDb.child("city").setValue(it.city)
                eventsDb.child("logo").setValue(it.logo)
                eventsDb.child("homeTeam").setValue(it.homeTeam)
                eventsDb.child("homeTeamLogo").setValue(it.homeTeamLogo)
                eventsDb.child("awayTeam").setValue(it.awayTeam)
                eventsDb.child("awayTeamLogo").setValue(it.awayTeamLogo)
                eventsDb.child("markerLocations").setValue("")
                eventsDb.child("importantGame").setValue(it.importantGame.toString())
                eventsDb.child("comments").setValue(it.comments.toString())
            }
            var timestamp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime.now().toString().split(".")[0]
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            timestamp = timestamp.substring(0, timestamp.length - 3)
            db.reference.child("timestamp").setValue(timestamp)
        }

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

        //TODO adicionar champions, liga europa, taça da liga, taça de Portugal
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
            //val jsonObject = JSONObject(body)
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
                    ), mutableListOf()
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

    }
}