package com.example.sports.auxiliary

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.sports.data.MarkerLocations
import com.example.sports.data.EventType
import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class Global{
    companion object{
        const val size = 40
        var userId = ""
        val favSports = hashSetOf<String>()
        val upvotes = hashSetOf<String>()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun isYesterday(localDateTime: LocalDateTime): Boolean {
    val todayStart = LocalDateTime.now().toLocalDate().atStartOfDay()
    return !localDateTime.toLocalDate().isEqual(todayStart.toLocalDate())
}


fun getEventType(eventType : String?) : EventType {
    if(eventType == null) return EventType.FOOTBALL
    return when (eventType) {
        "Football" -> EventType.FOOTBALL
        "Handball" -> EventType.HANDBALL
        "Basketball" -> EventType.BASKETBALL
        "Futsal" -> EventType.FUTSAL
        "Volleyball" -> EventType.VOLLEYBALL
        "Tennis" -> EventType.TENNIS
        else -> EventType.HOKEY
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun dateStringToLocalDateTime(date: String?): LocalDateTime {
    if(date == null) return LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    return LocalDateTime.parse(date, formatter)
}

fun getMarker(homeTeam : String,eventType: EventType,cupGame:String,stadiumName : String) : MarkerLocations{
    if(!cupGame.lowercase().contains("season")) return MarkerLocations.getStadiumForFootballCup(stadiumName)
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

fun getTimeFilterValue(time: String) : Long {
    return when(time) {
        "today" -> 0
        "1 day" -> 1
        "3 days" -> 3
        "5 days" -> 5
        "1 week" -> 7
        else -> 14
    }
}

fun requireLogin(context: Context): Boolean {
    val fileName = "userID.txt"
    val file = File(context.filesDir, fileName)

    if (!file.exists()) {
        Log.d("FileNotFound", "File $fileName does not exist.")
        file.createNewFile()
        return true
    }

    val content = file.readText()
    if(content.isEmpty()) return true
    Global.userId = content
    Log.d("FileContent", content)
    return false
}

fun writeIdToFile(context: Context, id: String) {
    val fileName = "userID.txt"
    val file = File(context.filesDir, fileName)
    try {
        val fileWriter = FileWriter(file, true)
        fileWriter.append(id)
        fileWriter.close()
        Log.d("IdWritten", "ID $id written to file.")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun clearFile(context: Context) {
    val file = File(context.filesDir, "userID.txt")
    file.delete()
    file.createNewFile()
}

fun updateAverage(currentAverage:Int,numbersUsed : Int,oldNumber:Int, newNumber : Int) : Int{
    Log.d("currentAverage",currentAverage.toString())
    Log.d("numbersUsed",numbersUsed.toString())
    Log.d("oldNumber",oldNumber.toString())
    Log.d("newNumber",newNumber.toString())
    var x = ((currentAverage * numbersUsed) - oldNumber + newNumber).toDouble()
    x /= numbersUsed
    return ceil(x).toInt()
}

fun newAverage(currentAverage:Int,numbersUsed : Int, newNumber : Int) : Int{
    Log.d("currentAverage",currentAverage.toString())
    Log.d("numbersUsed",numbersUsed.toString())
    Log.d("newNumber",newNumber.toString())
    var x : Double = ((currentAverage*numbersUsed) + newNumber).toDouble()
    Log.d("x",x.toString())
    x /= numbersUsed+1
    Log.d("x",x.toString())
    Log.d("x",ceil(x).toString())
    return ceil(x).toInt()
}