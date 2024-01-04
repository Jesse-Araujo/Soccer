package com.example.soocer.data

import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import com.example.soocer.auxiliary.convertStringToBoolean
import com.example.soocer.auxiliary.dateStringToLocalDateTime
import com.example.soocer.auxiliary.getEventType
import com.example.soocer.auxiliary.getMarker
import com.example.soocer.auxiliary.isYesterday
import com.example.soocer.events.Events
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime

class FirebaseFunctions {

    companion object {

        val firebaseRTDB =
            "https://sports-app-24a12-default-rtdb.europe-west1.firebasedatabase.app/"
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
            Events.events.forEach {
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
    }
}