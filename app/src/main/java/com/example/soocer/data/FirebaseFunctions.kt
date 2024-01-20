package com.example.soocer.data

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import com.example.soocer.auxiliary.Global
import com.example.soocer.auxiliary.convertStringToBoolean
import com.example.soocer.auxiliary.dateStringToLocalDateTime
import com.example.soocer.auxiliary.getEventType
import com.example.soocer.auxiliary.getMarker
import com.example.soocer.auxiliary.isYesterday
import com.example.soocer.auxiliary.newAverage
import com.example.soocer.auxiliary.updateAverage
import com.example.soocer.components.stringToBitmap
import com.example.soocer.events.Events
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import kotlin.math.ceil

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

        private fun readFromDB(
            eventsDb: DatabaseReference, receivedData: MutableState<Boolean>,
            onFinished: (List<Events>) -> Unit
        ) {
            eventsDb.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val eventsList = mutableListOf<Events>()
                    Log.d("info de hoje", "quero")
                    for (snapshot in dataSnapshot.children) {
                        //if(snapshot.key != "events") continue
                        val id = snapshot.child("id").getValue(Long::class.java) ?: continue
                        val eventType =
                            getEventType(snapshot.child("eventType").getValue(String::class.java))
                        val league =
                            snapshot.child("league").getValue(String::class.java) ?: continue
                        val date = snapshot.child("date").getValue(String::class.java) ?: continue
                        val city = snapshot.child("city").getValue(String::class.java) ?: continue
                        val logo = snapshot.child("logo").getValue(String::class.java) ?: continue
                        val homeTeam =
                            snapshot.child("homeTeam").getValue(String::class.java) ?: continue
                        val homeTeamLogo =
                            snapshot.child("homeTeamLogo").getValue(String::class.java) ?: continue
                        val awayTeam =
                            snapshot.child("awayTeam").getValue(String::class.java) ?: continue
                        val awayTeamLogo =
                            snapshot.child("awayTeamLogo").getValue(String::class.java) ?: continue
                        //val importantGame = snapshot.child("importantGame").getValue(String::class.java) ?: continue
                        val upvotes = snapshot.child("upvotes").getValue(Long::class.java) ?: 0
                        val importantGame = upvotes >= 2 || Events.isBigGame(homeTeam, awayTeam)

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
                                //convertStringToBoolean(importantGame),
                                importantGame,
                                upvotes.toInt(),
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
            getAllUpvotes(db.reference.child("events")) { map ->
                db.reference.child("events").setValue(null)
                db.reference.child("timestamp").setValue(null)
                Log.d("events save DB", Events.events.toString())
                Events.events.forEach {
                    val pair = map[it.id.toLong()]
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
                    //eventsDb.child("importantGame").setValue(it.importantGame.toString())
                    //eventsDb.child("upvotes").setValue(it.upvotes)
                    val bol =
                        (pair?.first ?: 0) >= 2 || convertStringToBoolean(pair?.second ?: "false")
                    if (it.id == 357373) {
                        Log.d("first", (pair?.first ?: 0).toString())
                        Log.d("second", (pair?.second ?: "false").toString())
                        Log.d("bol", bol.toString())
                    }
                    it.importantGame = bol
                    eventsDb.child("importantGame").setValue(bol.toString())
                    eventsDb.child("upvotes").setValue(pair?.first ?: 0L)
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

        fun getAllUpvotes(
            eventsDb: DatabaseReference,
            onFinished: (HashMap<Long, Pair<Long, String>>) -> Unit
        ) {
            val upvotesBigGameList = hashMapOf<Long, Pair<Long, String>>()
            eventsDb.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val id = snapshot.child("id").getValue(Long::class.java) ?: continue
                        val upvotes = snapshot.child("upvotes").getValue(Long::class.java) ?: 0
                        val importantGame =
                            snapshot.child("importantGame").getValue(String::class.java) ?: continue
                        upvotesBigGameList[id] = Pair(upvotes, importantGame)
                    }
                    onFinished(upvotesBigGameList)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }

        fun saveUserFavSportsInFirebase(favSports: HashSet<String>) {
            Global.favSports.addAll(favSports)
            val db = FirebaseDatabase.getInstance(firebaseRTDB)
            Log.d("id qd dou delitus", Global.userId)
            if (Global.userId.isNotEmpty()) db.reference.child(Global.userId).setValue(null)
            Log.d("vou meter", favSports.toString())
            favSports.forEach {
                val eventsDb = db.reference.child(Global.userId).push()
                eventsDb.child("sport").setValue(it)
            }
        }

        fun getUserFavSports(onFinished: (HashSet<String>) -> Unit) {
            val db = FirebaseDatabase.getInstance(firebaseRTDB)
            val favSportsDB = db.reference.child(Global.userId)

            favSportsDB.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val favSports = hashSetOf<String>()
                    for (snapshot in dataSnapshot.children) {
                        Log.d("snapshot", snapshot.toString())
                        //if(snapshot.key != Global.userID.txt) continue
                        Log.d("user sports", snapshot.toString())
                        Log.d("user sports", snapshot.children.toString())
                        //for (sn in snapshot.children) {
                        //}
                        Log.d("user sports", snapshot.toString())
                        val eventType = snapshot.child("sport")
                            .getValue(String::class.java)//getEventType(snapshot.child("sport").getValue(String::class.java))
                        Log.d("type", eventType.toString())
                        if (eventType != null) {
                            favSports.add(eventType)
                        }
                    }
                    Global.favSports.addAll(favSports)
                    onFinished(favSports)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("Error getting fav sports:", databaseError.toException())
                }
            })
        }

        fun saveUserUpvotesInFirebase() {
            //Global.upvotes.addAll(upvotes)
            val db = FirebaseDatabase.getInstance(firebaseRTDB)
            if (Global.userId.isNotEmpty()) db.reference.child(Global.userId).child("upvotes")
                .setValue(null)
            Global.upvotes.forEach {
                val eventsDb = db.reference.child(Global.userId).child("upvotes").push()
                eventsDb.child("eventID").setValue(it)
            }
        }

        fun getUserUpvotedGames() {
            val db = FirebaseDatabase.getInstance(firebaseRTDB)
            val userDb = db.reference.child(Global.userId).child("upvotes")

            userDb.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val upvotes = hashSetOf<String>()
                    for (snapshot in dataSnapshot.children) {
                        Log.d("upvote snap", snapshot.toString())
                        val upvoteId = snapshot.child("eventID").getValue(String::class.java)
                        Log.d("upvote id", upvoteId.toString())
                        if (upvoteId != null) {
                            upvotes.add(upvoteId)
                        }
                    }
                    Global.upvotes.addAll(upvotes)
                    //onFinished(favSports)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("Error getting fav sports:", databaseError.toException())
                }
            })
        }

        fun changeEventUpvote(eventId: String, upvote: Boolean) {
            val db = FirebaseDatabase.getInstance(firebaseRTDB)
            db.reference.child("events").get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val id = snapshot.child("id").getValue(Long::class.java) ?: continue
                        if (id.toString() == eventId) {
                            var upvotes = snapshot.child("upvotes").getValue(Int::class.java) ?: 0
                            if (upvote) upvotes++ else upvotes--
                            snapshot.ref.child("upvotes").setValue(upvotes)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener {
                                }
                        }
                    }
                } else {
                    // No events found
                    // Handle accordingly
                }
            }.addOnFailureListener {
            }
        }

        fun getUserReview(markerName: String, onFinished: (Review, Boolean) -> Unit) {
            val db = FirebaseDatabase.getInstance(firebaseRTDB)
            val userDb = db.reference.child("markers")
            var found = false
            userDb.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        //Log.d("review snap", snapshot.toString())
                        val reviewName = snapshot.child("markerName").getValue(String::class.java)
                        Log.d("reviewName", reviewName.toString())
                        if (reviewName != null && reviewName == markerName) {
                            for (userSnapShot in snapshot.child("users").children) {
                                val userId =
                                    userSnapShot.child("userId").getValue(String::class.java)
                                if (userId == Global.userId) {
                                    Log.d("snapshot user", userSnapShot.toString())
                                    Log.d("encontrei user", Global.userId)
                                    val globalRating =
                                        userSnapShot.child("globalRating").getValue(Int::class.java)
                                            ?: 5
                                    val comfort =
                                        userSnapShot.child("comfort").getValue(Int::class.java) ?: 5
                                    val accessibility =
                                        userSnapShot.child("accessibility")
                                            .getValue(Int::class.java) ?: 5
                                    val quality =
                                        userSnapShot.child("quality").getValue(Int::class.java) ?: 5
                                    val comment =
                                        userSnapShot.child("comment").getValue(String::class.java)
                                            ?: ""
                                    val photo =
                                        userSnapShot.child("photo").getValue(String::class.java)
                                            ?: ""
                                    val review = Review(
                                        reviewName,
                                        globalRating,
                                        comfort,
                                        accessibility,
                                        quality,
                                        comment,
                                        photo
                                    )
                                    found = true
                                    onFinished(review, found)
                                    break
                                }
                            }
                        }
                    }
                    if (!found) {
                        onFinished(Review("", 5, 5, 5, 5, "", ""), found)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("Error getting fav sports:", databaseError.toException())
                }
            })
        }

        fun getMarkerReview(
            markerName: String,
            onFinished: (Review, MutableList<Triple<String, String, Bitmap?>>) -> Unit
        ) {
            val db = FirebaseDatabase.getInstance(firebaseRTDB)
            val markerDb = db.reference.child("markers")
            var found = false
            markerDb.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val reviews = mutableListOf<Triple<String, String, Bitmap?>>()
                    for (snapshot in dataSnapshot.children) {
                        val markerNameDb = snapshot.child("markerName").getValue(String::class.java)
                        if (markerNameDb != null && markerName == markerNameDb) {
                            Log.d("marker snap", snapshot.toString())
                            val reviewName =
                                snapshot.child("markerName").getValue(String::class.java)
                            val globalRating =
                                snapshot.child("globalRating").getValue(Int::class.java) ?: 5
                            val comfort = snapshot.child("comfort").getValue(Int::class.java) ?: 5
                            val accessibility =
                                snapshot.child("accessibility").getValue(Int::class.java) ?: 5
                            val quality = snapshot.child("quality").getValue(Int::class.java) ?: 5
                            /*val comments: HashSet<String> = snapshot.child("comments")
                                .getValue(object : GenericTypeIndicator<List<String>>() {})
                                ?.toHashSet()
                                ?: hashSetOf()
                            val photos: List<String> = snapshot.child("photos")
                                .getValue(object : GenericTypeIndicator<List<String>>() {})
                                ?: emptyList()*/

                            for (markerSnapshot in snapshot.child("users").children) {
                               /* Log.d("$reviewName snap", markerSnapshot.toString())
                                comments.add(
                                    markerSnapshot.child("comment").getValue(String::class.java)
                                        ?: ""
                                )
                                photos.add(
                                    markerSnapshot.child("photo").getValue(String::class.java) ?: ""
                                )*/
                                val photo = markerSnapshot.child("photo").getValue(String::class.java) ?: ""
                                val bitmap = stringToBitmap(photo)
                                reviews.add(
                                    Triple(
                                        markerSnapshot.child("globalRating")
                                            .getValue(Int::class.java).toString()
                                            ?: "",
                                        markerSnapshot.child("comment").getValue(String::class.java)
                                            ?: "",
                                        bitmap
                                    )
                                )
                            }
                            Log.d("reviewName", reviewName.toString())
                            if (reviewName != null && reviewName == markerName) {
                                val review = Review(
                                    reviewName,
                                    globalRating,
                                    comfort,
                                    accessibility,
                                    quality,
                                    "",
                                    ""
                                )/*
                                val photosBitmap = mutableListOf<Bitmap>()
                                photos.forEach {
                                    val bitmap = stringToBitmap(it)
                                    if (bitmap != null) photosBitmap.add(bitmap)
                                }*/
                                onFinished(review,reviews)//, comments, photosBitmap)
                                found = true
                                break
                            }
                        }
                    }
                    if (!found) {
                        onFinished(
                            Review("", 5, 5, 5, 5, "", ""),
                            emptyList<Triple<String,String,Bitmap?>>().toMutableList()
                            //emptyList<String>().toHashSet(),
                            //emptyList<Bitmap>().toMutableList()
                        )
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("Error getting fav sports:", databaseError.toException())
                }
            })
        }


        fun saveUserReview(review: Review,onFinished: () -> Unit) {
            val db = FirebaseDatabase.getInstance(firebaseRTDB)
            val userDb = db.reference.child("markers")
            var found = false
            userDb.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val markerName = snapshot.child("markerName").getValue(String::class.java)
                        if (markerName != null && review.markerName == markerName) {
                            for (markerSnapshot in snapshot.child("users").children) {
                                val userId =
                                    markerSnapshot.child("userId").getValue(String::class.java)
                                if (userId == Global.userId) {
                                    Log.d("encontrei",Global.userId)
                                    Log.d("comm",review.comment)
                                    Log.d("glob",review.globalRating.toString())
                                    val userToUpdateRef = markerSnapshot.ref
                                    userToUpdateRef.child("globalRating")
                                        .setValue(review.globalRating)
                                    userToUpdateRef.child("comfort").setValue(review.comfort)
                                    userToUpdateRef.child("accessibility")
                                        .setValue(review.accessibility)
                                    userToUpdateRef.child("quality").setValue(review.quality)
                                    userToUpdateRef.child("comment").setValue(review.comment)
                                    userToUpdateRef.child("photo").setValue(review.photo)
                                    found = true
                                }
                            }
                            if (!found) {
                                snapshot.child("users").children
                                val newUserRef = snapshot.child("users").ref.push()
                                newUserRef.child("userId").setValue(Global.userId)
                                newUserRef.child("globalRating").setValue(review.globalRating)
                                newUserRef.child("comfort").setValue(review.comfort)
                                newUserRef.child("accessibility").setValue(review.accessibility)
                                newUserRef.child("quality").setValue(review.quality)
                                newUserRef.child("comment").setValue(review.comment)
                                newUserRef.child("photo").setValue(review.photo)
                            }

                            break
                        }
                    }
                    onFinished()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("Error getting fav sports:", databaseError.toException())
                }
            })
        }

        fun updateMarkerReview(
            review: Review,
            userHasAlreadyReviewed: Boolean,
            oldReview: Review,
            onFinished: (Unit) -> Unit
        ) {
            val db = FirebaseDatabase.getInstance(firebaseRTDB)
            val markerDb = db.reference.child("markers")
            var found = false
            markerDb.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        Log.d("snapshot keru", snapshot.toString())
                        val markerName = snapshot.child("markerName").getValue(String::class.java)
                        if (markerName != null && review.markerName == markerName) {

                            val globalRating =
                                snapshot.child("globalRating").getValue(Int::class.java) ?: 5
                            val comfort = snapshot.child("comfort").getValue(Int::class.java) ?: 5
                            val accessibility =
                                snapshot.child("accessibility").getValue(Int::class.java) ?: 5
                            val quality = snapshot.child("quality").getValue(Int::class.java) ?: 5
                            var numberReviews =
                                snapshot.child("numberReviews").getValue(Int::class.java) ?: 0
                            //val userKey = snapshot.key

                            val userToUpdateRef = snapshot.ref//.child(userKey!!)
                            Log.d("numberReviews", numberReviews.toString())

                            if (userHasAlreadyReviewed) {
                                userToUpdateRef.child("globalRating").setValue(
                                    updateAverage(
                                        globalRating,
                                        numberReviews,
                                        oldReview.globalRating,
                                        review.globalRating
                                    )
                                )
                                userToUpdateRef.child("comfort").setValue(
                                    updateAverage(
                                        comfort,
                                        numberReviews,
                                        oldReview.comfort,
                                        review.comfort
                                    )
                                )
                                userToUpdateRef.child("accessibility").setValue(
                                    updateAverage(
                                        accessibility,
                                        numberReviews,
                                        oldReview.accessibility,
                                        review.accessibility
                                    )
                                )
                                userToUpdateRef.child("quality").setValue(
                                    updateAverage(
                                        quality,
                                        numberReviews,
                                        oldReview.quality,
                                        review.quality
                                    )
                                )

                            } else {
                                userToUpdateRef.child("globalRating").setValue(
                                    newAverage(
                                        globalRating,
                                        numberReviews,
                                        review.globalRating
                                    )
                                )
                                userToUpdateRef.child("comfort")
                                    .setValue(newAverage(comfort, numberReviews, review.comfort))
                                userToUpdateRef.child("accessibility").setValue(
                                    newAverage(
                                        accessibility,
                                        numberReviews,
                                        review.accessibility
                                    )
                                )
                                userToUpdateRef.child("quality")
                                    .setValue(newAverage(quality, numberReviews, review.quality))
                                numberReviews++
                                userToUpdateRef.child("numberReviews").setValue(numberReviews)
                            }
                            onFinished(Unit)
                            found = true
                            break
                        }
                    }
                    if (!found) {
                        val newUserRef = markerDb.push()
                        newUserRef.child("markerName").setValue(review.markerName)
                        newUserRef.child("globalRating").setValue(review.globalRating)
                        newUserRef.child("comfort").setValue(review.comfort)
                        newUserRef.child("accessibility").setValue(review.accessibility)
                        newUserRef.child("quality").setValue(review.quality)
                        newUserRef.child("comments").setValue(listOf(review.comment))
                        newUserRef.child("photos").setValue(listOf(review.photo))
                        newUserRef.child("numberReviews").setValue(1)
                        onFinished(Unit)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("Error getting fav sports:", databaseError.toException())
                }
            })
        }

    }
}