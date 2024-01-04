package com.example.soocer.auxiliary

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.soocer.data.MarkerLocations
import com.example.soocer.events.EventType
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


/*fun isYesterday(timestamp: Long): Boolean {
    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_YEAR)

    calendar.timeInMillis = timestamp
    val date = calendar.get(Calendar.DAY_OF_YEAR)
    Log.d("timestamp", date.toString())
    Log.d("hoje", today.toString())

    return date == (today - 1)
}*/

@RequiresApi(Build.VERSION_CODES.O)
fun isYesterday(localDateTime: LocalDateTime): Boolean {
    val yesterday = LocalDateTime.now().minus(1, ChronoUnit.DAYS)
    return localDateTime.isAfter(yesterday.toLocalDate().atStartOfDay()) &&
            localDateTime.isBefore(LocalDateTime.now().toLocalDate().atStartOfDay())
}


fun getEventType(eventType : String?) : EventType {
    if(eventType == null) return EventType.FOOTBALL
    return when (eventType) {
        "Football" -> EventType.FOOTBALL
        "Handball" -> EventType.HANDBALL
        "Basketball" -> EventType.BASKETBALL
        "Futsal" -> EventType.FUTSAL
        "Volleyball" -> EventType.VOLLEYBALL
        else -> EventType.HOKEY
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun dateStringToLocalDateTime(date: String?): LocalDateTime {
    if(date == null) return LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    return LocalDateTime.parse(date, formatter)
}

fun getMarker(homeTeam : String,eventType: EventType) : MarkerLocations{
    return when (eventType) {
        EventType.FOOTBALL -> MarkerLocations.getClubStadium(homeTeam)
        EventType.HANDBALL -> MarkerLocations.getClubPavilion(homeTeam)
        else -> MarkerLocations.getClubPavilion(homeTeam)
    }
}

fun convertStringToBoolean(bol : String) = bol == "true"


fun getDistanceBetweenTwoPoints(loc1 :LatLng,loc2 :LatLng) : Double{
    val lat1 = Math.toRadians(loc1.latitude)
    val lon1 = Math.toRadians(loc1.longitude)
    val lat2 = Math.toRadians(loc2.latitude)
    val lon2 = Math.toRadians(loc2.longitude)

    val dLon = lon2 - lon1
    val dLat = lat2 - lat1

    val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    val earthRadius = 6371.0

    return earthRadius * c
}